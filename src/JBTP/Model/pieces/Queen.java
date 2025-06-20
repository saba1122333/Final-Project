package JBTP.Model.pieces;

import JBTP.Model.*;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {
    // Queen moves in 8 directions: horizontal, vertical, and diagonal
    private static final int[][] MOVEMENT_VECTORS = {
            {0, -1},  // North
            {1, -1},  // Northeast
            {1, 0},   // East
            {1, 1},   // Southeast
            {0, 1},   // South
            {-1, 1},  // Southwest
            {-1, 0},  // West
            {-1, -1}  // Northwest
    };

    public Queen(int color, Position position) {
        super(color, position);
    }

    // Constructor for duplicating
    protected Queen(int color, Position position, boolean hasMoved) {
        super(color, position, hasMoved);
    }

    @Override
    protected String generateImagePath() {
        return getColor() == PieceColor.WHITE ? "assets/wqueen.png" : "assets/bqueen.png";
    }

    @Override
    public String getType() {
        return "Queen";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        List<Move> validMoves = new ArrayList<>();
        Position start = getPosition();

        if (start == null) return validMoves;

        // Explore all eight directions
        for (int[] direction : MOVEMENT_VECTORS) {
            traceMovementPath(board, start, direction[0], direction[1], validMoves);
        }

        return validMoves;
    }

    private void traceMovementPath(Board board, Position start, int colStep, int rowStep, List<Move> moves) {
        Position current = start;

        // Continue moving in this direction until hitting a piece or board edge
        for (int distance = 1; distance < 8; distance++) {
            Position target = current.offset(colStep, rowStep);

            if (!target.isValid()) {
                break; // Reached edge of board
            }

            Piece targetPiece = board.getPiece(target);

            if (targetPiece == null) {
                // Empty square - can move here
                checkAndAddMove(board, start, target, null, moves);
            } else {
                // Found a piece
                if (targetPiece.getColor() != getColor()) {
                    // Can capture opponent's piece
                    checkAndAddMove(board, start, target, targetPiece, moves);
                }
                // Can't move past any piece
                break;
            }

            current = target;
        }
    }

    private void checkAndAddMove(Board board, Position from, Position to, Piece capturedPiece, List<Move> moveList) {
        Move candidate = Move.createMove(from, to, this, capturedPiece);

        // Only add if the move doesn't leave our king in check
        if (!wouldMakeOwnKingVulnerable(board, candidate)) {
            moveList.add(candidate);
        }
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        List<Position> attackedSquares = new ArrayList<>();
        Position start = getPosition();

        if (start == null) return attackedSquares;

        // Check all eight directions
        for (int[] direction : MOVEMENT_VECTORS) {
            Position current = start;

            // Move outward until hitting edge or piece
            for (int distance = 1; distance < 8; distance++) {
                Position target = current.offset(direction[0], direction[1]);

                if (!target.isValid()) {
                    break; // Off the board
                }

                attackedSquares.add(target);

                // Can't attack beyond any piece
                if (board.getPiece(target) != null) {
                    break;
                }

                current = target;
            }
        }

        return attackedSquares;
    }

    @Override
    public Piece duplicate() {
        return new Queen(
                getColor(),
                getPosition() != null ? new Position(getPosition().getColumn(), getPosition().getRow()) : null,
                hasMoved()
        );
    }

    @Override
    public String toString() {
        return getColor() == PieceColor.WHITE ? "♕" : "♛";
    }
}