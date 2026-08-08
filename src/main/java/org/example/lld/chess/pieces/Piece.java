package org.example.lld.chess.pieces;

import lombok.Data;
import org.example.lld.chess.Board;
import org.example.lld.chess.Cell;
import org.example.lld.chess.Position;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;

@Data
public abstract class Piece {
    private final PieceType pieceType;
    private final PieceColour pieceColour;
    private boolean killed;
    /** Needed by castling (king/rook) and the pawn double step. */
    private boolean hasMoved;

    protected Piece(PieceType pieceType, PieceColour pieceColour) {
        this.pieceType = pieceType;
        this.pieceColour = pieceColour;
        this.killed = false;
        this.hasMoved = false;
    }

    /**
     * Walks every square strictly between {@code start} and {@code end}.
     * Only meaningful for straight or diagonal lines, which is where callers use it.
     */
    protected boolean isPathClear(Board board, Cell start, Cell end) {
        int endRow = end.getPosition().getRow();
        int endCol = end.getPosition().getCol();
        int rowStep = Integer.compare(endRow, start.getPosition().getRow());
        int colStep = Integer.compare(endCol, start.getPosition().getCol());

        int row = start.getPosition().getRow() + rowStep;
        int col = start.getPosition().getCol() + colStep;

        // Stop when *both* coordinates reach the target: on a rank/file move one of
        // them never changes, so an && here would exit before checking anything.
        while (row != endRow || col != endCol) {
            if (board.getCell(new Position(row, col)).getPiece() != null) {
                return false;
            }
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    /** A destination is reachable when it is empty or holds an opponent piece. */
    protected boolean canLandOn(Cell end) {
        Piece target = end.getPiece();
        return target == null || target.getPieceColour() != pieceColour;
    }

    /**
     * Geometry-only validation: does not consider whose turn it is, nor whether the
     * move would expose the own king. {@link org.example.lld.chess.Game} layers that on top.
     */
    public abstract boolean isValidMove(Board board, Cell start, Cell end);

    public Piece copy() {
        Piece clone = PieceFactory.create(pieceType, pieceColour);
        clone.setHasMoved(hasMoved);
        clone.setKilled(killed);
        return clone;
    }

    /** Upper case for white, lower case for black. */
    public String getSymbol() {
        String symbol = switch (pieceType) {
            case KING -> "K";
            case QUEEN -> "Q";
            case KNIGHT -> "N";
            case PAWN -> "P";
            case ROOK -> "R";
            case BISHOP -> "B";
        };
        return pieceColour == PieceColour.WHITE ? symbol : symbol.toLowerCase();
    }

    @Override
    public String toString() {
        return getSymbol();
    }
}