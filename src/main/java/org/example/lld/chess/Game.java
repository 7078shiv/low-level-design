package org.example.lld.chess;

import lombok.Getter;
import org.example.lld.chess.enums.GameStatus;
import org.example.lld.chess.enums.PieceColour;
import org.example.lld.chess.enums.PieceType;
import org.example.lld.chess.pieces.Pawn;
import org.example.lld.chess.pieces.Piece;
import org.example.lld.chess.pieces.PieceFactory;
import org.example.lld.chess.player.HumanPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates a single chess game: turn order, legality (a move may not leave the
 * own king in check), the special rules (castling, en passant, promotion) and the
 * end conditions (checkmate, stalemate, draws).
 *
 * <p>The pieces only know their movement geometry. Everything that needs knowledge of
 * the whole position or of the move history is decided here.
 */
@Getter
public class Game {

    private final String id;
    private final Player whitePlayer;
    private final Player blackPlayer;

    private Board board;
    private PieceColour turn;
    private GameStatus status;
    private String resultReason;

    private final List<Move> moveHistory = new ArrayList<>();
    /** Pieces taken by white, i.e. black pieces, and the other way round. */
    private final List<Piece> capturedByWhite = new ArrayList<>();
    private final List<Piece> capturedByBlack = new ArrayList<>();

    /** Square a pawn may capture onto by en passant, valid for one ply only. */
    private Position enPassantTarget;
    /** Plies since the last capture or pawn move; 100 means the fifty move rule applies. */
    private int halfMoveClock;

    private final List<String> positionHistory = new ArrayList<>();
    private final Deque<Snapshot> undoStack = new ArrayDeque<>();

    public Game() {
        this("White", "Black");
    }

    /** Two humans at the same board. */
    public Game(String whiteName, String blackName) {
        this(new HumanPlayer(PieceColour.WHITE, whiteName), new HumanPlayer(PieceColour.BLACK, blackName));
    }

    public Game(Player whitePlayer, Player blackPlayer) {
        this.id = UUID.randomUUID().toString();
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.board = new Board();
        this.turn = PieceColour.WHITE;
        this.status = GameStatus.IN_PROGRESS;
        this.halfMoveClock = 0;
        this.positionHistory.add(positionKey());
    }

    // ------------------------------------------------------------------ queries

    public Player playerOf(PieceColour colour) {
        return colour == PieceColour.WHITE ? whitePlayer : blackPlayer;
    }

    public static PieceColour opponentOf(PieceColour colour) {
        return colour == PieceColour.WHITE ? PieceColour.BLACK : PieceColour.WHITE;
    }

    public boolean isInCheck(PieceColour colour) {
        Cell king = board.findKing(colour);
        return king != null && isSquareAttacked(board, king.getPosition(), opponentOf(colour));
    }

    public boolean isOver() {
        return status != GameStatus.IN_PROGRESS;
    }

    /**
     * Is {@code target} attacked by any piece of {@code byColour}?
     * Pawns are special cased: they capture diagonally but move straight, so their
     * normal move validation would not report an attack on an empty square.
     */
    public static boolean isSquareAttacked(Board board, Position target, PieceColour byColour) {
        Cell targetCell = board.getCell(target);
        if (targetCell == null) return false;

        for (Cell cell : board.cellsWithPiecesOf(byColour)) {
            Piece piece = cell.getPiece();
            if (piece.getPieceType() == PieceType.PAWN) {
                int direction = Pawn.forwardDirection(byColour);
                int rowDelta = target.getRow() - cell.getPosition().getRow();
                int colDiff = Math.abs(target.getCol() - cell.getPosition().getCol());
                if (rowDelta == direction && colDiff == 1) return true;
            } else if (piece.isValidMove(board, cell, targetCell)) {
                return true;
            }
        }
        return false;
    }



