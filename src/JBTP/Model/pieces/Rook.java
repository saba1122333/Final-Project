package JBTP.Model.pieces;

import Model.*;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    // Directions in which a rook can move: north, east, south, west
    private static final int[][] MOVEMENT_DIRECTIONS = {
            {0, -1}, // Up
            {1, 0},  // Right
            {0, 1},  // Down
            {-1, 0}  // Left
    };

    public Rook(int color, Position position) {
        super(color, position);
    }

    // Constructor for duplicating with movement history
    protected Rook(int color, Position position, boolean hasMoved) {
        super(color, position, hasMoved);
    }

    @Override
    protected String generateImagePath() {
        return getColor() == PieceColor.WHITE ? "assets/wrook.png" : "assets/brook.png";
    }

    @Override
    public String getType() {
        return "Rook";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        List<Move> validMoves = new ArrayList<>();
        Position startPos = getPosition();

        if (startPos == null) return validMoves;

        // Explore each cardinal direction
        for (int[] dir : MOVEMENT_DIRECTIONS) {
            explorePathway(board, startPos, dir[0], dir[1], validMoves);
        }

        return validMoves;
    }

    private void explorePathway(Board board, Position start, int colStep, int rowStep, List<Move> moves) {
        Position current = start;

        // Continue stepping in the given direction
        for (int step = 1; step < 8; step++) {
            Position next = current.offset(colStep, rowStep);

            // Stop if we've reached the edge of the board
            if (!next.isValid()) {
                break;
            }

            Piece pieceAtTarget = board.getPiece(next);

            if (pieceAtTarget == null) {
                // Empty square - can move here
                evaluateAndAddMove(board, start, next, null, moves);
            } else {
                // Found a piece
                if (pieceAtTarget.getColor() != getColor()) {
                    // Can capture opponent's piece
                    evaluateAndAddMove(board, start, next, pieceAtTarget, moves);
                }
                // Can't move past any piece, so stop looking in this direction
                break;
            }

            current = next;
        }
    }

    private void evaluateAndAddMove(Board board, Position from, Position to, Piece capturedPiece, List<Move> moveList) {
        Move candidate = Move.createMove(from, to, this, capturedPiece);

        // Only add if the move doesn't leave our king in check
        if (!wouldMakeOwnKingVulnerable(board, candidate)) {
            moveList.add(candidate);
        }
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        List<Position> threatenedSquares = new ArrayList<>();
        Position startPos = getPosition();

        if (startPos == null) return threatenedSquares;

        // Scan outward in each direction
        for (int[] dir : MOVEMENT_DIRECTIONS) {
            Position current = startPos;

            // Continue stepping in direction until hitting edge or piece
            for (int step = 1; step < 8; step++) {
                Position target = current.offset(dir[0], dir[1]);

                if (!target.isValid()) {
                    break; // Off the board
                }

                threatenedSquares.add(target);

                // Stop scanning beyond any piece
                if (board.getPiece(target) != null) {
                    break;
                }

                current = target;
            }
        }

        return threatenedSquares;
    }

    @Override
    public Piece duplicate() {
        return new Rook(
                getColor(),
                getPosition() != null ? new Position(getPosition().getColumn(), getPosition().getRow()) : null,
                hasMoved()
        );
    }

    // Method to help with castling logic
    public boolean canParticipateInCastling(Board board, King king) {
        // Both pieces must not have moved yet
        if (hasMoved() || king.hasMoved()) {
            return false;
        }

        // Must be on same row
        if (getPosition().getRow() != king.getPosition().getRow()) {
            return false;
        }

        // No pieces can be between king and rook
        if (board.isPieceBetween(getPosition(), king.getPosition())) {
            return false;
        }

        // King cannot be in check
        return !board.isKingInCheck(king.getColor());
    }

    @Override
    public String toString() {
        return getColor() == PieceColor.WHITE ? "♖" : "♜";
    }
}