package JBTP.Model.pieces;

import Model.*;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    // Standard constructor
    public Bishop(int color, Position position) {
        super(color, position);
    }

    // Constructor with hasMoved parameter for duplication
    protected Bishop(int color, Position position, boolean hasMoved) {
        super(color, position, hasMoved);
    }

    @Override
    protected String generateImagePath() {
        return getColor() == PieceColor.WHITE ? "images/wb.png" : "images/bb.png";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();
        Position currentPos = getPosition();

        // The four diagonal directions a bishop can move
        int[][] diagonalDirections = {
                {1, 1},   // Northeast
                {1, -1},  // Southeast
                {-1, -1}, // Southwest
                {-1, 1}   // Northwest
        };

        // Check each diagonal direction
        for (int[] direction : diagonalDirections) {
            exploreDiagonalPath(board, currentPos, direction[0], direction[1], legalMoves);
        }

        return legalMoves;
    }

    private void exploreDiagonalPath(Board board, Position startPos, int colDelta, int rowDelta, List<Move> moves) {
        // Start one step away from the current position
        Position currentPos = startPos.offset(colDelta, rowDelta);

        // Continue moving in this direction until we hit a piece or the edge of the board
        while (currentPos.isValid()) {
            Piece pieceAtPosition = board.getPiece(currentPos);

            if (pieceAtPosition == null) {
                // Empty square - this is a potential move
                addMoveIfLegal(board, startPos, currentPos, null, moves);
            } else {
                // Hit a piece
                if (pieceAtPosition.getColor() != getColor()) {
                    // Can capture opponent's piece
                    addMoveIfLegal(board, startPos, currentPos, pieceAtPosition, moves);
                }
                // Can't move past any piece, so stop exploring this direction
                break;
            }

            // Move to the next square in this direction
            currentPos = currentPos.offset(colDelta, rowDelta);
        }
    }

    private void addMoveIfLegal(Board board, Position from, Position to, Piece capturedPiece, List<Move> moves) {
        Move potentialMove = Move.createMove(from, to, this, capturedPiece);

        // Only add the move if it doesn't leave our king in check
        if (!wouldMakeOwnKingVulnerable(board, potentialMove)) {
            moves.add(potentialMove);
        }
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        List<Position> attackPositions = new ArrayList<>();
        Position currentPos = getPosition();

        // The same four diagonal directions
        int[][] diagonalDirections = {
                {1, 1},   // Northeast
                {1, -1},  // Southeast
                {-1, -1}, // Southwest
                {-1, 1}   // Northwest
        };

        // Check attack squares in each diagonal direction
        for (int[] direction : diagonalDirections) {
            collectAttackSquares(board, currentPos, direction[0], direction[1], attackPositions);
        }

        return attackPositions;
    }

    private void collectAttackSquares(Board board, Position startPos, int colDelta, int rowDelta, List<Position> attackPositions) {
        // Start one step away from the current position
        Position currentPos = startPos.offset(colDelta, rowDelta);

        // Continue in this direction until we hit a piece or the edge of the board
        while (currentPos.isValid()) {
            attackPositions.add(currentPos);

            // If we hit a piece, we can't attack past it
            if (board.getPiece(currentPos) != null) {
                break;
            }

            // Move to the next square in this direction
            currentPos = currentPos.offset(colDelta, rowDelta);
        }
    }

    @Override
    public String getType() {
        return "Bishop";
    }

    @Override
    public Piece duplicate() {
        return new Bishop(getColor(), getPosition(), hasMoved());
    }
}