package org.example.lld.chess.bot.engine;

import static org.example.lld.chess.bot.engine.SearchBoard.*;

/**
 * Static evaluation in centipawns, from the point of view of the side to move.
 *
 * <p>Material and square tables carry most of it; the king table is blended between a
 * "stay behind the pawns" opening table and a "walk to the centre" endgame table as
 * material comes off, which is what stops the bot from stalling in won endings.
 */
public final class Evaluation {

    public static final int MATE = 30_000;
    public static final int MATE_THRESHOLD = MATE - 1_000;

    static final int[] PIECE_VALUE = {0, 100, 320, 330, 500, 900, 0};

    /** Rough game phase weight per piece, used to blend the two king tables. */
    private static final int[] PHASE_WEIGHT = {0, 0, 1, 1, 2, 4, 0};
    private static final int TOTAL_PHASE = 24;

    private static final int BISHOP_PAIR = 30;
    private static final int DOUBLED_PAWN = -12;
    private static final int ISOLATED_PAWN = -16;
    private static final int[] PASSED_PAWN_BY_RANK = {0, 5, 10, 20, 35, 60, 100, 0};
    private static final int ROOK_ON_OPEN_FILE = 18;
    private static final int TEMPO = 8;

    // tables are written a1..h8 for white and mirrored for black
    private static final int[] PAWN_TABLE = {
              0,  0,  0,  0,  0,  0,  0,  0,
              5, 10, 10,-20,-20, 10, 10,  5,
              5, -5,-10,  0,  0,-10, -5,  5,
              0,  0,  0, 20, 20,  0,  0,  0,
              5,  5, 10, 25, 25, 10,  5,  5,
             10, 10, 20, 30, 30, 20, 10, 10,
             50, 50, 50, 50, 50, 50, 50, 50,
              0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] KNIGHT_TABLE = {
            -50,-40,-30,-30,-30,-30,-40,-50,
            -40,-20,  0,  5,  5,  0,-20,-40,
            -30,  5, 10, 15, 15, 10,  5,-30,
            -30,  0, 15, 20, 20, 15,  0,-30,
            -30,  5, 15, 20, 20, 15,  5,-30,
            -30,  0, 10, 15, 15, 10,  0,-30,
            -40,-20,  0,  0,  0,  0,-20,-40,
            -50,-40,-30,-30,-30,-30,-40,-50
    };

    private static final int[] BISHOP_TABLE = {
            -20,-10,-10,-10,-10,-10,-10,-20,
            -10,  5,  0,  0,  0,  0,  5,-10,
            -10, 10, 10, 10, 10, 10, 10,-10,
            -10,  0, 10, 10, 10, 10,  0,-10,
            -10,  5,  5, 10, 10,  5,  5,-10,
            -10,  0,  5, 10, 10,  5,  0,-10,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -20,-10,-10,-10,-10,-10,-10,-20
    };

    private static final int[] ROOK_TABLE = {
              0,  0,  5, 10, 10,  5,  0,  0,
             -5,  0,  0,  0,  0,  0,  0, -5,
             -5,  0,  0,  0,  0,  0,  0, -5,
             -5,  0,  0,  0,  0,  0,  0, -5,
             -5,  0,  0,  0,  0,  0,  0, -5,
             -5,  0,  0,  0,  0,  0,  0, -5,
              5, 10, 10, 10, 10, 10, 10,  5,
              0,  0,  0,  0,  0,  0,  0,  0
    };

    private static final int[] QUEEN_TABLE = {
            -20,-10,-10, -5, -5,-10,-10,-20,
            -10,  0,  5,  0,  0,  0,  0,-10,
            -10,  5,  5,  5,  5,  5,  0,-10,
              0,  0,  5,  5,  5,  5,  0, -5,
             -5,  0,  5,  5,  5,  5,  0, -5,
            -10,  0,  5,  5,  5,  5,  0,-10,
            -10,  0,  0,  0,  0,  0,  0,-10,
            -20,-10,-10, -5, -5,-10,-10,-20
    };

    private static final int[] KING_OPENING = {
             20, 30, 10,  0,  0, 10, 30, 20,
             20, 20,  0,  0,  0,  0, 20, 20,
            -10,-20,-20,-20,-20,-20,-20,-10,
            -20,-30,-30,-40,-40,-30,-30,-20,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30,
            -30,-40,-40,-50,-50,-40,-40,-30
    };

    private static final int[] KING_ENDGAME = {
            -50,-30,-30,-30,-30,-30,-30,-50,
            -30,-30,  0,  0,  0,  0,-30,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 30, 40, 40, 30,-10,-30,
            -30,-10, 20, 30, 30, 20,-10,-30,
            -30,-20,-10,  0,  0,-10,-20,-30,
            -50,-40,-30,-20,-20,-30,-40,-50
    };

    private Evaluation() {
    }

