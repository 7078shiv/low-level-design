package org.example.lld.chess.api.dto;

import org.example.lld.chess.enums.PieceType;

/** {"from":"e2","to":"e4"} with an optional promotion piece. */
public record MoveRequest(String from, String to, PieceType promotion) {
}
