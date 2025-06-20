package JBTP.Model.pieces;

import JBTP.Model.*;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(int color, Position position) {
        super(color, position);
    }

    // Copy constructor with hasMoved state
    protected Pawn(int color, Position position, boolean hasMoved) {
        super(color, position, hasMoved);
    }

    @Override
    protected String generateImagePath() {
        return getColor() == PieceColor.WHITE ? "assets/wpawn.png" : "assets/bpawn.png";
    }

    @Override
    public String getType() {
        return "Pawn";
    }

    @Override
    public List<Move> getLegalMoves(Board board) {
        List<Move> possibleMoves = new ArrayList<>();
        Position pos = getPosition();

        if (pos == null) return possibleMoves;

        int column = pos.getColumn();
        int row = pos.getRow();

        // Direction is based on piece color (white moves up, black moves down)
        int forwardDirection = (getColor() == PieceColor.WHITE) ? -1 : 1;

        // Try moving forward one square
        Position forwardOne = pos.offset(0, forwardDirection);
        if (forwardOne.isValid() && board.getPiece(forwardOne) == null) {
            tryAddMove(board, pos, forwardOne, null, possibleMoves);

            // If pawn hasn't moved yet, it can move two squares forward
            if (!hasMoved()) {
                Position forwardTwo = pos.offset(0, 2 * forwardDirection);
                if (forwardTwo.isValid() &&
                        board.getPiece(forwardTwo) == null &&
                        !board.isPieceBetween(pos, forwardTwo)) {
                    tryAddMove(board, pos, forwardTwo, null, possibleMoves);
                }
            }
        }

        // Capture diagonally
        tryDiagonalCapture(board, pos.offset(-1, forwardDirection), possibleMoves);
        tryDiagonalCapture(board, pos.offset(1, forwardDirection), possibleMoves);

        // En Passant capture
        tryEnPassantCapture(board, possibleMoves);

        return possibleMoves;
    }

    private void tryDiagonalCapture(Board board, Position capturePos, List<Move> moves) {
        if (capturePos.isValid()) {
            Piece targetPiece = board.getPiece(capturePos);
            if (targetPiece != null && targetPiece.getColor() != getColor()) {
                tryAddMove(board, getPosition(), capturePos, targetPiece, moves);
            }
        }
    }

    private void tryAddMove(Board board, Position from, Position to, Piece capturedPiece, List<Move> moves) {
        boolean isPromotion = isPromotionRank(to.getRow());

        Move move = isPromotion ?
                Move.createPromotion(from, to, this, capturedPiece) :
                Move.createMove(from, to, this, capturedPiece);

        // Check if this move would leave our king in check
        if (!wouldMakeOwnKingVulnerable(board, move)) {
            moves.add(move);
        }
    }

    private boolean isPromotionRank(int row) {
        return (getColor() == PieceColor.WHITE && row == 0) ||
                (getColor() == PieceColor.BLACK && row == 7);
    }

    private void tryEnPassantCapture(Board board, List<Move> moves) {
        Move lastMove = board.getLastMove();
        if (lastMove == null) {
            return;
        }

        // Check if last move was a two-square pawn advance
        Piece lastMovedPiece = lastMove.getMovingPiece();
        if (!(lastMovedPiece instanceof Pawn)) {
            return;
        }

        Position start = lastMove.getOrigin();
        Position end = lastMove.getDestination();
        if (Math.abs(start.getRow() - end.getRow()) != 2) {
            return;
        }

        Position myPosition = getPosition();
        // Check if our pawn is adjacent to the opponent's pawn
        if (myPosition.getRow() == end.getRow() &&
                Math.abs(myPosition.getColumn() - end.getColumn()) == 1) {

            // Determine where our pawn would move to
            int captureRow = myPosition.getRow() + (getColor() == PieceColor.WHITE ? -1 : 1);
            Position capturePosition = new Position(end.getColumn(), captureRow);

            // Create an en passant move
            Move enPassantMove = Move.createEnPassant(
                    myPosition,
                    capturePosition,
                    this,
                    lastMovedPiece
            );

            // Verify this move doesn't leave king in check
            if (!wouldMakeOwnKingVulnerable(board, enPassantMove)) {
                moves.add(enPassantMove);
            }
        }
    }

    @Override
    public List<Position> getAttackPositions(Board board) {
        List<Position> attackPositions = new ArrayList<>();
        Position pos = getPosition();

        if (pos == null) return attackPositions;

        int forwardDirection = (getColor() == PieceColor.WHITE) ? -1 : 1;

        // Pawns attack diagonally
        Position leftAttack = pos.offset(-1, forwardDirection);
        if (leftAttack.isValid()) {
            attackPositions.add(leftAttack);
        }

        Position rightAttack = pos.offset(1, forwardDirection);
        if (rightAttack.isValid()) {
            attackPositions.add(rightAttack);
        }

        return attackPositions;
    }

    @Override
    public Piece duplicate() {
        return new Pawn(getColor(),
                getPosition() != null ? new Position(getPosition().getColumn(), getPosition().getRow()) : null,
                hasMoved());
    }

    @Override
    public String toString() {
        return (getColor() == PieceColor.WHITE) ? "♙" : "♟";
    }
}