package JBTP.Model.pieces;

import JBTP.Model.*;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(int color, Position position) {
        super(color, position);
    }

    // Constructor that includes hasMoved state for when duplicating pieces
    protected King(int color, Position position, boolean hasMoved) {
        super(color, position, hasMoved);
    }

    @Override
    protected String generateImagePath() {
        return getColor() == PieceColor.WHITE ? "images/wk.png" : "images/bk.png";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();
        Position currentPos = getPosition();

        // Check all eight surrounding squares
        for (int colOffset = -1; colOffset <= 1; colOffset++) {
            for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                // Skip the current position
                if (colOffset == 0 && rowOffset == 0) {
                    continue;
                }

                Position targetPos = currentPos.offset(colOffset, rowOffset);

                // Check if the position is valid and either empty or contains an opponent's piece
                if (targetPos.isValid() && canMoveTo(board, targetPos)) {
                    Piece capturedPiece = board.getPiece(targetPos);

                    // Create a move
                    Move potentialMove = Move.createMove(currentPos, targetPos, this, capturedPiece);

                    // Check if this move would leave our king in check
                    if (!wouldMakeOwnKingVulnerable(board, potentialMove)) {
                        legalMoves.add(potentialMove);
                    }
                }
            }
        }

        // Add castling moves if applicable
        addCastlingMoves(board, legalMoves);

        return legalMoves;
    }

    private void addCastlingMoves(Board board, List<Move> legalMoves) {
        // King can castle only if it hasn't moved
        if (hasMoved()) {
            return;
        }

        // Check if the king is in check
        if (board.isKingInCheck(getColor())) {
            return;
        }

        Position kingPos = getPosition();
        int row = kingPos.getRow();

        // Kingside castling
        checkKingsideCastling(board, legalMoves, row);

        // Queenside castling
        checkQueensideCastling(board, legalMoves, row);
    }

    private void checkKingsideCastling(Board board, List<Move> legalMoves, int row) {
        // Check if kingside rook is in place and hasn't moved
        Position rookPos = new Position(7, row);
        Piece rook = board.getPiece(rookPos);

        if (rook != null && rook.getType().equals("Rook") &&
                rook.getColor() == getColor() && !rook.hasMoved()) {

            // Check if squares between king and rook are empty
            boolean pathClear = true;
            for (int col = 5; col <= 6; col++) {
                Position pos = new Position(col, row);
                if (board.getPiece(pos) != null) {
                    pathClear = false;
                    break;
                }

                // Also check if the king would pass through check
                if (isSquareUnderAttack(board, pos)) {
                    pathClear = false;
                    break;
                }
            }

            if (pathClear) {
                Position destination = new Position(6, row);
                legalMoves.add(Move.createCastling(getPosition(), destination, this));
            }
        }
    }

    private void checkQueensideCastling(Board board, List<Move> legalMoves, int row) {
        // Check if queenside rook is in place and hasn't moved
        Position rookPos = new Position(0, row);
        Piece rook = board.getPiece(rookPos);

        if (rook != null && rook.getType().equals("Rook") &&
                rook.getColor() == getColor() && !rook.hasMoved()) {

            // Check if squares between king and rook are empty
            boolean pathClear = true;
            for (int col = 1; col <= 3; col++) {
                Position pos = new Position(col, row);
                if (board.getPiece(pos) != null) {
                    pathClear = false;
                    break;
                }

                // The king only passes through squares 2 and 3
                if (col >= 2 && isSquareUnderAttack(board, pos)) {
                    pathClear = false;
                    break;
                }
            }

            if (pathClear) {
                Position destination = new Position(2, row);
                legalMoves.add(Move.createCastling(getPosition(), destination, this));
            }
        }
    }

    private boolean isSquareUnderAttack(Board board, Position position) {
        int opponentColor = (getColor() == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        List<Piece> opponentPieces = board.getPiecesByColor(opponentColor);

        for (Piece piece : opponentPieces) {
            List<Position> attackPositions = piece.getAttackPositions(board);
            if (attackPositions.contains(position)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        List<Position> attackPositions = new ArrayList<>();
        Position currentPos = getPosition();

        // Kings attack all eight surrounding squares
        for (int colOffset = -1; colOffset <= 1; colOffset++) {
            for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
                // Skip the current position
                if (colOffset == 0 && rowOffset == 0) {
                    continue;
                }

                Position targetPos = currentPos.offset(colOffset, rowOffset);

                if (targetPos.isValid()) {
                    attackPositions.add(targetPos);
                }
            }
        }

        return attackPositions;
    }

    @Override
    public String getType() {
        return "King";
    }

    @Override
    public Piece duplicate() {
        return new King(getColor(), getPosition(), hasMoved());
    }
}