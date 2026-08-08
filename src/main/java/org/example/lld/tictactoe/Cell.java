package org.example.lld.tictactoe;

import lombok.Getter;
import lombok.Setter;

public class Cell {
    @Setter
    @Getter
    Symbol symbol = Symbol.empty;
    int row,col;

    Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    boolean isEmpty(){
        return this.symbol == Symbol.empty;
    }

}
