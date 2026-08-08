package org.example.lld.chess.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Liveness probe, kept off the {@code /games} path so it can never be mistaken
 * for a game id.
 */
@RestController
public class HealthController {

    private final GameService gameService;

    public HealthController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/api/chess/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "games", gameService.activeGames());
    }
}
