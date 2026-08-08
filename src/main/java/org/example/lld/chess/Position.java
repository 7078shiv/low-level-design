package org.example.lld.chess;

import lombok.Data;

@Data
public class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean isValid() {
        return row >= 0 && row <= 7 && col >= 0 && col <= 7;
    }

    /** Board coordinates as algebraic notation, e.g. (0,4) -> "e1". */
    public String toAlgebraic() {
        return "" + (char) ('a' + col) + (char) ('1' + row);
    }

    /** Parses "e1" style notation. Returns null when the text is not a square. */
    public static Position fromAlgebraic(String square) {
        if (square == null || square.length() != 2) return null;
        int col = square.charAt(0) - 'a';
        int row = square.charAt(1) - '1';
        Position position = new Position(row, col);
        return position.isValid() ? position : null;
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}