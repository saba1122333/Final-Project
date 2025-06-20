package JBTP.Model;

public class Move {
    private final Position origin;
    private final Position destination;
    private final Piece movingPiece;
    private final Piece takenPiece;
    private final boolean promotion;
    private final boolean castlingMove;
    private final boolean enPassantCapture;
    private final int moveFlags; // Bitfield for additional move data

    // Constants for move flags
    public static final int FLAG_CHECK = 1;
    public static final int FLAG_CHECKMATE = 2;
    public static final int FLAG_FIRST_MOVE = 4;

    public Move(Position origin, Position destination, Piece movingPiece) {
        this(origin, destination, movingPiece, null, false, false, false, 0);
    }

    public Move(Position origin, Position destination, Piece movingPiece,
                Piece takenPiece, boolean promotion, boolean castlingMove,
                boolean enPassantCapture, int moveFlags) {
        this.origin = origin;
        this.destination = destination;
        this.movingPiece = movingPiece;
        this.takenPiece = takenPiece;
        this.promotion = promotion;
        this.castlingMove = castlingMove;
        this.enPassantCapture = enPassantCapture;
        this.moveFlags = moveFlags;
    }

    // Factory method for creating simple moves
    public static Move createMove(Position origin, Position destination, Piece movingPiece, Piece takenPiece) {
        return new Move(origin, destination, movingPiece, takenPiece, false, false, false, 0);
    }

    // Factory method for creating promotion moves
    public static Move createPromotion(Position origin, Position destination, Piece movingPiece, Piece takenPiece) {
        return new Move(origin, destination, movingPiece, takenPiece, true, false, false, 0);
    }

    // Factory method for creating castling moves
    public static Move createCastling(Position origin, Position destination, Piece movingPiece) {
        return new Move(origin, destination, movingPiece, null, false, true, false, 0);
    }

    // Factory method for creating en passant captures
    public static Move createEnPassant(Position origin, Position destination, Piece movingPiece, Piece takenPiece) {
        return new Move(origin, destination, movingPiece, takenPiece, false, false, true, 0);
    }

    public Position getOrigin() {
        return origin;
    }

    public Position getDestination() {
        return destination;
    }

    public Piece getMovingPiece() {
        return movingPiece;
    }

    public Piece getTakenPiece() {
        return takenPiece;
    }

    public boolean isPromotion() {
        return promotion;
    }

    public boolean isCastlingMove() {
        return castlingMove;
    }

    public boolean isEnPassantCapture() {
        return enPassantCapture;
    }

    public boolean hasFlag(int flag) {
        return (moveFlags & flag) != 0;
    }

    public int getMoveFlags() {
        return moveFlags;
    }

    public Move withFlag(int flag) {
        return new Move(origin, destination, movingPiece, takenPiece,
                promotion, castlingMove, enPassantCapture, moveFlags | flag);
    }

    @Override
    public String toString() {
        String moveText = movingPiece.getType() + ": " + origin + " → " + destination;
        if (takenPiece != null) {
            moveText += " captures " + takenPiece.getType();
        }
        if (promotion) moveText += " (promotion)";
        if (castlingMove) moveText += " (castling)";
        if (enPassantCapture) moveText += " (en passant)";
        if (hasFlag(FLAG_CHECK)) moveText += " +";
        if (hasFlag(FLAG_CHECKMATE)) moveText += " #";
        return moveText;
    }
}