    /** Every square the piece on {@code from} may legally move to right now. */
    public List<Position> getLegalDestinations(Position from) {
        List<Position> destinations = new ArrayList<>();
        for (Candidate candidate : legalMoves(from)) {
            destinations.add(candidate.to());
        }
        return destinations;
    }

    /** All legal moves for the side to move, keyed by origin square in algebraic notation. */
    public Map<String, List<String>> getAllLegalMoves() {
        Map<String, List<String>> byOrigin = new LinkedHashMap<>();
        if (isOver()) return byOrigin;

        for (Cell cell : board.cellsWithPiecesOf(turn)) {
            List<Position> destinations = getLegalDestinations(cell.getPosition());
            if (destinations.isEmpty()) continue;
            List<String> squares = new ArrayList<>();
            for (Position destination : destinations) {
                squares.add(destination.toAlgebraic());
            }
            byOrigin.put(cell.getPosition().toAlgebraic(), squares);
        }
        return byOrigin;
    }

    /** Every legal move for the side to move, as origin/destination pairs. Used by the bots. */
    public List<MoveOption> legalMoveOptions() {
        List<MoveOption> options = new ArrayList<>();
        if (isOver()) return options;

        for (Cell cell : board.cellsWithPiecesOf(turn)) {
            for (Candidate candidate : legalMoves(cell.getPosition())) {
                options.add(new MoveOption(candidate.from(), candidate.to()));
            }
        }
        return options;
    }

    /** True when the move would put a pawn on the last rank, so the UI must ask for a piece. */
    public boolean isPromotion(Position from, Position to) {
        Cell start = board.getCell(from);
        if (start == null || start.getPiece() == null) return false;
        Piece piece = start.getPiece();
        return piece.getPieceType() == PieceType.PAWN
                && to != null
                && to.getRow() == Pawn.promotionRow(piece.getPieceColour());
    }

    // ------------------------------------------------------------------ playing

    public Move makeMove(Position from, Position to) {
        return makeMove(from, to, PieceType.QUEEN);
    }

    public Move makeMove(Position from, Position to, PieceType promotionChoice) {
        if (isOver()) {
            throw new IllegalStateException("The game is already over: " + status);
        }
        Cell start = board.getCell(from);
        if (start == null) {
            throw new IllegalArgumentException("Square out of the board");
        }
        Piece piece = start.getPiece();
        if (piece == null) {
            throw new IllegalArgumentException("There is no piece on " + from.toAlgebraic());
        }
        if (piece.getPieceColour() != turn) {
            throw new IllegalArgumentException("It is " + turn + " to move");
        }

        Candidate candidate = null;
        for (Candidate option : legalMoves(from)) {
            if (option.to().equals(to)) {
                candidate = option;
                break;
            }
        }
        if (candidate == null) {
            throw new IllegalArgumentException(
                    piece.getPieceType() + " on " + from.toAlgebraic() + " cannot move to " + to.toAlgebraic());
        }

        undoStack.push(snapshot());

        Move move = new Move(playerOf(turn), from, to, piece.getPieceType());
        // Notation needs the position as it stands *before* the move.
        String notationCore = buildNotationCore(candidate, promotionChoice);

        boolean pawnMove = piece.getPieceType() == PieceType.PAWN;
        Applied applied = applyOn(board, candidate, promotionChoice);

        if (applied.captured() != null) {
            move.setPieceKilled(applied.captured().getPieceType());
            move.setCapturedAt(applied.capturedAt());
            if (turn == PieceColour.WHITE) {
                capturedByWhite.add(applied.captured());
            } else {
                capturedByBlack.add(applied.captured());
            }
        }
        move.setCastling(candidate.castling());
        move.setEnPassant(candidate.enPassant());
        move.setPromotedTo(applied.promotedTo());

        halfMoveClock = (pawnMove || applied.captured() != null) ? 0 : halfMoveClock + 1;
        enPassantTarget = null;
        if (pawnMove && Math.abs(to.getRow() - from.getRow()) == 2) {
            enPassantTarget = new Position((from.getRow() + to.getRow()) / 2, from.getCol());
        }

        turn = opponentOf(turn);
        positionHistory.add(positionKey());

        boolean opponentInCheck = isInCheck(turn);
        boolean opponentHasMoves = hasAnyLegalMove(turn);
        move.setCheck(opponentInCheck);
        move.setCheckmate(opponentInCheck && !opponentHasMoves);
        move.setNotation(notationCore + (move.isCheckmate() ? "#" : opponentInCheck ? "+" : ""));

        updateStatus(opponentInCheck, opponentHasMoves);
        moveHistory.add(move);
        return move;
    }