    public static int evaluate(SearchBoard board) {
        int[] squares = board.squares;

        int material = 0;         // white minus black, opening tables applied
        int kingOpening = 0;
        int kingEndgame = 0;
        int phase = 0;
        int bishops = 0, blackBishops = 0;

        int[] pawnsPerFile = new int[8];
        int[] blackPawnsPerFile = new int[8];

        for (int square = 0; square < 64; square++) {
            int occupant = squares[square];
            if (occupant == EMPTY) continue;

            int colour = colourOf(occupant);
            int type = typeOf(occupant);
            int mirrored = colour == WHITE ? square : mirror(square);
            int sign = colour == WHITE ? 1 : -1;

            phase += PHASE_WEIGHT[type];

            if (type == KING) {
                kingOpening += sign * KING_OPENING[mirrored];
                kingEndgame += sign * KING_ENDGAME[mirrored];
                continue;
            }

            material += sign * (PIECE_VALUE[type] + table(type)[mirrored]);

            switch (type) {
                case BISHOP -> {
                    if (colour == WHITE) bishops++; else blackBishops++;
                }
                case PAWN -> {
                    if (colour == WHITE) pawnsPerFile[square & 7]++; else blackPawnsPerFile[square & 7]++;
                }
                case ROOK -> {
                    // an open file is worth a little; measured after the pawn counts below
                }
                default -> { }
            }
        }

        if (bishops >= 2) material += BISHOP_PAIR;
        if (blackBishops >= 2) material -= BISHOP_PAIR;

        material += pawnStructure(squares, pawnsPerFile, blackPawnsPerFile);
        material += rookFiles(squares, pawnsPerFile, blackPawnsPerFile);

        int clamped = Math.min(phase, TOTAL_PHASE);
        int kingScore = (kingOpening * clamped + kingEndgame * (TOTAL_PHASE - clamped)) / TOTAL_PHASE;

        int white = material + kingScore;
        return (board.side == WHITE ? white : -white) + TEMPO;
    }

    private static int pawnStructure(int[] squares, int[] whiteFiles, int[] blackFiles) {
        int score = 0;
        for (int file = 0; file < 8; file++) {
            if (whiteFiles[file] > 1) score += DOUBLED_PAWN * (whiteFiles[file] - 1);
            if (blackFiles[file] > 1) score -= DOUBLED_PAWN * (blackFiles[file] - 1);

            boolean whiteNeighbours = (file > 0 && whiteFiles[file - 1] > 0)
                    || (file < 7 && whiteFiles[file + 1] > 0);
            boolean blackNeighbours = (file > 0 && blackFiles[file - 1] > 0)
                    || (file < 7 && blackFiles[file + 1] > 0);
            if (whiteFiles[file] > 0 && !whiteNeighbours) score += ISOLATED_PAWN;
            if (blackFiles[file] > 0 && !blackNeighbours) score -= ISOLATED_PAWN;
        }

        for (int square = 0; square < 64; square++) {
            int occupant = squares[square];
            if (occupant == EMPTY || typeOf(occupant) != PAWN) continue;
            int file = square & 7;
            int rank = square >>> 3;

            if (colourOf(occupant) == WHITE) {
                if (noBlockers(blackFiles, file) && noPawnAhead(squares, square, WHITE)) {
                    score += PASSED_PAWN_BY_RANK[rank];
                }
            } else if (noBlockers(whiteFiles, file) && noPawnAhead(squares, square, BLACK)) {
                score -= PASSED_PAWN_BY_RANK[7 - rank];
            }
        }
        return score;
    }

    /** Cheap first filter: nothing on the neighbouring files at all means nothing can stop it. */
    private static boolean noBlockers(int[] enemyFiles, int file) {
        return enemyFiles[file] == 0
                && (file == 0 || enemyFiles[file - 1] == 0)
                && (file == 7 || enemyFiles[file + 1] == 0);
    }

    private static boolean noPawnAhead(int[] squares, int square, int colour) {
        int step = colour == WHITE ? 8 : -8;
        int enemyPawn = piece(colour ^ 1, PAWN);
        for (int ahead = square + step; ahead >= 0 && ahead < 64; ahead += step) {
            if (squares[ahead] == enemyPawn) return false;
        }
        return true;
    }

    private static int rookFiles(int[] squares, int[] whiteFiles, int[] blackFiles) {
        int score = 0;
        for (int square = 0; square < 64; square++) {
            int occupant = squares[square];
            if (occupant == EMPTY || typeOf(occupant) != ROOK) continue;
            int file = square & 7;
            if (colourOf(occupant) == WHITE) {
                if (whiteFiles[file] == 0) score += ROOK_ON_OPEN_FILE;
            } else if (blackFiles[file] == 0) {
                score -= ROOK_ON_OPEN_FILE;
            }
        }
        return score;
    }

    private static int mirror(int square) {
        return square ^ 56;
    }

    private static int[] table(int type) {
        return switch (type) {
            case PAWN -> PAWN_TABLE;
            case KNIGHT -> KNIGHT_TABLE;
            case BISHOP -> BISHOP_TABLE;
            case ROOK -> ROOK_TABLE;
            case QUEEN -> QUEEN_TABLE;
            default -> KING_OPENING;
        };
    }
}
