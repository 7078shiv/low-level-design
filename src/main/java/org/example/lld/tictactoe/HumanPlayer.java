package org.example.lld.tictactoe;
import java.util.Scanner;

public class HumanPlayer extends Player {
    Scanner sc = new Scanner(System.in);
    public HumanPlayer(String name, Symbol symbol) {
        super(name, symbol);
    }
    @Override
    Move makeMove(Board board) {
        System.out.println("Please enter your turn row:");
        int row = sc.nextInt();
        System.out.println("Please enter your turn column:");
        int column = sc.nextInt();
        return new Move(row,column,symbol);
    }
}
