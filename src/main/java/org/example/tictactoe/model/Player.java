package org.example.tictactoe.model;

import lombok.Data;

@Data
public class Player {
    public String name;
    public PlayingPeace playingPeace;
    public Player(String name, PlayingPeace playingPeace) {
        this.name = name;
        this.playingPeace = playingPeace;
    }
}
