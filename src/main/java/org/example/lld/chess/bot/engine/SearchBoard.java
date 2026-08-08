package org.example.lld.chess.bot.engine;

import java.util.Random;

/**
 * A compact mutable position used only by the bot's search.
 *
 * <p>{@link org.example.lld.chess.Board} is the model the game and the UI speak; it copies
 * itself for every legality test, which is fine for a person clicking squares and far too
 * slow for a search that visits millions of positions. This class holds the same position
 * as a flat {@code int[64]} with make/unmake, so nothing is allocated while searching.
 *
 * <p>Square index is {@code row * 8 + col}, so index 0 is a1 and index 63 is h8, the same
 * orientation the rest of the code uses.
 */
public final class SearchBoard {

    public static final int EMPTY = 0;
    public static final int PAWN = 1, KNIGHT = 2, BISHOP = 3, ROOK = 4, QUEEN = 5, KING = 6;
    public static final int WHITE = 0, BLACK = 1;

    /** Castling rights bits. */
    public static final int WHITE_KING_SIDE = 1, WHITE_QUEEN_SIDE = 2,
            BLACK_KING_SIDE = 4, BLACK_QUEEN_SIDE = 8;

    /** Extra bits carried in an encoded move, on top of from, to and the promotion piece. */
    public static final int FLAG_EN_PASSANT = 1 << 16;
    public static final int FLAG_CASTLE = 1 << 17;
    public static final int FLAG_DOUBLE_PUSH = 1 << 18;

    private static final int MAX_HISTORY = 512;

    private static final int[] KNIGHT_STEPS = {-17, -15, -10, -6, 6, 10, 15, 17};
    private static final int[] KING_STEPS = {-9, -8, -7, -1, 1, 7, 8, 9};
    private static final int[] ROOK_DIRECTIONS = {-8, -1, 1, 8};
    private static final int[] BISHOP_DIRECTIONS = {-9, -7, 7, 9};

    /** Zobrist keys. Fixed seed so a position always hashes the same way. */
    private static final long[][] PIECE_KEYS = new long[15][64];
    private static final long[] CASTLING_KEYS = new long[16];
    private static final long[] EN_PASSANT_FILE_KEYS = new long[8];
    private static final long SIDE_KEY;

    static {
        Random random = new Random(0x5EED_C4E5L);
        for (int piece = 0; piece < 15; piece++) {
            for (int square = 0; square < 64; square++) {
                PIECE_KEYS[piece][square] = random.nextLong();
            }
        }
        for (int i = 0; i < 16; i++) CASTLING_KEYS[i] = random.nextLong();
        for (int i = 0; i < 8; i++) EN_PASSANT_FILE_KEYS[i] = random.nextLong();
        SIDE_KEY = random.nextLong();
    }

    public final int[] squares = new int[64];
    public final int[] kingSquare = new int[2];
    public int side;
    public int castling;
    public int epSquare = -1;
    public int halfMoveClock;
    public long key;

    private final int[] historyCaptured = new int[MAX_HISTORY];
    private final int[] historyCastling = new int[MAX_HISTORY];
    private final int[] historyEpSquare = new int[MAX_HISTORY];
    private final int[] historyHalfMove = new int[MAX_HISTORY];
    private final long[] historyKey = new long[MAX_HISTORY];
    private int historyDepth;

    // ----------------------------------------------------------------- encoding

    public static int piece(int colour, int type) {
        return (colour << 3) | type;
    }

    public static int colourOf(int piece) {
        return piece >> 3;
    }

    public static int typeOf(int piece) {
        return piece & 7;
    }

    public static int encode(int from, int to, int promotion, int flags) {
        return from | (to << 6) | (promotion << 12) | flags;
    }

    public static int fromOf(int move) {
        return move & 63;
    }

    public static int toOf(int move) {
        return (move >>> 6) & 63;
    }

    public static int promotionOf(int move) {
        return (move >>> 12) & 7;
    }

    private static int rowOf(int square) {
        return square >>> 3;
    }

    private static int colOf(int square) {
        return square & 7;
    }

    // ------------------------------------------------------------------- setup

    public void put(int square, int piece) {
        squares[square] = piece;
        if (typeOf(piece) == KING) kingSquare[colourOf(piece)] = square;
    }

    /** Recomputes the hash from scratch; called once after the position is loaded. */
    public void refreshKey() {
        long hash = 0;
        for (int square = 0; square < 64; square++) {
            int piece = squares[square];
            if (piece != EMPTY) hash ^= PIECE_KEYS[piece][square];
        }
        hash ^= CASTLING_KEYS[castling];
        if (epSquare >= 0) hash ^= EN_PASSANT_FILE_KEYS[colOf(epSquare)];
        if (side == BLACK) hash ^= SIDE_KEY;
        key = hash;
    }

