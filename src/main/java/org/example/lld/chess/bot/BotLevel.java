package org.example.lld.chess.bot;

import lombok.Getter;

/**
 * Playing strength. All three run the same search; what changes is how far ahead it is
 * allowed to look and how long it may think.
 */
@Getter
public enum BotLevel {

    /** Two ply and a quick capture check: it will take a hanging piece and little else. */
    EASY("Bot (easy)", 2, 200),

    /** Enough depth to see short tactics coming. */
    MEDIUM("Bot (medium)", 5, 1_000),

    /** Thinks for a few seconds and usually reaches eight ply or more. */
    HARD("Bot (hard)", 64, 3_500);

    private final String label;
    private final int maxDepth;
    private final long budgetMillis;

    BotLevel(String label, int maxDepth, long budgetMillis) {
        this.label = label;
        this.maxDepth = maxDepth;
        this.budgetMillis = budgetMillis;
    }

    public BotStrategy createStrategy() {
        return new EngineBot(maxDepth, budgetMillis);
    }
}
