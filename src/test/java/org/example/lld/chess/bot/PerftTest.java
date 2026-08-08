package org.example.lld.chess.bot;

import org.example.lld.chess.Game;
import org.example.lld.chess.MoveOption;
import org.example.lld.chess.bot.engine.SearchBoard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Perft counts every legal move sequence to a given depth. The numbers below are the
 * published values for the opening position, so if move generation is wrong anywhere
 * (castling, en passant, promotion, pins) the count will not match.
 */
class PerftTest {

    private static long perft(SearchBoard board, int depth) {
        if (depth == 0) return 1;

        int[] moves = new int[256];
        int count = board.generateMoves(moves, 0);
        long total = 0;
        for (int i = 0; i < count; i++) {
            board.make(moves[i]);
            if (board.lastMoveWasLegal()) {
                total += depth == 1 ? 1 : perft(board, depth - 1);
            }
            board.unmake(moves[i]);
        }
        return total;
    }

    private static SearchBoard startPosition() {
        return EngineBot.toSearchBoard(new Game());
    }

    @Test
    void perftMatchesTheKnownCountsForTheOpeningPosition() {
        assertEquals(20, perft(startPosition(), 1));
        assertEquals(400, perft(startPosition(), 2));
        assertEquals(8_902, perft(startPosition(), 3));
        assertEquals(197_281, perft(startPosition(), 4));
        assertEquals(4_865_609, perft(startPosition(), 5));
    }

    /** A position rich in castling, en passant and promotions, from the standard test set. */
    @Test
    void perftMatchesAfterCastlingAndEnPassantAppear() {
        Game game = new Game();
        // 1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4 Nf6 5. Nc3 a6 6. Be3 e5 7. Nb3 Be7 8. Be2 O-O
        String[] moves = {"e2", "e4", "c7", "c5", "g1", "f3", "d7", "d6", "d2", "d4", "c5", "d4",
                "f3", "d4", "g8", "f6", "b1", "c3", "a7", "a6", "c1", "e3", "e7", "e5",
                "d4", "b3", "f8", "e7", "f1", "e2", "e8", "g8"};
        for (int i = 0; i < moves.length; i += 2) {
            game.makeMove(moves[i], moves[i + 1]);
        }

        // both generators must agree on the legal move count at this position
        SearchBoard board = EngineBot.toSearchBoard(game);
        assertEquals(game.legalMoveOptions().size(), perft(board, 1),
                "the engine and the LLD must see the same legal moves");
    }

    /** Cross-checks the two move generators over a whole game played by the bot. */
    @Test
    void engineAndDomainAgreeThroughoutAGame() {
        Game game = new Game();
        EngineBot bot = new EngineBot(3, 200);

        for (int ply = 0; ply < 40 && !game.isOver(); ply++) {
            SearchBoard board = EngineBot.toSearchBoard(game);
            assertEquals(game.legalMoveOptions().size(), perft(board, 1),
                    "disagreement at ply " + ply + " after " + game.getMoveHistory());

            MoveOption option = bot.chooseMove(game);
            game.makeMove(option.from(), option.to(),
                    option.promotion() == null ? org.example.lld.chess.enums.PieceType.QUEEN : option.promotion());
        }
    }
}
