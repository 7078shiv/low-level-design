package org.example.lld.chess.api.dto;

import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;

/** One occupied square. Null entries in the board matrix mean an empty square. */
public record SquareDto(PieceType type, PieceColour colour, String symbol, String square) {
}
