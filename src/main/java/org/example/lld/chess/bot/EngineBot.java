package org.example.lld.chess.bot;

import org.example.lld.chess.Board;
import org.example.lld.chess.Cell;
import org.example.lld.chess.Game;
import org.example.lld.chess.MoveOption;
import org.example.lld.chess.Position;
import org.example.lld.chess.bot.engine.Search;
import org.example.lld.chess.bot.engine.SearchBoard;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;
import org.example.lld.chess.pieces.Piece;

import java.util.List;

/**
 * Translates the game position into the search representation, looks for the best move,
 * and translates the answer back.
 *
 * <p>The two representations are kept in step by the checks at the end: whatever the search
 * returns has to appear in the game's own list of legal moves before it is played.
 */
public class EngineBot implements BotStrategy {

    private final int maxDepth;
    private final long budgetMillis;
    private final Search search = new Search();

    public EngineBot(int maxDepth, long budgetMillis) {
        this.maxDepth = maxDepth;
        this.budgetMillis = budgetMillis;
    }

    @Override
    public MoveOption chooseMove(Game game) {
        List<MoveOption> legal = game.legalMoveOptions();
        if (legal.isEmpty()) return null;

        SearchBoard position = toSearchBoard(game);
        int move = search.findBestMove(position, maxDepth, budgetMillis);
        MoveOption chosen = move == 0 ? null : toMoveOption(move);

        // if the two move generators ever disagree, play something legal rather than fail
        return chosen != null && containsMove(legal, chosen) ? chosen : legal.get(0);
    }

    /** Depth the last search reached, handy for logging and tests. */
    public int lastDepthReached() {
        return search.getDepthReached();
    }

    private static boolean containsMove(List<MoveOption> legal, MoveOption chosen) {
        for (MoveOption option : legal) {
            if (option.from().equals(chosen.from()) && option.to().equals(chosen.to())) return true;
        }
        return false;
    }

    static SearchBoard toSearchBoard(Game game) {
        SearchBoard position = new SearchBoard();
        Board board = game.getBoard();

        for (Cell[] rank : board.getCells()) {
            for (Cell cell : rank) {
                Piece piece = cell.getPiece();
                if (piece == null) continue;
                int index = cell.getPosition().getRow() * 8 + cell.getPosition().getCol();
                position.put(index, SearchBoard.piece(colourCode(piece.getPieceColour()), typeCode(piece.getPieceType())));
            }
        }

        position.side = colourCode(game.getTurn());
        position.castling = castlingRights(board);
        Position enPassant = game.getEnPassantTarget();
        position.epSquare = enPassant == null ? -1 : enPassant.getRow() * 8 + enPassant.getCol();
        position.halfMoveClock = game.getHalfMoveClock();
        position.refreshKey();
        return position;
    }

    /** Reads the rights straight off the pieces: a king or rook that has moved loses them. */
    private static int castlingRights(Board board) {
        int rights = 0;
        rights |= rightFor(board, 0, 7, SearchBoard.WHITE_KING_SIDE, PieceColour.WHITE);
        rights |= rightFor(board, 0, 0, SearchBoard.WHITE_QUEEN_SIDE, PieceColour.WHITE);
        rights |= rightFor(board, 7, 7, SearchBoard.BLACK_KING_SIDE, PieceColour.BLACK);
        rights |= rightFor(board, 7, 0, SearchBoard.BLACK_QUEEN_SIDE, PieceColour.BLACK);
        return rights;
    }

    private static int rightFor(Board board, int row, int rookCol, int bit, PieceColour colour) {
        Piece king = board.getCell(row, 4).getPiece();
        Piece rook = board.getCell(row, rookCol).getPiece();
        boolean kingReady = king != null && king.getPieceType() == PieceType.KING
                && king.getPieceColour() == colour && !king.isHasMoved();
        boolean rookReady = rook != null && rook.getPieceType() == PieceType.ROOK
                && rook.getPieceColour() == colour && !rook.isHasMoved();
        return kingReady && rookReady ? bit : 0;
    }

    private static MoveOption toMoveOption(int move) {
        int from = SearchBoard.fromOf(move);
        int to = SearchBoard.toOf(move);
        int promotion = SearchBoard.promotionOf(move);
        return new MoveOption(
                new Position(from / 8, from % 8),
                new Position(to / 8, to % 8),
                promotion == 0 ? null : pieceType(promotion));
    }

    static int colourCode(PieceColour colour) {
        return colour == PieceColour.WHITE ? SearchBoard.WHITE : SearchBoard.BLACK;
    }

    static int typeCode(PieceType type) {
        return switch (type) {
            case PAWN -> SearchBoard.PAWN;
            case KNIGHT -> SearchBoard.KNIGHT;
            case BISHOP -> SearchBoard.BISHOP;
            case ROOK -> SearchBoard.ROOK;
            case QUEEN -> SearchBoard.QUEEN;
            case KING -> SearchBoard.KING;
        };
    }

    static PieceType pieceType(int code) {
        return switch (code) {
            case SearchBoard.KNIGHT -> PieceType.KNIGHT;
            case SearchBoard.BISHOP -> PieceType.BISHOP;
            case SearchBoard.ROOK -> PieceType.ROOK;
            case SearchBoard.QUEEN -> PieceType.QUEEN;
            case SearchBoard.KING -> PieceType.KING;
            default -> PieceType.PAWN;
        };
    }
}
