package JBTP.Controller;

import Model.*;
import Model.pieces.King;

import java.util.ArrayList;
import java.util.List;

public class CheckmateDetector {
    private Board board;
    private List<Position> availableMovePositions;

    /**
     * Creates a new CheckmateDetector that works with the provided board
     *
     * @param board The chess board to analyze
     */
    public CheckmateDetector(Board board) {
        this.board = board;
        this.availableMovePositions = new ArrayList<>();
    }

    /**
     * Determines if the black king is currently in check
     *
     * @return true if black is in check, false otherwise
     */
    public boolean isBlackInCheck() {
        return board.isKingInCheck(PieceColor.BLACK);
    }

    /**
     * Determines if the white king is currently in check
     *
     * @return true if white is in check, false otherwise
     */
    public boolean isWhiteInCheck() {
        return board.isKingInCheck(PieceColor.WHITE);
    }

    /**
     * Determines if the black player is in checkmate
     *
     * @return true if black is checkmated, false otherwise
     */
    public boolean isBlackCheckmated() {
        return board.isCheckmate(PieceColor.BLACK);
    }

    /**
     * Determines if the white player is in checkmate
     *
     * @return true if white is checkmated, false otherwise
     */
    public boolean isWhiteCheckmated() {
        return board.isCheckmate(PieceColor.WHITE);
    }

    /**
     * Gets a list of all positions that a player can move to on their turn
     *
     * @param isWhiteTurn indicates whose turn it is
     * @return a list of valid destination positions
     */
    public List<Position> getValidDestinations(boolean isWhiteTurn) {
        int activeColor = isWhiteTurn ? PieceColor.WHITE : PieceColor.BLACK;
        List<Move> legalMoves = board.getAllLegalMoves(activeColor);

        // Clear the previous list of positions
        availableMovePositions.clear();

        // Build the new list from legal moves
        for (Move move : legalMoves) {
            Position destination = move.getDestination();
            if (!availableMovePositions.contains(destination)) {
                availableMovePositions.add(destination);
            }
        }

        return availableMovePositions;
    }

    /**
     * Tests if a proposed move would be legal (not leaving the king in check)
     *
     * @param piece The piece to move
     * @param targetPosition The destination position
     * @return true if the move is legal, false if it would leave the king in check
     */
    public boolean isMoveLegal(Piece piece, Position targetPosition) {
        Position startPosition = piece.getPosition();
        Piece capturedPiece = board.getPiece(targetPosition);

        // Create the move
        Move proposedMove = Move.createMove(
                startPosition,
                targetPosition,
                piece,
                capturedPiece
        );

        // Create a copy of the board to test the move
        Board simulationBoard = new Board(board);

        // Execute the move on the copy
        simulationBoard.executeMove(proposedMove);

        // Check if the player's king would be in check after this move
        return !simulationBoard.isKingInCheck(piece.getColor());
    }

    /**
     * Updates the board reference when the game board changes
     *
     * @param board The new board to analyze
     */
    public void setBoard(Board board) {
        this.board = board;
    }

    /**
     * Checks if the king has any legal moves to escape check
     *
     * @param king The king to analyze
     * @return true if the king can move to a safe square
     */
    private boolean canKingEscape(King king) {
        List<Move> kingMoves = king.getLegalMoves(board);
        return !kingMoves.isEmpty();
    }

