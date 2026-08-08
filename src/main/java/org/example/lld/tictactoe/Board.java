package org.example.lld.tictactoe;

import lombok.Getter;

public class Board {
    @Getter
    int size;
    Cell[][] grid;
    int movesMade = 0;
    int[] rowCount,columnCount;  // O(1) time
    int diagonalCount,antiDiagonalCount;

    Board(int size) {
        this.size = size;
        grid = new Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
        rowCount = new int[size];
        columnCount = new int[size];
        diagonalCount = 0;
        antiDiagonalCount = 0;
    }

    boolean applyMove(Move move) {
        int r = move.row;
        int c = move.col;
        grid[r][c].setSymbol(move.symbol);
        movesMade++;
        int val = move.symbol == Symbol.X ? 1 : -1;
        rowCount[r] += val;
        columnCount[c] += val;
        if(r == c){
            diagonalCount++;
        }
        if(size == r+c){
            antiDiagonalCount++;
        }
        return rowCount[r] == size || columnCount[c] == size || diagonalCount == size || antiDiagonalCount == size;
    }

    boolean isValidMove(Move move) {
        return move.row<size && move.row>=0 && move.col<size && move.col>=0 && grid[move.row][move.col].isEmpty();
    }

    boolean isFull(){
        return movesMade == size*size;
    }

    boolean isCellEmpty(int r, int c){
        return grid[r][c].isEmpty();
    }

    public void print(){
        for (int i = 0; i < size; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < size; j++) {
                sb.append(grid[i][j].isEmpty() ? "-" : grid[i][j].getSymbol().name()).append(" ");
            }
            System.out.println(sb);
        }
        System.out.println();
    }

}
