package org.example.lld.tictactoe;

import lombok.Getter;

@Getter
public abstract class Player {
    @Getter
    String name;
    @Getter
    Symbol symbol;
    Player(String name, Symbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    abstract Move makeMove(Board board);
}
