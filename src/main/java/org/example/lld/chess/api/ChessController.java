package org.example.lld.chess.api;

import org.example.lld.chess.Game;
import org.example.lld.chess.Position;
import org.example.lld.chess.api.dto.GameStateDto;
import org.example.lld.chess.api.dto.MoveRequest;
import org.example.lld.chess.api.dto.NewGameRequest;
import org.example.lld.chess.enums.PieceColour;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chess/games")
@CrossOrigin
public class ChessController {

    private final GameService gameService;

    public ChessController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateDto newGame(@RequestBody(required = false) NewGameRequest request) {
        return gameService.toDto(gameService.createGame(request));
    }

    /** Asks the bot to think and play its move. Only valid when it is the bot's turn. */
    @PostMapping("/{gameId}/bot-move")
    public GameStateDto botMove(@PathVariable String gameId) {
        gameService.playBotMove(gameId);
        return gameService.toDto(gameService.get(gameId));
    }

    @GetMapping("/{gameId}")
    public GameStateDto state(@PathVariable String gameId) {
        return gameService.toDto(gameService.get(gameId));
    }

    /** Legal destinations for a single square, for clients that do not want the whole map. */
    @GetMapping("/{gameId}/legal-moves")
    public Map<String, Object> legalMoves(@PathVariable String gameId, @RequestParam String from) {
        Game game = gameService.get(gameId);
        Position origin = Position.fromAlgebraic(from);
        if (origin == null) {
            throw new IllegalArgumentException("'" + from + "' is not a square on the board");
        }
        return Map.of("from", from,
                "to", game.getLegalDestinations(origin).stream().map(Position::toAlgebraic).toList());
    }

    @PostMapping("/{gameId}/moves")
    public GameStateDto move(@PathVariable String gameId, @RequestBody MoveRequest request) {
        gameService.move(gameId, request.from(), request.to(), request.promotion());
        return gameService.toDto(gameService.get(gameId));
    }

    @PostMapping("/{gameId}/undo")
    public GameStateDto undo(@PathVariable String gameId) {
        gameService.undo(gameId);
        return gameService.toDto(gameService.get(gameId));
    }

    @PostMapping("/{gameId}/resign")
    public GameStateDto resign(@PathVariable String gameId, @RequestParam PieceColour colour) {
        gameService.resign(gameId, colour);
        return gameService.toDto(gameService.get(gameId));
    }

    @PostMapping("/{gameId}/draw")
    public GameStateDto draw(@PathVariable String gameId) {
        gameService.draw(gameId);
        return gameService.toDto(gameService.get(gameId));
    }

    @DeleteMapping("/{gameId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String gameId) {
        gameService.delete(gameId);
    }

    // ------------------------------------------------------------------ errors

    @ExceptionHandler(GameService.GameNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(GameService.GameNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    /** Illegal moves are a normal part of play, so they answer 400 with a readable reason. */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}
