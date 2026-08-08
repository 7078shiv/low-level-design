package org.example.lld.chess.pieces;

import org.example.lld.chess.Board;
import org.example.lld.chess.Cell;
import org.example.lld.chess.Position;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;

public class Pawn extends Piece {

    public Pawn(PieceColour pieceColour) {
        super(PieceType.PAWN, pieceColour);
    }

    /** White starts on row 1 and walks up the board, black starts on row 6 and walks down. */
    public static int forwardDirection(PieceColour colour) {
        return colour == PieceColour.WHITE ? 1 : -1;
    }

    public static int startRow(PieceColour colour) {
        return colour == PieceColour.WHITE ? 1 : 6;
    }

    public static int promotionRow(PieceColour colour) {
        return colour == PieceColour.WHITE ? 7 : 0;
    }

    /** En passant is not handled here: it depends on the previous move, so Game adds it. */
    @Override
    public boolean isValidMove(Board board, Cell start, Cell end) {
        if (start.getPosition().equals(end.getPosition())) return false;
        if (!canLandOn(end)) return false;

        int direction = forwardDirection(getPieceColour());
        int rowDelta = end.getPosition().getRow() - start.getPosition().getRow();
        int colDiff = Math.abs(end.getPosition().getCol() - start.getPosition().getCol());
        boolean destinationEmpty = end.getPiece() == null;

        // single step forward
        if (rowDelta == direction && colDiff == 0) {
            return destinationEmpty;
        }

        // double step from the starting row, both squares must be free
        if (rowDelta == 2 * direction && colDiff == 0
                && start.getPosition().getRow() == startRow(getPieceColour())) {
            Position stepOver = new Position(start.getPosition().getRow() + direction, start.getPosition().getCol());
            return destinationEmpty && board.getCell(stepOver).getPiece() == null;
        }

        // diagonal capture
        if (rowDelta == direction && colDiff == 1) {
            return !destinationEmpty;
        }

        return false;
    }
}
