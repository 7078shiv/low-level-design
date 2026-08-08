package org.example.lld.chess.pieces;

import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;

/** Single place that knows how to turn a {@link PieceType} into a concrete piece. */
public final class PieceFactory {

    private PieceFactory() {
    }

    public static Piece create(PieceType type, PieceColour colour) {
        return switch (type) {
            case KING -> new King(colour);
            case QUEEN -> new Queen(colour);
            case ROOK -> new Rook(colour);
            case BISHOP -> new Bishop(colour);
            case KNIGHT -> new Knight(colour);
            case PAWN -> new Pawn(colour);
        };
    }
}