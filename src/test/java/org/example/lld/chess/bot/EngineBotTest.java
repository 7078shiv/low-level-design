package org.example.lld.chess.bot;

import org.example.lld.chess.Game;
import org.example.lld.chess.MoveOption;
import org.example.lld.chess.Position;
import org.example.lld.chess.enums.GameStatus;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;
import org.example.lld.chess.player.BotPlayer;
import org.example.lld.chess.player.HumanPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineBotTest {

    private static void play(Game game, String... squares) {
        for (int i = 0; i < squares.length; i += 2) {
            game.makeMove(squares[i], squares[i + 1]);
        }
    }

    private static void playBotMove(Game game, EngineBot bot) {
        MoveOption option = bot.chooseMove(game);
        game.makeMove(option.from(), option.to(),
                option.promotion() == null ? PieceType.QUEEN : option.promotion());
    }

    @Test
    void findsMateInOne() {
        Game game = new Game();
        // black has walked into the back rank mate Qh5xf7
        play(game, "e2", "e4", "e7", "e5", "f1", "c4", "b8", "c6", "d1", "h5", "g8", "f6");

        EngineBot bot = new EngineBot(4, 1_000);
        playBotMove(game, bot);

        assertEquals(GameStatus.WHITE_WIN, game.getStatus());
        assertEquals("Qxf7#", game.getMoveHistory().get(game.getMoveHistory().size() - 1).getNotation());
    }

    @Test
    void takesAFreeQueen() {
        Game game = new Game();
        // black walks the queen to d4 where the knight on f3 can simply take it
        play(game, "e2", "e4", "d7", "d5", "e4", "d5", "d8", "d5", "g1", "f3", "d5", "d4");

        MoveOption option = new EngineBot(4, 1_000).chooseMove(game);
        assertEquals("d4", option.to().toAlgebraic(), "the queen on d4 is free");
    }

    @Test
    void doesNotHangItsOwnQueenForNothing() {
        Game game = new Game();
        play(game, "e2", "e4", "e7", "e5", "g1", "f3", "b8", "c6");

        // white to move: every sensible move keeps the queen; a blunder would drop it
        MoveOption option = new EngineBot(5, 1_500).chooseMove(game);
        game.makeMove(option.from(), option.to());

        assertNotNull(game.getBoard().findKing(PieceColour.WHITE));
        assertFalse(game.getAllLegalMoves().isEmpty());
        // black should have no way to win material outright on the reply
        int before = countMaterial(game, PieceColour.WHITE);
        MoveOption reply = new EngineBot(4, 800).chooseMove(game);
        game.makeMove(reply.from(), reply.to());
        assertTrue(countMaterial(game, PieceColour.WHITE) >= before - 100,
                "white should not have dropped more than a pawn");
    }

    @Test
    void reachesRespectableDepthWithinItsBudget() {
        Game game = new Game();
        play(game, "e2", "e4", "e7", "e5", "g1", "f3", "b8", "c6", "f1", "b5", "a7", "a6");

        EngineBot bot = new EngineBot(64, 3_000);
        long start = System.currentTimeMillis();
        assertNotNull(bot.chooseMove(game));
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(bot.lastDepthReached() >= 6,
                "expected at least 6 ply in 3 seconds, reached " + bot.lastDepthReached());
        assertTrue(elapsed < 6_000, "took " + elapsed + "ms, which is well over the budget");
        System.out.println("depth reached: " + bot.lastDepthReached() + " in " + elapsed + "ms");
    }

    @Test
    void theHardBotBeatsTheEasyOne() {
        Game game = new Game(
                new BotPlayer(PieceColour.WHITE, BotLevel.HARD),
                new BotPlayer(PieceColour.BLACK, BotLevel.EASY));

        EngineBot strong = new EngineBot(6, 700);
        EngineBot weak = new EngineBot(1, 50);

        for (int ply = 0; ply < 80 && !game.isOver(); ply++) {
            playBotMove(game, game.getTurn() == PieceColour.WHITE ? strong : weak);
        }

        int white = countMaterial(game, PieceColour.WHITE);
        int black = countMaterial(game, PieceColour.BLACK);
        assertTrue(game.getStatus() == GameStatus.WHITE_WIN || white > black + 300,
                "the strong bot should be clearly winning: white " + white + " black " + black
                        + " status " + game.getStatus());
        System.out.println("strong vs weak after " + game.getMoveHistory().size()
                + " plies: white " + white + " black " + black + " status " + game.getStatus());
    }

    @Test
    void playersKnowWhetherTheyAreBots() {
        Game game = new Game(
                new HumanPlayer(PieceColour.WHITE, "Shivang"),
                new BotPlayer(PieceColour.BLACK, BotLevel.HARD));

        assertFalse(game.getWhitePlayer().isBot());
        assertTrue(game.getBlackPlayer().isBot());
        assertEquals("Bot (hard)", game.getBlackPlayer().getName());
    }

    private static int countMaterial(Game game, PieceColour colour) {
        int total = 0;
        for (var cell : game.getBoard().cellsWithPiecesOf(colour)) {
            total += switch (cell.getPiece().getPieceType()) {
                case PAWN -> 100;
                case KNIGHT -> 320;
                case BISHOP -> 330;
                case ROOK -> 500;
                case QUEEN -> 900;
                case KING -> 0;
            };
        }
        return total;
    }

    @Test
    void promotesWhenItCan() {
        Game game = new Game();
        play(game, "h2", "h4", "g7", "g5", "h4", "g5", "g8", "f6", "g5", "g6", "f6", "h5",
                "g6", "g7", "h5", "f6");

        MoveOption option = new EngineBot(4, 1_000).chooseMove(game);
        assertEquals("g7", option.from().toAlgebraic());
        assertEquals(PieceType.QUEEN, option.promotion(), "a free queen is the right promotion here");
        assertEquals(Position.fromAlgebraic("h8"), option.to());
    }
}