    // -------------------------------------------------------------------- play

    public void make(int move) {
        historyCaptured[historyDepth] = EMPTY;
        historyCastling[historyDepth] = castling;
        historyEpSquare[historyDepth] = epSquare;
        historyHalfMove[historyDepth] = halfMoveClock;
        historyKey[historyDepth] = key;

        int from = fromOf(move);
        int to = toOf(move);
        int moving = squares[from];
        int colour = colourOf(moving);
        int type = typeOf(moving);

        if (epSquare >= 0) key ^= EN_PASSANT_FILE_KEYS[colOf(epSquare)];
        key ^= CASTLING_KEYS[castling];

        if ((move & FLAG_EN_PASSANT) != 0) {
            int victimSquare = to + (colour == WHITE ? -8 : 8);
            historyCaptured[historyDepth] = squares[victimSquare];
            key ^= PIECE_KEYS[squares[victimSquare]][victimSquare];
            squares[victimSquare] = EMPTY;
        } else if (squares[to] != EMPTY) {
            historyCaptured[historyDepth] = squares[to];
            key ^= PIECE_KEYS[squares[to]][to];
        }

        key ^= PIECE_KEYS[moving][from];
        squares[from] = EMPTY;

        int promotion = promotionOf(move);
        int landed = promotion != 0 ? piece(colour, promotion) : moving;
        squares[to] = landed;
        key ^= PIECE_KEYS[landed][to];

        if (type == KING) {
            kingSquare[colour] = to;
            castling &= colour == WHITE ? ~(WHITE_KING_SIDE | WHITE_QUEEN_SIDE)
                    : ~(BLACK_KING_SIDE | BLACK_QUEEN_SIDE);

            if ((move & FLAG_CASTLE) != 0) {
                boolean kingSide = colOf(to) == 6;
                int rookFrom = kingSide ? to + 1 : to - 2;
                int rookTo = kingSide ? to - 1 : to + 1;
                int rook = squares[rookFrom];
                squares[rookFrom] = EMPTY;
                squares[rookTo] = rook;
                key ^= PIECE_KEYS[rook][rookFrom] ^ PIECE_KEYS[rook][rookTo];
            }
        }
        castling &= rightsLostBy(from);
        castling &= rightsLostBy(to);
        key ^= CASTLING_KEYS[castling];

        epSquare = (move & FLAG_DOUBLE_PUSH) != 0 ? (from + to) / 2 : -1;
        if (epSquare >= 0) key ^= EN_PASSANT_FILE_KEYS[colOf(epSquare)];

        halfMoveClock = (type == PAWN || historyCaptured[historyDepth] != EMPTY) ? 0 : halfMoveClock + 1;
        side ^= 1;
        key ^= SIDE_KEY;
        historyDepth++;
    }

    public void unmake(int move) {
        historyDepth--;
        side ^= 1;

        int from = fromOf(move);
        int to = toOf(move);
        int landed = squares[to];
        int colour = side;
        int moving = promotionOf(move) != 0 ? piece(colour, PAWN) : landed;

        squares[from] = moving;
        squares[to] = EMPTY;

        if (typeOf(moving) == KING) {
            kingSquare[colour] = from;
            if ((move & FLAG_CASTLE) != 0) {
                boolean kingSide = colOf(to) == 6;
                int rookFrom = kingSide ? to + 1 : to - 2;
                int rookTo = kingSide ? to - 1 : to + 1;
                squares[rookFrom] = squares[rookTo];
                squares[rookTo] = EMPTY;
            }
        }

        int captured = historyCaptured[historyDepth];
        if (captured != EMPTY) {
            if ((move & FLAG_EN_PASSANT) != 0) {
                squares[to + (colour == WHITE ? -8 : 8)] = captured;
            } else {
                squares[to] = captured;
            }
        }

        castling = historyCastling[historyDepth];
        epSquare = historyEpSquare[historyDepth];
        halfMoveClock = historyHalfMove[historyDepth];
        key = historyKey[historyDepth];
    }

    /** A null move lets the search ask "what if I could skip a turn?". */
    public void makeNullMove() {
        historyCaptured[historyDepth] = EMPTY;
        historyCastling[historyDepth] = castling;
        historyEpSquare[historyDepth] = epSquare;
        historyHalfMove[historyDepth] = halfMoveClock;
        historyKey[historyDepth] = key;
        historyDepth++;

        if (epSquare >= 0) key ^= EN_PASSANT_FILE_KEYS[colOf(epSquare)];
        epSquare = -1;
        side ^= 1;
        key ^= SIDE_KEY;
        halfMoveClock++;
    }

