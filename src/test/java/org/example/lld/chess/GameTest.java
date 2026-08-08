package org.example.lld.chess;

import org.example.lld.chess.enums.GameStatus;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private static void play(Game game, String... squares) {
        for (int i = 0; i < squares.length; i += 2) {
            game.makeMove(squares[i], squares[i + 1]);
        }
    }

    @Test
    void openingPositionHasTwentyLegalMoves() {
        Game game = new Game();
        int total = game.getAllLegalMoves().values().stream().mapToInt(List::size).sum();
        assertEquals(20, total, "16 pawn moves plus 4 knight moves");
    }

    @Test
    void pawnsMoveTowardsTheOpponentOnly() {
        Game game = new Game();
        game.makeMove("e2", "e4");
        assertNull(game.getBoard().getCell(Position.fromAlgebraic("e2")).getPiece());
        assertEquals(PieceType.PAWN, game.getBoard().getCell(Position.fromAlgebraic("e4")).getPiece().getPieceType());
        assertEquals(PieceColour.BLACK, game.getTurn());

        // black walks down the board: e6 and e5, never e8
        List<Position> blackPawn = game.getLegalDestinations(Position.fromAlgebraic("e7"));
        assertEquals(2, blackPawn.size());
        assertTrue(blackPawn.contains(Position.fromAlgebraic("e6")));
        assertTrue(blackPawn.contains(Position.fromAlgebraic("e5")));
        assertThrows(IllegalArgumentException.class, () -> game.makeMove("e7", "e8"));
    }

    @Test
    void rookIsBlockedByItsOwnPawn() {
        Game game = new Game();
        assertTrue(game.getLegalDestinations(Position.fromAlgebraic("a1")).isEmpty());
    }

    @Test
    void scholarsMateEndsTheGame() {
        Game game = new Game();
        play(game, "e2", "e4", "e7", "e5", "f1", "c4", "b8", "c6", "d1", "h5", "g8", "f6");
        Move mate = game.makeMove("h5", "f7");

        assertTrue(mate.isCheckmate());
        assertEquals("Qxf7#", mate.getNotation());
        assertEquals(GameStatus.WHITE_WIN, game.getStatus());
        assertEquals("Checkmate", game.getResultReason());
        assertTrue(game.isOver());
    }

    @Test
    void aPinnedPieceMayNotMove() {
        Game game = new Game();
        // Ruy Lopez: after ...d6 the b5 bishop pins the c6 knight against the king on e8
        play(game, "e2", "e4", "e7", "e5", "g1", "f3", "b8", "c6", "f1", "b5", "d7", "d6", "a2", "a3");
        assertTrue(game.getLegalDestinations(Position.fromAlgebraic("c6")).isEmpty(),
                "the knight shields the king from the bishop");
    }

    @Test
    void kingSideCastlingMovesTheRookToo() {
        Game game = new Game();
        play(game, "e2", "e4", "e7", "e5", "g1", "f3", "b8", "c6", "f1", "c4", "f8", "c5");
        Move castle = game.makeMove("e1", "g1");

        assertTrue(castle.isCastling());
        assertEquals("O-O", castle.getNotation());
        assertEquals(PieceType.KING, game.getBoard().getCell(Position.fromAlgebraic("g1")).getPiece().getPieceType());
        assertEquals(PieceType.ROOK, game.getBoard().getCell(Position.fromAlgebraic("f1")).getPiece().getPieceType());
        assertNull(game.getBoard().getCell(Position.fromAlgebraic("h1")).getPiece());
    }

    @Test
    void castlingIsForbiddenThroughAnAttackedSquare() {
        Game game = new Game();
        // a black knight on g3 covers f1, the square the white king would cross
        play(game, "g1", "f3", "g8", "f6", "e2", "e3", "f6", "e4", "f1", "e2", "e4", "g3");
        assertFalse(game.isInCheck(PieceColour.WHITE), "the knight attacks f1 and h1, not e1");
        assertFalse(game.getLegalDestinations(Position.fromAlgebraic("e1")).contains(Position.fromAlgebraic("g1")));
    }

    @Test
    void enPassantCapturesThePawnThatJustPassed() {
        Game game = new Game();
        play(game, "e2", "e4", "a7", "a6", "e4", "e5", "d7", "d5");
        assertEquals("d6", game.getEnPassantTarget().toAlgebraic());

        Move capture = game.makeMove("e5", "d6");
        assertTrue(capture.isEnPassant());
        assertEquals("exd6", capture.getNotation());
        assertNull(game.getBoard().getCell(Position.fromAlgebraic("d5")).getPiece(), "the black pawn is gone");
        assertEquals(1, game.getCapturedByWhite().size());
    }

    @Test
    void enPassantExpiresAfterOnePly() {
        Game game = new Game();
        play(game, "e2", "e4", "a7", "a6", "e4", "e5", "d7", "d5", "a2", "a3", "h7", "h6");
        assertNull(game.getEnPassantTarget());
        assertFalse(game.getLegalDestinations(Position.fromAlgebraic("e5")).contains(Position.fromAlgebraic("d6")));
    }

    @Test
    void promotionReplacesThePawn() {
        Game game = new Game();
        play(game, "h2", "h4", "g7", "g5", "h4", "g5", "g8", "f6", "g5", "g6", "f6", "h5",
                "g6", "g7", "h5", "f6");
        Move promotion = game.makeMove(Position.fromAlgebraic("g7"), Position.fromAlgebraic("h8"), PieceType.KNIGHT);

        assertEquals(PieceType.KNIGHT, promotion.getPromotedTo());
        assertEquals(PieceType.KNIGHT,
                game.getBoard().getCell(Position.fromAlgebraic("h8")).getPiece().getPieceType());
        assertTrue(promotion.getNotation().startsWith("gxh8=N"));
    }

    @Test
    void stalemateIsADraw() {
        Game game = new Game();
        play(game, "e2", "e3", "a7", "a5", "d1", "h5", "a8", "a6", "h5", "a5", "h7", "h5",
                "a5", "c7", "a6", "h6", "h2", "h4", "f7", "f6", "c7", "d7", "e8", "f7",
                "d7", "b7", "d8", "d3", "b7", "b8", "d3", "h7", "b8", "c8", "f7", "g6",
                "c8", "e6");

        assertEquals(GameStatus.DRAW, game.getStatus());
        assertEquals("Stalemate", game.getResultReason());
    }

    @Test
    void undoRestoresTheWholePosition() {
        Game game = new Game();
        play(game, "e2", "e4", "d7", "d5", "e4", "d5");
        assertEquals(1, game.getCapturedByWhite().size());

        assertTrue(game.undo());
        assertEquals(0, game.getCapturedByWhite().size());
        assertEquals(PieceColour.WHITE, game.getTurn());
        assertEquals(PieceType.PAWN, game.getBoard().getCell(Position.fromAlgebraic("d5")).getPiece().getPieceType());
        assertEquals(PieceColour.BLACK,
                game.getBoard().getCell(Position.fromAlgebraic("d5")).getPiece().getPieceColour());
        assertEquals(2, game.getMoveHistory().size());
    }

    @Test
    void resigningEndsTheGame() {
        Game game = new Game("Shivang", "Opponent");
        game.resign(PieceColour.WHITE);
        assertEquals(GameStatus.BLACK_WIN, game.getStatus());
        assertEquals("Shivang resigned", game.getResultReason());
        assertThrows(IllegalStateException.class, () -> game.makeMove("e2", "e4"));
    }

    @Test
    void notationDisambiguatesBetweenTwoKnights() {
        Game game = new Game();
        // knights on c3 and g5 can both reach e4, so the file has to be spelled out
        play(game, "g1", "f3", "a7", "a6", "b1", "c3", "b7", "b6", "f3", "g5", "c7", "c6");
        Move move = game.makeMove("c3", "e4");
        assertEquals("Nce4", move.getNotation());
    }
}
