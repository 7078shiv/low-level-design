package org.example.lld.chess.bot.engine;

import static org.example.lld.chess.bot.engine.SearchBoard.*;

/**
 * Alpha beta search with the usual set of speed-ups: iterative deepening, a transposition
 * table, killer and history move ordering, null move pruning, late move reductions and a
 * quiescence search so the bot is never fooled by a capture just over its horizon.
 *
 * <p>One instance searches one position at a time; {@link #findBestMove} is the entry point.
 */
public final class Search {

    private static final int MAX_PLY = 64;
    private static final int INFINITY = 1 << 24;

    /** Fixed size transposition table, a power of two so the index is a mask away. */
    private static final int TABLE_SIZE = 1 << 20;
    private static final int EXACT = 0, LOWER_BOUND = 1, UPPER_BOUND = 2;

    private final long[] tableKeys = new long[TABLE_SIZE];
    private final int[] tableMoves = new int[TABLE_SIZE];
    private final short[] tableScores = new short[TABLE_SIZE];
    private final byte[] tableDepths = new byte[TABLE_SIZE];
    private final byte[] tableFlags = new byte[TABLE_SIZE];

    private final int[][] moveLists = new int[MAX_PLY + 8][256];
    private final int[][] orderLists = new int[MAX_PLY + 8][256];
    private final int[][] killers = new int[MAX_PLY + 8][2];
    private final int[][] historyScores = new int[15][64];

    private SearchBoard board;
    private long deadline;
    private boolean abort;
    private long nodes;

    private int bestMove;
    private int bestScore;
    private int completedDepth;

    public int getNodes() {
        return (int) Math.min(nodes, Integer.MAX_VALUE);
    }

    public int getScore() {
        return bestScore;
    }

    public int getDepthReached() {
        return completedDepth;
    }

    /**
     * Searches until {@code maxDepth} or until the time budget runs out, whichever comes
     * first, and returns the best move found by the last depth that finished.
     */
    public int findBestMove(SearchBoard position, int maxDepth, long budgetMillis) {
        this.board = position;
        this.deadline = System.currentTimeMillis() + budgetMillis;
        this.abort = false;
        this.nodes = 0;
        this.bestMove = 0;
        this.bestScore = 0;
        this.completedDepth = 0;

        for (int[] killer : killers) {
            killer[0] = 0;
            killer[1] = 0;
        }
        for (int[] row : historyScores) {
            java.util.Arrays.fill(row, 0);
        }

        int previousBest = 0;
        for (int depth = 1; depth <= maxDepth; depth++) {
            int score = negamax(depth, 0, -INFINITY, INFINITY, true);
            if (abort) break;

            previousBest = bestMove;
            bestScore = score;
            completedDepth = depth;

            // a forced mate is as good as it gets, no point searching deeper
            if (Math.abs(score) >= Evaluation.MATE_THRESHOLD) break;
        }
        return previousBest != 0 ? previousBest : bestMove;
    }

    private int negamax(int depth, int ply, int alpha, int beta, boolean allowNull) {
        if (abort) return 0;
        if ((++nodes & 2047) == 0 && System.currentTimeMillis() > deadline) {
            abort = true;
            return 0;
        }

        boolean root = ply == 0;
        if (!root && (board.isRepetition() || board.halfMoveClock >= 100)) {
            return 0;
        }
        if (ply >= MAX_PLY) return Evaluation.evaluate(board);

        // mate distance pruning: never look for a slower mate than one already found
        alpha = Math.max(alpha, -Evaluation.MATE + ply);
        beta = Math.min(beta, Evaluation.MATE - ply - 1);
        if (alpha >= beta) return alpha;

        boolean inCheck = board.inCheck(board.side);
        if (inCheck) depth++;   // never stop the search with the king under fire

        if (depth <= 0) return quiescence(ply, alpha, beta);

        int index = (int) (board.key & (TABLE_SIZE - 1));
        int tableMove = 0;
        if (tableKeys[index] == board.key) {
            tableMove = tableMoves[index];
            if (!root && tableDepths[index] >= depth) {
                int stored = fromTableScore(tableScores[index], ply);
                int flag = tableFlags[index];
                if (flag == EXACT
                        || (flag == LOWER_BOUND && stored >= beta)
                        || (flag == UPPER_BOUND && stored <= alpha)) {
                    return stored;
                }
            }
        }

        // null move pruning: if skipping a turn still leaves the opponent worse off,
        // this branch is good enough to cut. Disabled in check and in thin endgames.
        if (allowNull && !root && !inCheck && depth >= 3 && beta < Evaluation.MATE_THRESHOLD
                && hasNonPawnMaterial(board.side)) {
            board.makeNullMove();
            int reduction = 2 + depth / 6;
            int score = -negamax(depth - 1 - reduction, ply + 1, -beta, -beta + 1, false);
            board.unmakeNullMove();
            if (abort) return 0;
            if (score >= beta) return beta;
        }

        int[] moves = moveLists[ply];
        int[] scores = orderLists[ply];
        int count = board.generateMoves(moves, 0);
        for (int i = 0; i < count; i++) {
            scores[i] = scoreMove(moves[i], tableMove, ply);
        }

        int originalAlpha = alpha;
        int best = -INFINITY;
        int bestLocalMove = 0;
        int legalMoves = 0;

        for (int i = 0; i < count; i++) {
            pickBest(moves, scores, i, count);
            int move = moves[i];

            board.make(move);
            if (!board.lastMoveWasLegal()) {
                board.unmake(move);
                continue;
            }
            legalMoves++;

            int score;
            if (legalMoves == 1) {
                score = -negamax(depth - 1, ply + 1, -beta, -alpha, true);
            } else {
                // late quiet moves are unlikely to beat the first one, so look shallower first
                int reduction = (depth >= 3 && legalMoves > 3 && !inCheck && isQuiet(move)) ? 1 : 0;
                score = -negamax(depth - 1 - reduction, ply + 1, -alpha - 1, -alpha, true);
                if (score > alpha && score < beta) {
                    score = -negamax(depth - 1, ply + 1, -beta, -alpha, true);
                }
            }
            board.unmake(move);
            if (abort) return 0;

            if (score > best) {
                best = score;
                bestLocalMove = move;
                if (root) bestMove = move;
            }
            if (score > alpha) alpha = score;
            if (alpha >= beta) {
                if (isQuiet(move)) {
                    rememberKiller(move, ply);
                    historyScores[board.squares[fromOf(move)]][toOf(move)] += depth * depth;
                }
                break;
            }
        }

        if (legalMoves == 0) {
            return inCheck ? -Evaluation.MATE + ply : 0;   // checkmate or stalemate
        }

        store(index, depth, best, originalAlpha, beta, bestLocalMove, ply);
        return best;
    }

