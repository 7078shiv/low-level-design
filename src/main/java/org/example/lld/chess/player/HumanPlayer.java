package org.example.lld.chess.player;

import org.example.lld.chess.Player;
import org.example.lld.chess.enums.PieceColour;

public class HumanPlayer extends Player {

    public HumanPlayer(PieceColour colour, String name) {
        super(colour, name);
    }

    @Override
    public boolean isBot() {
        return false;
    }
}
