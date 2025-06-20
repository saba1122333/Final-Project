package JBTP.Model.pieces;

import JBTP.Model.*;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    // Standard constructor
    public Knight(int color, Position position) {
        super(color, position);
    }

    // Constructor with hasMoved parameter for duplication
    protected Knight(int color, Position position, boolean hasMoved) {
        super(color, position, hasMoved);
    }

    @Override
    protected String generateImagePath() {
        return getColor() == PieceColor.WHITE ? "images/wn.png" : "images/bn.png";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        List<Move> legalMoves = new ArrayList<>();
        Position currentPos = getPosition();

        // All possible knight movement patterns (L-shapes)
        int[][] knightJumps = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        // Check each possible knight move
        for (int[] jump : knightJumps) {
            Position targetPos = currentPos.offset(jump[0], jump[1]);

            // Ensure the position is on the board and we can move there
            if (targetPos.isValid() && canMoveTo(board, targetPos)) {
                Piece capturedPiece = board.getPiece(targetPos);

                // Create a potential move
                Move potentialMove = Move.createMove(
                        currentPos,
                        targetPos,
                        this,
                        capturedPiece
                );

                // Only add the move if it doesn't leave our king in check
                if (!wouldMakeOwnKingVulnerable(board, potentialMove)) {
                    legalMoves.add(potentialMove);
                }
            }
        }

        return legalMoves;
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        List<Position> attackPositions = new ArrayList<>();
        Position currentPos = getPosition();

        // The same knight movement patterns
        int[][] knightJumps = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        // A knight attacks all squares it can move to
        for (int[] jump : knightJumps) {
            Position targetPos = currentPos.offset(jump[0], jump[1]);

            // Only include valid board positions
            if (targetPos.isValid()) {
                attackPositions.add(targetPos);
            }
        }

        return attackPositions;
    }

    @Override
    public String getType() {
        return "Knight";
    }

    @Override
    public Piece duplicate() {
        return new Knight(getColor(), getPosition(), hasMoved());
    }
}