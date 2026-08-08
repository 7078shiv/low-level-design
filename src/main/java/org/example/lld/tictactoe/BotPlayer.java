package org.example.lld.tictactoe;

public class BotPlayer extends Player {
    BotPlayer(Symbol symbol) {
        super("Bot",symbol);
    }
    @Override
    Move makeMove(Board board) {
        for(int i = 0;i<board.size;i++){
            for(int j = 0;j<board.size;j++){
                if(board.isCellEmpty(i,j)){
                    return new Move(i,j,symbol);
                }
            }
        }
        return null;
    }
}
