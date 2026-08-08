package org.example.lld.chess;

import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;
import org.example.lld.chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static final int SIZE = 8;

    private final Cell[][] cells;

    public Board() {
        this(true);
        initPieces();
    }

    /** Empty board. {@link #copy()} fills it in itself. */
    private Board(boolean empty) {
        cells = new Cell[SIZE][SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new Cell(null, new Position(row, col));
            }
        }
    }

    public Cell getCell(Position position) {
        if (position == null || !position.isValid()) {
            return null;
        }
        return cells[position.getRow()][position.getCol()];
    }

    public Cell getCell(int row, int col) {
        return getCell(new Position(row, col));
    }

    public Cell[][] getCells() {
        return cells;
    }

    public void initPieces() {
        PieceType[] backRank = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };

        for (int col = 0; col < SIZE; col++) {
            cells[0][col].setPiece(PieceFactory.create(backRank[col], PieceColour.WHITE));
            cells[1][col].setPiece(new Pawn(PieceColour.WHITE));
            cells[6][col].setPiece(new Pawn(PieceColour.BLACK));
            cells[7][col].setPiece(PieceFactory.create(backRank[col], PieceColour.BLACK));
        }
    }

    /** Deep copy, so a move can be simulated without touching the live board. */
    public Board copy() {
        Board clone = new Board(true);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece piece = cells[row][col].getPiece();
                clone.cells[row][col].setPiece(piece == null ? null : piece.copy());
            }
        }
        return clone;
    }

    public Cell findKing(PieceColour colour) {
        for (Cell[] rank : cells) {
            for (Cell cell : rank) {
                Piece piece = cell.getPiece();
                if (piece != null && piece.getPieceType() == PieceType.KING && piece.getPieceColour() == colour) {
                    return cell;
                }
            }
        }
        return null;
    }

    public List<Cell> cellsWithPiecesOf(PieceColour colour) {
        List<Cell> occupied = new ArrayList<>();
        for (Cell[] rank : cells) {
            for (Cell cell : rank) {
                if (cell.getPiece() != null && cell.getPiece().getPieceColour() == colour) {
                    occupied.add(cell);
                }
            }
        }
        return occupied;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = SIZE - 1; row >= 0; row--) {
            sb.append(row + 1).append(' ');
            for (int col = 0; col < SIZE; col++) {
                Piece piece = cells[row][col].getPiece();
                sb.append(piece == null ? "." : piece.getSymbol()).append(' ');
            }
            sb.append('\n');
        }
        sb.append("  a b c d e f g h");
        return sb.toString();
    }
}
