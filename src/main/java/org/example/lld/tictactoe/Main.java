package org.example.lld.tictactoe;

import java.util.ArrayDeque;
import java.util.Deque;

public class Main {

    //
    // Clarify Requirement Questions
    // 1) Board size is fixed or extensible ---> extensible
    // 2) No of player is always 2, or it can be more  -->  it can be more
    // 3) Symbols -->  O and X
    // 4) Human vs Human or Bot too  -->  Design so Bot player can plug in
    // 5) Do i need move history / undo -->  optional
    // 6) win condition -->  full -> row,col,diagonal

    // Entities (noun)
    // Board,player,Symbol,Cell,Game,GameStatus
    public static void main(String[] args) {

        Board board = new Board(5);
        HumanPlayer player = new HumanPlayer("Shivang", Symbol.X);
        BotPlayer botPlayer = new BotPlayer(Symbol.O);
        Deque<Player> players = new ArrayDeque<>();
        players.add(player);
        players.add(botPlayer);
        Game game = new Game(board, players);
        game.play();
    }
}