    /** Plays out the captures so the evaluation only ever runs on a quiet position. */
    private int quiescence(int ply, int alpha, int beta) {
        if (abort) return 0;
        if ((++nodes & 2047) == 0 && System.currentTimeMillis() > deadline) {
            abort = true;
            return 0;
        }
        if (ply >= MAX_PLY) return Evaluation.evaluate(board);

        int standPat = Evaluation.evaluate(board);
        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;

        int[] moves = moveLists[ply];
        int[] scores = orderLists[ply];
        int count = board.generateCaptures(moves, 0);
        for (int i = 0; i < count; i++) {
            scores[i] = captureScore(moves[i]);
        }

        for (int i = 0; i < count; i++) {
            pickBest(moves, scores, i, count);
            int move = moves[i];

            board.make(move);
            if (!board.lastMoveWasLegal()) {
                board.unmake(move);
                continue;
            }
            int score = -quiescence(ply + 1, -beta, -alpha);
            board.unmake(move);
            if (abort) return 0;

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }
        return alpha;
    }

    // ------------------------------------------------------------- move order

    /** Selection sort one slot at a time, so a cutoff skips sorting the rest. */
    private static void pickBest(int[] moves, int[] scores, int from, int count) {
        int best = from;
        for (int i = from + 1; i < count; i++) {
            if (scores[i] > scores[best]) best = i;
        }
        if (best != from) {
            int move = moves[from];
            moves[from] = moves[best];
            moves[best] = move;
            int score = scores[from];
            scores[from] = scores[best];
            scores[best] = score;
        }
    }

    private int scoreMove(int move, int tableMove, int ply) {
        if (move == tableMove) return 10_000_000;

        int victim = board.squares[toOf(move)];
        if (victim != EMPTY || (move & FLAG_EN_PASSANT) != 0) return 1_000_000 + captureScore(move);
        if (promotionOf(move) != 0) return 900_000 + Evaluation.PIECE_VALUE[promotionOf(move)];
        if (move == killers[ply][0]) return 800_000;
        if (move == killers[ply][1]) return 700_000;
        return historyScores[board.squares[fromOf(move)]][toOf(move)];
    }

    /** Most valuable victim, least valuable attacker. */
    private int captureScore(int move) {
        int victim = board.squares[toOf(move)];
        int victimValue = victim == EMPTY
                ? Evaluation.PIECE_VALUE[PAWN]                 // en passant
                : Evaluation.PIECE_VALUE[typeOf(victim)];
        int attackerValue = Evaluation.PIECE_VALUE[typeOf(board.squares[fromOf(move)])];
        return victimValue * 16 - attackerValue + Evaluation.PIECE_VALUE[promotionOf(move)];
    }

    private boolean isQuiet(int move) {
        return board.squares[toOf(move)] == EMPTY && promotionOf(move) == 0
                && (move & FLAG_EN_PASSANT) == 0;
    }

    private void rememberKiller(int move, int ply) {
        if (killers[ply][0] == move) return;
        killers[ply][1] = killers[ply][0];
        killers[ply][0] = move;
    }

    private boolean hasNonPawnMaterial(int colour) {
        for (int square = 0; square < 64; square++) {
            int occupant = board.squares[square];
            if (occupant == EMPTY || colourOf(occupant) != colour) continue;
            int type = typeOf(occupant);
            if (type != PAWN && type != KING) return true;
        }
        return false;
    }

    // -------------------------------------------------------- transpositions

    private void store(int index, int depth, int score, int alpha, int beta, int move, int ply) {
        if (abort) return;
        // prefer the deeper analysis when two positions land on the same slot
        if (tableKeys[index] == board.key && tableDepths[index] > depth) return;

        tableKeys[index] = board.key;
        tableMoves[index] = move;
        tableDepths[index] = (byte) Math.min(depth, Byte.MAX_VALUE);
        tableScores[index] = (short) toTableScore(score, ply);
        tableFlags[index] = (byte) (score <= alpha ? UPPER_BOUND : score >= beta ? LOWER_BOUND : EXACT);
    }

    /** Mate scores are stored as a distance from the node, not from the root. */
    private static int toTableScore(int score, int ply) {
        if (score >= Evaluation.MATE_THRESHOLD) return score + ply;
        if (score <= -Evaluation.MATE_THRESHOLD) return score - ply;
        return score;
    }

    private static int fromTableScore(int score, int ply) {
        if (score >= Evaluation.MATE_THRESHOLD) return score - ply;
        if (score <= -Evaluation.MATE_THRESHOLD) return score + ply;
        return score;
    }
}
