package org.example.lld.chess;

import org.example.lld.chess.enums.PieceType;

/**
 * A legal move offered by the engine, before anyone commits to playing it.
 * {@code promotion} is null unless the move puts a pawn on the last rank.
 */
public record MoveOption(Position from, Position to, PieceType promotion) {

    public MoveOption(Position from, Position to) {
        this(from, to, null);
    }

    @Override
    public String toString() {
        return from.toAlgebraic() + to.toAlgebraic()
                + (promotion == null ? "" : "=" + promotion.name().charAt(0));
    }
}