    public void unmakeNullMove() {
        historyDepth--;
        side ^= 1;
        castling = historyCastling[historyDepth];
        epSquare = historyEpSquare[historyDepth];
        halfMoveClock = historyHalfMove[historyDepth];
        key = historyKey[historyDepth];
    }

    /** Corner and king squares that cost a castling right when touched. */
    private static int rightsLostBy(int square) {
        return switch (square) {
            case 0 -> ~WHITE_QUEEN_SIDE;
            case 7 -> ~WHITE_KING_SIDE;
            case 4 -> ~(WHITE_KING_SIDE | WHITE_QUEEN_SIDE);
            case 56 -> ~BLACK_QUEEN_SIDE;
            case 63 -> ~BLACK_KING_SIDE;
            case 60 -> ~(BLACK_KING_SIDE | BLACK_QUEEN_SIDE);
            default -> ~0;
        };
    }

    // ----------------------------------------------------------------- attacks

    public boolean inCheck(int colour) {
        return isAttacked(kingSquare[colour], colour ^ 1);
    }

    /** Is {@code square} attacked by any piece of {@code byColour}? */
    public boolean isAttacked(int square, int byColour) {
        int row = rowOf(square);
        int col = colOf(square);

        // pawns: a white pawn on the rank below attacks upwards
        int pawnRow = byColour == WHITE ? row - 1 : row + 1;
        if (pawnRow >= 0 && pawnRow < 8) {
            int pawn = piece(byColour, PAWN);
            if (col > 0 && squares[pawnRow * 8 + col - 1] == pawn) return true;
            if (col < 7 && squares[pawnRow * 8 + col + 1] == pawn) return true;
        }

        int knight = piece(byColour, KNIGHT);
        for (int step : KNIGHT_STEPS) {
            int target = square + step;
            if (target < 0 || target > 63 || distance(square, target) > 2) continue;
            if (squares[target] == knight) return true;
        }

        int king = piece(byColour, KING);
        for (int step : KING_STEPS) {
            int target = square + step;
            if (target < 0 || target > 63 || distance(square, target) != 1) continue;
            if (squares[target] == king) return true;
        }

        if (slidesTo(square, byColour, ROOK_DIRECTIONS, ROOK)) return true;
        return slidesTo(square, byColour, BISHOP_DIRECTIONS, BISHOP);
    }

    private boolean slidesTo(int square, int byColour, int[] directions, int straightType) {
        int wanted = piece(byColour, straightType);
        int queen = piece(byColour, QUEEN);
        for (int direction : directions) {
            int target = square;
            while (true) {
                int next = target + direction;
                if (next < 0 || next > 63 || distance(target, next) != 1) break;
                target = next;
                int occupant = squares[target];
                if (occupant == EMPTY) continue;
                if (occupant == wanted || occupant == queen) return true;
                break;
            }
        }
        return false;
    }

    /** Chebyshev distance, used to catch steps that wrapped around a board edge. */
    private static int distance(int a, int b) {
        int rowGap = Math.abs(rowOf(a) - rowOf(b));
        int colGap = Math.abs(colOf(a) - colOf(b));
        return Math.max(rowGap, colGap);
    }

    // -------------------------------------------------------- move generation

    /** Appends every pseudo legal move for the side to move; returns the new list size. */
    public int generateMoves(int[] list, int count) {
        return generate(list, count, false);
    }

    /** Captures and promotions only, for the quiescence search. */
    public int generateCaptures(int[] list, int count) {
        return generate(list, count, true);
    }

    private int generate(int[] list, int count, boolean capturesOnly) {
        int me = side;
        int forward = me == WHITE ? 8 : -8;
        int startRow = me == WHITE ? 1 : 6;
        int promotionRow = me == WHITE ? 7 : 0;

        for (int from = 0; from < 64; from++) {
            int occupant = squares[from];
            if (occupant == EMPTY || colourOf(occupant) != me) continue;

            switch (typeOf(occupant)) {
                case PAWN -> {
                    int oneUp = from + forward;
                    if (oneUp >= 0 && oneUp < 64 && squares[oneUp] == EMPTY) {
                        if (rowOf(oneUp) == promotionRow) {
                            count = addPromotions(list, count, from, oneUp, 0);
                        } else if (!capturesOnly) {
                            list[count++] = encode(from, oneUp, 0, 0);
                            int twoUp = oneUp + forward;
                            if (rowOf(from) == startRow && squares[twoUp] == EMPTY) {
                                list[count++] = encode(from, twoUp, 0, FLAG_DOUBLE_PUSH);
                            }
                        }
                    }
                    for (int sideStep = -1; sideStep <= 1; sideStep += 2) {
                        int target = from + forward + sideStep;
                        if (target < 0 || target > 63 || distance(from, target) != 1) continue;
                        int victim = squares[target];
                        if (victim != EMPTY && colourOf(victim) != me) {
                            if (rowOf(target) == promotionRow) {
                                count = addPromotions(list, count, from, target, 0);
                            } else {
                                list[count++] = encode(from, target, 0, 0);
                            }
                        } else if (victim == EMPTY && target == epSquare) {
                            list[count++] = encode(from, target, 0, FLAG_EN_PASSANT);
                        }
                    }
                }
                case KNIGHT -> count = addSteps(list, count, from, KNIGHT_STEPS, 2, me, capturesOnly);
                case KING -> {
                    count = addSteps(list, count, from, KING_STEPS, 1, me, capturesOnly);
                    if (!capturesOnly) count = addCastles(list, count, me);
                }
                case BISHOP -> count = addSlides(list, count, from, BISHOP_DIRECTIONS, me, capturesOnly);
                case ROOK -> count = addSlides(list, count, from, ROOK_DIRECTIONS, me, capturesOnly);
                case QUEEN -> {
                    count = addSlides(list, count, from, ROOK_DIRECTIONS, me, capturesOnly);
                    count = addSlides(list, count, from, BISHOP_DIRECTIONS, me, capturesOnly);
                }
                default -> { }
            }
        }
        return count;
    }

