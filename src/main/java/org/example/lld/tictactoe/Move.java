package org.example.lld.tictactoe;

import lombok.Getter;

public class Move {
    @Getter
    int row,col;
    @Getter
    Symbol symbol;
    public Move(int row, int col, Symbol symbol){
        this.row = row;
        this.col = col;
        this.symbol = symbol;
    }
}
