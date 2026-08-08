package org.example.lld.chess;

import lombok.Data;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;

/**
 * A move that has actually been played. Positions are stored (not Cells) because
 * cells are live objects on the board and would mutate under the history.
 */
@Data
public class Move {
    private final Player player;
    private final Position from;
    private final Position to;
    private final PieceType pieceMoved;
    private final PieceColour colour;

    private PieceType pieceKilled;
    private Position capturedAt;

    private boolean castling;
    private boolean enPassant;
    private PieceType promotedTo;

    private boolean check;
    private boolean checkmate;

    /** Standard algebraic notation, filled in by Game once the move is resolved. */
    private String notation;

    public Move(Player player, Position from, Position to, PieceType pieceMoved) {
        this.player = player;
        this.from = from;
        this.to = to;
        this.pieceMoved = pieceMoved;
        this.colour = player.getColour();
    }

    public boolean isCapture() {
        return pieceKilled != null;
    }

    @Override
    public String toString() {
        return notation != null ? notation : from.toAlgebraic() + to.toAlgebraic();
    }
}