    private int addPromotions(int[] list, int count, int from, int to, int flags) {
        list[count++] = encode(from, to, QUEEN, flags);
        list[count++] = encode(from, to, KNIGHT, flags);
        list[count++] = encode(from, to, ROOK, flags);
        list[count++] = encode(from, to, BISHOP, flags);
        return count;
    }

    private int addSteps(int[] list, int count, int from, int[] steps, int maxDistance,
                         int me, boolean capturesOnly) {
        for (int step : steps) {
            int to = from + step;
            if (to < 0 || to > 63 || distance(from, to) > maxDistance) continue;
            int occupant = squares[to];
            if (occupant != EMPTY && colourOf(occupant) == me) continue;
            if (capturesOnly && occupant == EMPTY) continue;
            list[count++] = encode(from, to, 0, 0);
        }
        return count;
    }

    private int addSlides(int[] list, int count, int from, int[] directions, int me, boolean capturesOnly) {
        for (int direction : directions) {
            int to = from;
            while (true) {
                int next = to + direction;
                if (next < 0 || next > 63 || distance(to, next) != 1) break;
                to = next;
                int occupant = squares[to];
                if (occupant == EMPTY) {
                    if (!capturesOnly) list[count++] = encode(from, to, 0, 0);
                    continue;
                }
                if (colourOf(occupant) != me) list[count++] = encode(from, to, 0, 0);
                break;
            }
        }
        return count;
    }

    private int addCastles(int[] list, int count, int me) {
        int homeRow = me == WHITE ? 0 : 7;
        int kingSquareIndex = homeRow * 8 + 4;
        if (kingSquare[me] != kingSquareIndex) return count;

        int kingSideRight = me == WHITE ? WHITE_KING_SIDE : BLACK_KING_SIDE;
        int queenSideRight = me == WHITE ? WHITE_QUEEN_SIDE : BLACK_QUEEN_SIDE;
        int enemy = me ^ 1;

        if ((castling & kingSideRight) != 0
                && squares[kingSquareIndex + 1] == EMPTY && squares[kingSquareIndex + 2] == EMPTY
                && !isAttacked(kingSquareIndex, enemy)
                && !isAttacked(kingSquareIndex + 1, enemy)
                && !isAttacked(kingSquareIndex + 2, enemy)) {
            list[count++] = encode(kingSquareIndex, kingSquareIndex + 2, 0, FLAG_CASTLE);
        }
        if ((castling & queenSideRight) != 0
                && squares[kingSquareIndex - 1] == EMPTY && squares[kingSquareIndex - 2] == EMPTY
                && squares[kingSquareIndex - 3] == EMPTY
                && !isAttacked(kingSquareIndex, enemy)
                && !isAttacked(kingSquareIndex - 1, enemy)
                && !isAttacked(kingSquareIndex - 2, enemy)) {
            list[count++] = encode(kingSquareIndex, kingSquareIndex - 2, 0, FLAG_CASTLE);
        }
        return count;
    }

    /** After {@link #make(int)}, did the side that just moved leave its own king attacked? */
    public boolean lastMoveWasLegal() {
        return !isAttacked(kingSquare[side ^ 1], side);
    }

    /** True when this exact position already appeared earlier along the current line. */
    public boolean isRepetition() {
        int limit = Math.max(0, historyDepth - halfMoveClock);
        for (int i = historyDepth - 2; i >= limit; i -= 2) {
            if (historyKey[i] == key) return true;
        }
        return false;
    }
}
