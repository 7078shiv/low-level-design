package org.example.lld.chess.api.dto;

import org.example.lld.chess.bot.BotLevel;
import org.example.lld.chess.enums.GameStatus;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;

import java.util.List;
import java.util.Map;

/**
 * Everything the frontend needs to draw the position, in one payload.
 *
 * @param board        8x8, index 0 is rank 1, index 7 is rank 8; null means empty
 * @param legalMoves   origin square to the squares it may reach, for the side to move
 * @param botColour    null in a two player game, otherwise the side the bot plays
 * @param botToMove    true when the client should ask the server to play the bot's reply
 */
public record GameStateDto(String gameId,
                           SquareDto[][] board,
                           PieceColour turn,
                           GameStatus status,
                           String resultReason,
                           boolean inCheck,
                           boolean gameOver,
                           String whiteName,
                           String blackName,
                           PieceColour botColour,
                           BotLevel difficulty,
                           boolean botToMove,
                           Map<String, List<String>> legalMoves,
                           List<MoveDto> history,
                           MoveDto lastMove,
                           List<PieceType> capturedByWhite,
                           List<PieceType> capturedByBlack,
                           int halfMoveClock,
                           boolean canUndo) {
}
