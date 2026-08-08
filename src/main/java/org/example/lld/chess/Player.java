package org.example.lld.chess;

import lombok.Getter;
import lombok.Setter;
import org.example.lld.chess.enums.PieceColour;

/**
 * A side in the game. Subclasses decide where the moves come from:
 * {@link org.example.lld.chess.player.HumanPlayer} waits for the UI,
 * {@link org.example.lld.chess.player.BotPlayer} works one out itself.
 */
@Getter
public abstract class Player {

    private final PieceColour colour;
    @Setter
    private String name;

    protected Player(PieceColour colour, String name) {
        this.colour = colour;
        this.name = name;
    }

    public abstract boolean isBot();

    @Override
    public String toString() {
        return name + " (" + colour + ")";
    }
}
