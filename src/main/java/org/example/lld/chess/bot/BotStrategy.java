package org.example.lld.chess.bot;

import org.example.lld.chess.Game;
import org.example.lld.chess.MoveOption;

/** How a bot picks its move. One implementation per playing strength. */
public interface BotStrategy {

    /** Returns null when the side to move has nothing to play. */
    MoveOption chooseMove(Game game);
}