    /** Takes back the last move. Returns false when there is nothing to undo. */
    public boolean undo() {
        if (undoStack.isEmpty()) return false;
        restore(undoStack.pop());
        return true;
    }

    public void resign(PieceColour colour) {
        if (isOver()) return;
        status = colour == PieceColour.WHITE ? GameStatus.BLACK_WIN : GameStatus.WHITE_WIN;
        resultReason = playerOf(colour).getName() + " resigned";
    }

    public void agreeDraw() {
        if (isOver()) return;
        status = GameStatus.DRAW;
        resultReason = "Draw by agreement";
    }

    // ------------------------------------------------------- move generation

    private List<Candidate> legalMoves(Position from) {
        List<Candidate> legal = new ArrayList<>();
        if (isOver()) return legal;

        Cell start = board.getCell(from);
        if (start == null || start.getPiece() == null || start.getPiece().getPieceColour() != turn) {
            return legal;
        }
        for (Candidate candidate : pseudoLegalMoves(start)) {
            if (!leavesOwnKingInCheck(candidate)) {
                legal.add(candidate);
            }
        }
        return legal;
    }

    /** Moves that satisfy the piece geometry and the special rules, ignoring king safety. */
    private List<Candidate> pseudoLegalMoves(Cell start) {
        List<Candidate> candidates = new ArrayList<>();
        Piece piece = start.getPiece();

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Cell end = board.getCell(row, col);
                if (piece.isValidMove(board, start, end)) {
                    candidates.add(new Candidate(start.getPosition(), end.getPosition(), false, false));
                }
            }
        }

        if (piece.getPieceType() == PieceType.PAWN) {
            addEnPassant(start, candidates);
        } else if (piece.getPieceType() == PieceType.KING) {
            addCastling(start, candidates);
        }
        return candidates;
    }

    private void addEnPassant(Cell start, List<Candidate> candidates) {
        if (enPassantTarget == null) return;
        Piece pawn = start.getPiece();
        int direction = Pawn.forwardDirection(pawn.getPieceColour());
        if (enPassantTarget.getRow() - start.getPosition().getRow() != direction) return;
        if (Math.abs(enPassantTarget.getCol() - start.getPosition().getCol()) != 1) return;

        // The pawn that just double stepped sits next to us, not on the target square.
        Cell victim = board.getCell(new Position(enPassantTarget.getRow() - direction, enPassantTarget.getCol()));
        if (victim == null || victim.getPiece() == null) return;
        if (victim.getPiece().getPieceColour() == pawn.getPieceColour()) return;

        candidates.add(new Candidate(start.getPosition(), enPassantTarget, false, true));
    }

    private void addCastling(Cell kingCell, List<Candidate> candidates) {
        Piece king = kingCell.getPiece();
        if (king.isHasMoved()) return;

        PieceColour colour = king.getPieceColour();
        int row = colour == PieceColour.WHITE ? 0 : 7;
        if (kingCell.getPosition().getRow() != row || kingCell.getPosition().getCol() != 4) return;
        if (isInCheck(colour)) return;

        // king side: rook on h, king travels e -> g through f
        tryCastle(row, 7, new int[]{5, 6}, new int[]{5, 6}, 6, colour, candidates);
        // queen side: rook on a, b must be empty too but the king never visits it
        tryCastle(row, 0, new int[]{1, 2, 3}, new int[]{2, 3}, 2, colour, candidates);
    }

    private void tryCastle(int row, int rookCol, int[] emptyCols, int[] safeCols,
                           int kingDestCol, PieceColour colour, List<Candidate> candidates) {
        Cell rookCell = board.getCell(row, rookCol);
        Piece rook = rookCell.getPiece();
        if (rook == null || rook.getPieceType() != PieceType.ROOK
                || rook.getPieceColour() != colour || rook.isHasMoved()) {
            return;
        }
        for (int col : emptyCols) {
            if (board.getCell(row, col).getPiece() != null) return;
        }
        PieceColour opponent = opponentOf(colour);
        for (int col : safeCols) {
            if (isSquareAttacked(board, new Position(row, col), opponent)) return;
        }
        candidates.add(new Candidate(new Position(row, 4), new Position(row, kingDestCol), true, false));
    }

    private boolean leavesOwnKingInCheck(Candidate candidate) {
        PieceColour colour = board.getCell(candidate.from()).getPiece().getPieceColour();
        Board simulation = board.copy();
        applyOn(simulation, candidate, PieceType.QUEEN);
        Cell king = simulation.findKing(colour);
        return king != null && isSquareAttacked(simulation, king.getPosition(), opponentOf(colour));
    }

    private boolean hasAnyLegalMove(PieceColour colour) {
        PieceColour saved = turn;
        turn = colour;
        try {
            for (Cell cell : board.cellsWithPiecesOf(colour)) {
                if (!legalMoves(cell.getPosition()).isEmpty()) return true;
            }
            return false;
        } finally {
            turn = saved;
        }
    }

    // ------------------------------------------------------------- applying

    /** Mutates {@code target} with the move. Works on the live board and on simulations alike. */
    private Applied applyOn(Board target, Candidate candidate, PieceType promotionChoice) {
        Cell start = target.getCell(candidate.from());
        Cell end = target.getCell(candidate.to());
        Piece piece = start.getPiece();
        PieceColour colour = piece.getPieceColour();

        Piece captured = null;
        Position capturedAt = null;
        PieceType promotedTo = null;

        if (candidate.enPassant()) {
            Position victimSquare = new Position(
                    candidate.to().getRow() - Pawn.forwardDirection(colour), candidate.to().getCol());
            Cell victim = target.getCell(victimSquare);
            captured = victim.getPiece();
            capturedAt = victimSquare;
            victim.setPiece(null);
        } else if (end.getPiece() != null) {
            captured = end.getPiece();
            capturedAt = candidate.to();
        }

        start.setPiece(null);
        end.setPiece(piece);
        piece.setHasMoved(true);

        if (piece.getPieceType() == PieceType.PAWN && candidate.to().getRow() == Pawn.promotionRow(colour)) {
            promotedTo = sanitisePromotion(promotionChoice);
            Piece promoted = PieceFactory.create(promotedTo, colour);
            promoted.setHasMoved(true);
            end.setPiece(promoted);
        }

        if (candidate.castling()) {
            int row = candidate.from().getRow();
            boolean kingSide = candidate.to().getCol() == 6;
            Cell rookFrom = target.getCell(row, kingSide ? 7 : 0);
            Cell rookTo = target.getCell(row, kingSide ? 5 : 3);
            Piece rook = rookFrom.getPiece();
            rookFrom.setPiece(null);
            rookTo.setPiece(rook);
            if (rook != null) rook.setHasMoved(true);
        }

        if (captured != null) captured.setKilled(true);
        return new Applied(captured, capturedAt, promotedTo);
    }

    private PieceType sanitisePromotion(PieceType choice) {
        if (choice == PieceType.QUEEN || choice == PieceType.ROOK
                || choice == PieceType.BISHOP || choice == PieceType.KNIGHT) {
            return choice;
        }
        return PieceType.QUEEN;
    }

    // ------------------------------------------------------------- end of game

    private void updateStatus(boolean opponentInCheck, boolean opponentHasMoves) {
        if (!opponentHasMoves) {
            if (opponentInCheck) {
                status = turn == PieceColour.WHITE ? GameStatus.BLACK_WIN : GameStatus.WHITE_WIN;
                resultReason = "Checkmate";
            } else {
                status = GameStatus.DRAW;
                resultReason = "Stalemate";
            }
            return;
        }
        if (hasInsufficientMaterial()) {
            status = GameStatus.DRAW;
            resultReason = "Insufficient material";
        } else if (halfMoveClock >= 100) {
            status = GameStatus.DRAW;
            resultReason = "Fifty move rule";
        } else if (isThreefoldRepetition()) {
            status = GameStatus.DRAW;
            resultReason = "Threefold repetition";
        }
    }

    private boolean hasInsufficientMaterial() {
        List<Piece> minorPieces = new ArrayList<>();
        List<Position> bishopSquares = new ArrayList<>();

        for (Cell[] rank : board.getCells()) {
            for (Cell cell : rank) {
                Piece piece = cell.getPiece();
                if (piece == null || piece.getPieceType() == PieceType.KING) continue;
                if (piece.getPieceType() == PieceType.PAWN || piece.getPieceType() == PieceType.ROOK
                        || piece.getPieceType() == PieceType.QUEEN) {
                    return false;
                }
                minorPieces.add(piece);
                if (piece.getPieceType() == PieceType.BISHOP) bishopSquares.add(cell.getPosition());
            }
        }
        if (minorPieces.size() <= 1) return true;
        if (minorPieces.size() == 2 && bishopSquares.size() == 2
                && minorPieces.get(0).getPieceColour() != minorPieces.get(1).getPieceColour()) {
            // opposite coloured kings each with a bishop: a draw when both bishops share a square colour
            int firstSquareColour = (bishopSquares.get(0).getRow() + bishopSquares.get(0).getCol()) % 2;
            int secondSquareColour = (bishopSquares.get(1).getRow() + bishopSquares.get(1).getCol()) % 2;
            return firstSquareColour == secondSquareColour;
        }
        return false;
    }

    private boolean isThreefoldRepetition() {
        String current = positionHistory.get(positionHistory.size() - 1);
        int occurrences = 0;
        for (String key : positionHistory) {
            if (key.equals(current)) occurrences++;
        }
        return occurrences >= 3;
    }

    /** Layout plus side to move plus castling and en passant rights, as one comparable string. */
    private String positionKey() {
        StringBuilder key = new StringBuilder();
        for (Cell[] rank : board.getCells()) {
            for (Cell cell : rank) {
                Piece piece = cell.getPiece();
                key.append(piece == null ? "." : piece.getSymbol());
            }
        }
        key.append('|').append(turn);
        key.append('|').append(castlingRights());
        key.append('|').append(enPassantTarget == null ? "-" : enPassantTarget.toAlgebraic());
        return key.toString();
    }

    private String castlingRights() {
        StringBuilder rights = new StringBuilder();
        for (PieceColour colour : PieceColour.values()) {
            int row = colour == PieceColour.WHITE ? 0 : 7;
            Piece king = board.getCell(row, 4).getPiece();
            boolean kingReady = king != null && king.getPieceType() == PieceType.KING
                    && king.getPieceColour() == colour && !king.isHasMoved();
            for (int rookCol : new int[]{7, 0}) {
                Piece rook = board.getCell(row, rookCol).getPiece();
                boolean rookReady = rook != null && rook.getPieceType() == PieceType.ROOK
                        && rook.getPieceColour() == colour && !rook.isHasMoved();
                rights.append(kingReady && rookReady ? '1' : '0');
            }
        }
        return rights.toString();
    }

    // ------------------------------------------------------------- notation

    /** Standard algebraic notation without the trailing check or mate marker. */
    private String buildNotationCore(Candidate candidate, PieceType promotionChoice) {
        if (candidate.castling()) {
            return candidate.to().getCol() == 6 ? "O-O" : "O-O-O";
        }
        Piece piece = board.getCell(candidate.from()).getPiece();
        boolean capture = candidate.enPassant() || board.getCell(candidate.to()).getPiece() != null;
        String destination = candidate.to().toAlgebraic();

        if (piece.getPieceType() == PieceType.PAWN) {
            StringBuilder notation = new StringBuilder();
            if (capture) {
                notation.append(candidate.from().toAlgebraic().charAt(0)).append('x');
            }
            notation.append(destination);
            if (candidate.to().getRow() == Pawn.promotionRow(piece.getPieceColour())) {
                notation.append('=').append(PieceFactory
                        .create(sanitisePromotion(promotionChoice), PieceColour.WHITE).getSymbol());
            }
            return notation.toString();
        }

        return piece.getSymbol().toUpperCase()
                + disambiguation(piece, candidate)
                + (capture ? "x" : "")
                + destination;
    }

    /** Adds the file, rank, or both when another identical piece could reach the same square. */
    private String disambiguation(Piece piece, Candidate candidate) {
        List<Position> rivals = new ArrayList<>();
        for (Cell cell : board.cellsWithPiecesOf(piece.getPieceColour())) {
            if (cell.getPosition().equals(candidate.from())) continue;
            if (cell.getPiece().getPieceType() != piece.getPieceType()) continue;
            for (Candidate option : legalMoves(cell.getPosition())) {
                if (option.to().equals(candidate.to())) {
                    rivals.add(cell.getPosition());
                    break;
                }
            }
        }
        if (rivals.isEmpty()) return "";

        boolean sameFile = false;
        boolean sameRank = false;
        for (Position rival : rivals) {
            if (rival.getCol() == candidate.from().getCol()) sameFile = true;
            if (rival.getRow() == candidate.from().getRow()) sameRank = true;
        }
        String square = candidate.from().toAlgebraic();
        if (!sameFile) return square.substring(0, 1);
        if (!sameRank) return square.substring(1, 2);
        return square;
    }

    // ------------------------------------------------------------- undo state

    private Snapshot snapshot() {
        return new Snapshot(board.copy(), turn, status, resultReason, enPassantTarget, halfMoveClock,
                new ArrayList<>(moveHistory), new ArrayList<>(capturedByWhite),
                new ArrayList<>(capturedByBlack), new ArrayList<>(positionHistory));
    }

    private void restore(Snapshot snapshot) {
        board = snapshot.board();
        turn = snapshot.turn();
        status = snapshot.status();
        resultReason = snapshot.resultReason();
        enPassantTarget = snapshot.enPassantTarget();
        halfMoveClock = snapshot.halfMoveClock();
        moveHistory.clear();
        moveHistory.addAll(snapshot.moveHistory());
        capturedByWhite.clear();
        capturedByWhite.addAll(snapshot.capturedByWhite());
        capturedByBlack.clear();
        capturedByBlack.addAll(snapshot.capturedByBlack());
        positionHistory.clear();
        positionHistory.addAll(snapshot.positionHistory());
    }

    /** A move under consideration, before it is known to be legal. */
    private record Candidate(Position from, Position to, boolean castling, boolean enPassant) {
    }

    /** What actually happened on the board when a candidate was applied. */
    private record Applied(Piece captured, Position capturedAt, PieceType promotedTo) {
    }

    private record Snapshot(Board board, PieceColour turn, GameStatus status, String resultReason,
                            Position enPassantTarget, int halfMoveClock, List<Move> moveHistory,
                            List<Piece> capturedByWhite, List<Piece> capturedByBlack,
                            List<String> positionHistory) {
    }

    /** Convenience for tests and the console: makeMove("e2", "e4"). */
    public Move makeMove(String from, String to) {
        return makeMove(Position.fromAlgebraic(from), Position.fromAlgebraic(to), PieceType.QUEEN);
    }
}