    /**
     * Checks if any friendly piece can capture a piece that's checking the king
     *
     * @param color The color of the side being checked
     * @return true if the attacking piece can be captured
     */
    private boolean canAttackerBeCaptured(int color) {
        King king = board.getKing(color);
        int opponentColor = (color == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        List<Piece> checkingPieces = findCheckingPieces(king, opponentColor);

        // Can only capture if there's exactly one piece giving check
        if (checkingPieces.size() != 1) {
            return false;
        }

        Piece attacker = checkingPieces.get(0);
        List<Piece> friendlyPieces = board.getPiecesByColor(color);

        for (Piece defender : friendlyPieces) {
            List<Move> possibleMoves = defender.getLegalMoves(board);
            for (Move move : possibleMoves) {
                if (move.getDestination().equals(attacker.getPosition())) {
                    // Make sure this capture doesn't leave king in check
                    Board tempBoard = new Board(board);
                    tempBoard.executeMove(move);

                    if (!tempBoard.isKingInCheck(color)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if the check can be blocked by interposing a piece
     *
     * @param color The color of the side being checked
     * @return true if a piece can block the check
     */
    private boolean canCheckBeBlocked(int color) {
        King king = board.getKing(color);
        int opponentColor = (color == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        List<Piece> checkingPieces = findCheckingPieces(king, opponentColor);

        // Can only block if there's exactly one piece giving check
        if (checkingPieces.size() != 1) {
            return false;
        }

        // Only sliding pieces (queen, rook, bishop) can be blocked
        Piece attacker = checkingPieces.get(0);
        String pieceType = attacker.getType();

        if (!pieceType.equals("Queen") && !pieceType.equals("Rook") && !pieceType.equals("Bishop")) {
            return false;
        }

        // Find squares between attacker and king
        List<Position> pathSquares = findSquaresBetween(attacker.getPosition(), king.getPosition());

        // Check if any friendly piece can move to block
        List<Piece> friendlyPieces = board.getPiecesByColor(color);

        for (Piece defender : friendlyPieces) {
            if (defender instanceof King) continue;  // King can't block check

            List<Move> possibleMoves = defender.getLegalMoves(board);
            for (Move move : possibleMoves) {
                if (pathSquares.contains(move.getDestination())) {
                    // Verify this block doesn't leave king in check
                    Board tempBoard = new Board(board);
                    tempBoard.executeMove(move);

                    if (!tempBoard.isKingInCheck(color)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Finds all pieces currently checking the king
     *
     * @param king The king being checked
     * @param opponentColor The color of potential attackers
     * @return List of pieces giving check to the king
     */
    private List<Piece> findCheckingPieces(King king, int opponentColor) {
        List<Piece> checkingPieces = new ArrayList<>();
        List<Piece> opponentPieces = board.getPiecesByColor(opponentColor);
        Position kingPosition = king.getPosition();

        for (Piece piece : opponentPieces) {
            List<Position> attackPositions = piece.getAttackPositions(board);
            if (attackPositions.contains(kingPosition)) {
                checkingPieces.add(piece);
            }
        }

        return checkingPieces;
    }

    /**
     * Finds all squares between two positions (for straight lines or diagonals)
     *
     * @param start Starting position
     * @param end Ending position
     * @return List of positions between start and end (exclusive)
     */
    private List<Position> findSquaresBetween(Position start, Position end) {
        List<Position> squares = new ArrayList<>();

        int startCol = start.getColumn();
        int startRow = start.getRow();
        int endCol = end.getColumn();
        int endRow = end.getRow();

        // Same row - horizontal
        if (startRow == endRow) {
            int minCol = Math.min(startCol, endCol);
            int maxCol = Math.max(startCol, endCol);

            for (int col = minCol + 1; col < maxCol; col++) {
                squares.add(new Position(col, startRow));
            }
        }
        // Same column - vertical
        else if (startCol == endCol) {
            int minRow = Math.min(startRow, endRow);
            int maxRow = Math.max(startRow, endRow);

            for (int row = minRow + 1; row < maxRow; row++) {
                squares.add(new Position(startCol, row));
            }
        }
        // Diagonal path
        else if (Math.abs(startCol - endCol) == Math.abs(startRow - endRow)) {
            int colDirection = (endCol > startCol) ? 1 : -1;
            int rowDirection = (endRow > startRow) ? 1 : -1;

            int col = startCol + colDirection;
            int row = startRow + rowDirection;

            while (col != endCol && row != endRow) {
                squares.add(new Position(col, row));
                col += colDirection;
                row += rowDirection;
            }
        }

        return squares;
    }
}