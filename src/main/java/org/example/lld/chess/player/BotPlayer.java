package org.example.lld.chess.player;

import lombok.Getter;
import org.example.lld.chess.Game;
import org.example.lld.chess.MoveOption;
import org.example.lld.chess.Player;
import org.example.lld.chess.bot.BotLevel;
import org.example.lld.chess.bot.BotStrategy;
import org.example.lld.chess.enums.PieceColour;

@Getter
public class BotPlayer extends Player {

    private final BotLevel level;
    private final BotStrategy strategy;

    public BotPlayer(PieceColour colour, BotLevel level) {
        this(colour, level, level.createStrategy());
    }

    /** Lets a deployment supply a search tuned to the hardware it runs on. */
    public BotPlayer(PieceColour colour, BotLevel level, BotStrategy strategy) {
        super(colour, level.getLabel());
        this.level = level;
        this.strategy = strategy;
    }

    @Override
    public boolean isBot() {
        return true;
    }

    /** Null when there is nothing to play, which only happens once the game is over. */
    public MoveOption chooseMove(Game game) {
        return strategy.chooseMove(game);
    }
}
