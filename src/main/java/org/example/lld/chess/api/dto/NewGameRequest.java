package org.example.lld.chess.api.dto;

import org.example.lld.chess.bot.BotLevel;
import org.example.lld.chess.enums.PieceColour;

/**
 * Leaving {@code botColour} null starts a game for two people at the same board.
 * Setting it makes that side a bot at the given {@code difficulty}.
 */
public record NewGameRequest(String whiteName,
                             String blackName,
                             PieceColour botColour,
                             BotLevel difficulty) {
}
