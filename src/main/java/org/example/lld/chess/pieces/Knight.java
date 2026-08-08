package org.example.lld.chess.pieces;

import org.example.lld.chess.Board;
import org.example.lld.chess.Cell;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;

public class Knight extends Piece {
    public Knight(PieceColour pieceColour) {
        super(PieceType.KNIGHT, pieceColour);
    }

    @Override
    public boolean isValidMove(Board board, Cell start, Cell end) {
        if (start.getPosition().equals(end.getPosition())) return false;
        if (!canLandOn(end)) return false;

        int rowDiff = Math.abs(start.getPosition().getRow() - end.getPosition().getRow());
        int colDiff = Math.abs(start.getPosition().getCol() - end.getPosition().getCol());

        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
}
