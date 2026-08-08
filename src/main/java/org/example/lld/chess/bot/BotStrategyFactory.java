package org.example.lld.chess.bot;

/**
 * Builds the search for a difficulty, scaling how long it may think.
 *
 * <p>The budgets on {@link BotLevel} assume a normal desktop core. On a small cloud
 * instance the same seconds buy far fewer nodes, so {@code timeScale} lets a deployment
 * hand the bot more wall clock to reach a comparable depth.
 */
public class BotStrategyFactory {

    private final double timeScale;

    public BotStrategyFactory(double timeScale) {
        this.timeScale = timeScale <= 0 ? 1.0 : timeScale;
    }

    public BotStrategy create(BotLevel level) {
        return new EngineBot(level.getMaxDepth(), Math.round(level.getBudgetMillis() * timeScale));
    }
}
