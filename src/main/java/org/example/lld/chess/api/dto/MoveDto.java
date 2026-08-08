package org.example.lld.chess.api.dto;

import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;

public record MoveDto(String from,
                      String to,
                      PieceColour colour,
                      PieceType piece,
                      PieceType captured,
                      PieceType promotedTo,
                      boolean castling,
                      boolean enPassant,
                      boolean check,
                      boolean checkmate,
                      String notation) {
}
