package org.example.lld.tictactoe;

import java.util.Deque;
import java.util.LinkedList;

public class Game {
    Board board;
    GameStatus gameStatus = GameStatus.IN_PROGRESS;
    Deque<Player> players = new LinkedList<>();
    Player winner;

    public Game(Board board,Deque<Player> players) {
        this.board = board;
        this.players = players;
    }

    public void play(){
        while (gameStatus == GameStatus.IN_PROGRESS){
            board.print();
            Player currentPlayer = players.pollFirst();
            Move move = currentPlayer.makeMove(board);

            if(!board.isValidMove(move)){
                System.out.println("Invalid move move again");
                players.addFirst(currentPlayer);
                continue;
            }
            Boolean response = board.applyMove(move);

            // winner

            if(response){
                gameStatus = GameStatus.WON;
                winner = currentPlayer;
                break;
            }

            if(board.isFull()){
                gameStatus = GameStatus.DRAW;
                break;
            }
            players.addLast(currentPlayer);
        }
        board.print();
        System.out.println(gameStatus == GameStatus.WON
                ? winner.getName() + " wins!" : "It's a draw!");

    }
}
