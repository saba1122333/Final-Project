package JBTP.Model;

import java.util.List;

public abstract class Piece {
    private final int color;
    private Position position;
    private final String pieceImage;
    private boolean hasMoved; // Track if the piece has moved (useful for pawns, rooks, kings)

    public Piece(int color, Position position) {
        this.color = color;
        this.position = position;
        this.pieceImage = generateImagePath();
        this.hasMoved = false;
    }

    // Constructor that also includes the hasMoved state for copying pieces
    protected Piece(int color, Position position, boolean hasMoved) {
        this.color = color;
        this.position = position;
        this.pieceImage = generateImagePath();
        this.hasMoved = hasMoved;
    }

    protected String generateImagePath() {
        String colorPrefix = (color == PieceColor.WHITE) ? "white" : "black";
        String pieceType = getType().toLowerCase();
        return "resources/pieces/" + colorPrefix + "_" + pieceType + ".png";
    }

    public int getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getPieceImage() {
        return pieceImage;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    /**
     * Get all legal moves for this piece on the given board
     */
    public abstract List<Move> getLegalMoves(Board board);

    /**
     * Get all positions this piece can attack (useful for checking check)
     */
    public abstract List<Position> getAttackPositions(Board board);

    /**
     * Get the piece type name
     */
    public abstract String getType();

    public boolean moveTo(Position position) {
        Position oldPosition = this.position;
        this.position = position;
        this.hasMoved = true;
        return true;
    }

    /**
     * Check if the move would leave the king in check
     */
    protected boolean wouldMakeOwnKingVulnerable(Board board, Move move) {
        // Create a temporary board to simulate the move
        Board tempBoard = new Board(board);

        // Find the same piece on the copied board
        Piece tempPiece = tempBoard.getPiece(move.getOrigin());

        // Create a move for the temporary board
        Move tempMove = Move.createMove(
                move.getOrigin(),
                move.getDestination(),
                tempPiece,
                tempBoard.getPiece(move.getDestination())
        );

        // Execute the move on the temporary board
        tempBoard.executeMove(tempMove);

        // Check if the king is in check after the move
        return tempBoard.isKingInCheck(this.getColor());
    }

    /**
     * Create a deep copy of this piece
     */
    public abstract Piece duplicate();

    /**
     * Determine if this piece can move to a target position on the given board
     */
    protected boolean canMoveTo(Board board, Position target) {
        // Check if the target position is valid
        if (!target.isValid()) {
            return false;
        }

        // Check if the target is occupied by a piece of the same color
        Piece targetPiece = board.getPiece(target);
        return targetPiece == null || targetPiece.getColor() != this.color;
    }

    @Override
    public String toString() {
        return PieceColor.colorName(color) + " " + getType() + " at " + position;
    }
}