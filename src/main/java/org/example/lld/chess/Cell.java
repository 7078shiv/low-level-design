package org.example.lld.chess;

import lombok.Data;
import org.example.lld.chess.pieces.Piece;


@Data
public class Cell {
    private Piece piece;
    private final Position position;

    public Cell(Piece piece, Position position) {
        this.piece = piece;
        this.position = position;
    }

    public boolean isEmpty() {
        return piece == null;
    }
}