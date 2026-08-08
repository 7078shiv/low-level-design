package org.example.lld.chess.api;

import org.example.lld.chess.Board;
import org.example.lld.chess.Cell;
import org.example.lld.chess.Game;
import org.example.lld.chess.Move;
import org.example.lld.chess.MoveOption;
import org.example.lld.chess.Player;
import org.example.lld.chess.Position;
import org.example.lld.chess.api.dto.GameStateDto;
import org.example.lld.chess.api.dto.MoveDto;
import org.example.lld.chess.api.dto.NewGameRequest;
import org.example.lld.chess.api.dto.SquareDto;
import org.example.lld.chess.bot.BotLevel;
import org.example.lld.chess.bot.BotStrategyFactory;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;
import org.example.lld.chess.pieces.Piece;
import org.example.lld.chess.player.BotPlayer;
import org.example.lld.chess.player.HumanPlayer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the running games in memory and adapts the domain model to the transport DTOs.
 * The engine itself stays free of any Spring or JSON concern.
 */
@Service
public class GameService {

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final BotStrategyFactory botStrategies;

    public GameService(@Value("${chess.bot.time-scale:1.0}") double botTimeScale) {
        this.botStrategies = new BotStrategyFactory(botTimeScale);
    }

    public Game createGame(NewGameRequest request) {
        NewGameRequest safe = request == null
                ? new NewGameRequest(null, null, null, null)
                : request;

        PieceColour botColour = safe.botColour();
        BotLevel difficulty = safe.difficulty() == null ? BotLevel.MEDIUM : safe.difficulty();

        Player white = playerFor(PieceColour.WHITE, safe.whiteName(), botColour, difficulty);
        Player black = playerFor(PieceColour.BLACK, safe.blackName(), botColour, difficulty);

        Game game = new Game(white, black);
        games.put(game.getId(), game);
        return game;
    }

    private Player playerFor(PieceColour colour, String name, PieceColour botColour, BotLevel difficulty) {
        if (colour == botColour) {
            return new BotPlayer(colour, difficulty, botStrategies.create(difficulty));
        }
        String fallback = colour == PieceColour.WHITE ? "White" : "Black";
        return new HumanPlayer(colour, name == null || name.isBlank() ? fallback : name);
    }

    public Game get(String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        return game;
    }

    public void delete(String gameId) {
        games.remove(gameId);
    }

    public int activeGames() {
        return games.size();
    }

    public Move move(String gameId, String from, String to, PieceType promotion) {
        Game game = get(gameId);
        Position origin = requireSquare(from);
        Position destination = requireSquare(to);
        synchronized (game) {
            return game.makeMove(origin, destination, promotion == null ? PieceType.QUEEN : promotion);
        }
    }

    /** Lets the bot work out and play its reply. Throws when it is not the bot's turn. */
    public Move playBotMove(String gameId) {
        Game game = get(gameId);
        synchronized (game) {
            if (game.isOver()) {
                throw new IllegalStateException("The game is already over");
            }
            Player toMove = game.playerOf(game.getTurn());
            if (!(toMove instanceof BotPlayer bot)) {
                throw new IllegalStateException("It is not the bot's turn");
            }
            MoveOption option = bot.chooseMove(game);
            if (option == null) {
                throw new IllegalStateException("The bot has no move to play");
            }
            return game.makeMove(option.from(), option.to(),
                    option.promotion() == null ? PieceType.QUEEN : option.promotion());
        }
    }

    /**
     * Takes back the last move. Against a bot that means two plies, so the player lands
     * back on their own turn rather than watching the bot move again straight away.
     */
    public void undo(String gameId) {
        Game game = get(gameId);
        synchronized (game) {
            if (!game.undo()) return;
            if (game.playerOf(game.getTurn()).isBot()) {
                game.undo();
            }
        }
    }

    public void resign(String gameId, PieceColour colour) {
        Game game = get(gameId);
        synchronized (game) {
            game.resign(colour);
        }
    }

    public void draw(String gameId) {
        Game game = get(gameId);
        synchronized (game) {
            game.agreeDraw();
        }
    }

    private Position requireSquare(String square) {
        Position position = Position.fromAlgebraic(square);
        if (position == null) {
            throw new IllegalArgumentException("'" + square + "' is not a square on the board");
        }
        return position;
    }

    // ------------------------------------------------------------------ mapping

    public GameStateDto toDto(Game game) {
        synchronized (game) {
            List<Move> history = game.getMoveHistory();
            Move last = history.isEmpty() ? null : history.get(history.size() - 1);

            BotPlayer bot = botOf(game);
            boolean botToMove = !game.isOver() && bot != null && bot.getColour() == game.getTurn();

            return new GameStateDto(
                    game.getId(),
                    toMatrix(game.getBoard()),
                    game.getTurn(),
                    game.getStatus(),
                    game.getResultReason(),
                    game.isInCheck(game.getTurn()),
                    game.isOver(),
                    game.getWhitePlayer().getName(),
                    game.getBlackPlayer().getName(),
                    bot == null ? null : bot.getColour(),
                    bot == null ? null : bot.getLevel(),
                    botToMove,
                    game.getAllLegalMoves(),
                    history.stream().map(GameService::toDto).toList(),
                    last == null ? null : toDto(last),
                    toTypes(game.getCapturedByWhite()),
                    toTypes(game.getCapturedByBlack()),
                    game.getHalfMoveClock(),
                    !history.isEmpty());
        }
    }

    private static BotPlayer botOf(Game game) {
        if (game.getWhitePlayer() instanceof BotPlayer white) return white;
        if (game.getBlackPlayer() instanceof BotPlayer black) return black;
        return null;
    }

    private static SquareDto[][] toMatrix(Board board) {
        SquareDto[][] matrix = new SquareDto[Board.SIZE][Board.SIZE];
        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Cell cell = board.getCell(row, col);
                Piece piece = cell.getPiece();
                matrix[row][col] = piece == null ? null : new SquareDto(
                        piece.getPieceType(),
                        piece.getPieceColour(),
                        piece.getSymbol(),
                        cell.getPosition().toAlgebraic());
            }
        }
        return matrix;
    }

    private static List<PieceType> toTypes(List<Piece> pieces) {
        List<PieceType> types = new ArrayList<>(pieces.size());
        for (Piece piece : pieces) {
            types.add(piece.getPieceType());
        }
        return types;
    }

    private static MoveDto toDto(Move move) {
        return new MoveDto(
                move.getFrom().toAlgebraic(),
                move.getTo().toAlgebraic(),
                move.getColour(),
                move.getPieceMoved(),
                move.getPieceKilled(),
                move.getPromotedTo(),
                move.isCastling(),
                move.isEnPassant(),
                move.isCheck(),
                move.isCheckmate(),
                move.getNotation());
    }

    /** Thrown when a game id is unknown, mapped to a 404 by the controller advice. */
    public static class GameNotFoundException extends RuntimeException {
        public GameNotFoundException(String gameId) {
            super("No game with id " + gameId);
        }
    }
}
