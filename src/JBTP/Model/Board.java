package JBTP.Model;

import Model.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] boardArray;
    private List<Piece> lightPieces;
    private List<Piece> darkPieces;
    private List<Move> moveSequence;
    private King lightKing;
    private King darkKing;

    public Board() {
        boardArray = new Piece[8][8];
        lightPieces = new ArrayList<>();
        darkPieces = new ArrayList<>();
        moveSequence = new ArrayList<>();
        setupInitialPosition();
    }

    // Deep copy constructor
    public Board(Board original) {
        this.boardArray = new Piece[8][8];
        this.lightPieces = new ArrayList<>();
        this.darkPieces = new ArrayList<>();
        this.moveSequence = new ArrayList<>(original.moveSequence);

        // Copy the light pieces
        for (Piece lightPiece : original.lightPieces) {
            Piece copiedPiece = lightPiece.duplicate();
            lightPieces.add(copiedPiece);
            Position pos = copiedPiece.getPosition();
            if (pos != null) {
                boardArray[pos.getRow()][pos.getColumn()] = copiedPiece;
            }

            // Track king reference
            if (copiedPiece instanceof King) {
                this.lightKing = (King) copiedPiece;
            }
        }

        // Copy the dark pieces
        for (Piece darkPiece : original.darkPieces) {
            Piece copiedPiece = darkPiece.duplicate();
            darkPieces.add(copiedPiece);
            Position pos = copiedPiece.getPosition();
            if (pos != null) {
                boardArray[pos.getRow()][pos.getColumn()] = copiedPiece;
            }

            // Track king reference
            if (copiedPiece instanceof King) {
                this.darkKing = (King) copiedPiece;
            }
        }
    }

    private void setupInitialPosition() {
        // Setup pawns
        for (int col = 0; col < 8; col++) {
            addPieceToBoard(new Pawn(PieceColor.BLACK, new Position(col, 1)));
            addPieceToBoard(new Pawn(PieceColor.WHITE, new Position(col, 6)));
        }

        // Setup rooks
        addPieceToBoard(new Rook(PieceColor.BLACK, new Position(0, 0)));
        addPieceToBoard(new Rook(PieceColor.BLACK, new Position(7, 0)));
        addPieceToBoard(new Rook(PieceColor.WHITE, new Position(0, 7)));
        addPieceToBoard(new Rook(PieceColor.WHITE, new Position(7, 7)));

        // Setup knights
        addPieceToBoard(new Knight(PieceColor.BLACK, new Position(1, 0)));
        addPieceToBoard(new Knight(PieceColor.BLACK, new Position(6, 0)));
        addPieceToBoard(new Knight(PieceColor.WHITE, new Position(1, 7)));
        addPieceToBoard(new Knight(PieceColor.WHITE, new Position(6, 7)));

        // Setup bishops
        addPieceToBoard(new Bishop(PieceColor.BLACK, new Position(2, 0)));
        addPieceToBoard(new Bishop(PieceColor.BLACK, new Position(5, 0)));
        addPieceToBoard(new Bishop(PieceColor.WHITE, new Position(2, 7)));
        addPieceToBoard(new Bishop(PieceColor.WHITE, new Position(5, 7)));

        // Setup queens
        addPieceToBoard(new Queen(PieceColor.BLACK, new Position(3, 0)));
        addPieceToBoard(new Queen(PieceColor.WHITE, new Position(3, 7)));

        // Setup kings
        darkKing = new King(PieceColor.BLACK, new Position(4, 0));
        lightKing = new King(PieceColor.WHITE, new Position(4, 7));
        addPieceToBoard(darkKing);
        addPieceToBoard(lightKing);
    }

    private void addPieceToBoard(Piece piece) {
        Position pos = piece.getPosition();
        boardArray[pos.getRow()][pos.getColumn()] = piece;

        if (piece.getColor() == PieceColor.WHITE) {
            lightPieces.add(piece);
        } else {
            darkPieces.add(piece);
        }
    }

    public Piece getPiece(Position position) {
        if (!isPositionInBounds(position)) {
            return null;
        }
        return boardArray[position.getRow()][position.getColumn()];
    }

    public boolean isPositionInBounds(Position position) {
        int col = position.getColumn();
        int row = position.getRow();
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    public List<Piece> getPiecesByColor(int color) {
        return color == PieceColor.WHITE ? new ArrayList<>(lightPieces) : new ArrayList<>(darkPieces);
    }

    public King getKing(int color) {
        return color == PieceColor.WHITE ? lightKing : darkKing;
    }

    public boolean executeMove(Move move) {
        Position from = move.getOrigin();
        Position to = move.getDestination();
        Piece piece = move.getMovingPiece();

        // Handle captured piece
        if (move.getTakenPiece() != null) {
            Piece capturedPiece = move.getTakenPiece();
            if (capturedPiece.getColor() == PieceColor.WHITE) {
                lightPieces.remove(capturedPiece);
            } else {
                darkPieces.remove(capturedPiece);
            }
        }

        // Special handling for en passant
        if (move.isEnPassantCapture()) {
            Position capturedPawnPos = new Position(to.getColumn(), from.getRow());
            Piece capturedPawn = getPiece(capturedPawnPos);

            if (capturedPawn != null) {
                if (capturedPawn.getColor() == PieceColor.WHITE) {
                    lightPieces.remove(capturedPawn);
                } else {
                    darkPieces.remove(capturedPawn);
                }
                boardArray[capturedPawnPos.getRow()][capturedPawnPos.getColumn()] = null;
            }
        }

        // Special handling for castling moves
        if (move.isCastlingMove()) {
            int row = from.getRow();
            // Determine if it's kingside or queenside castling
            if (to.getColumn() > from.getColumn()) {  // Kingside
                // Move the rook
                Piece rook = getPiece(new Position(7, row));
                Position rookNewPos = new Position(5, row);
                boardArray[row][7] = null;  // Remove rook from old position
                boardArray[row][5] = rook;  // Place rook in new position
                rook.setPosition(rookNewPos);
                rook.setHasMoved(true);
            } else {  // Queenside
                // Move the rook
                Piece rook = getPiece(new Position(0, row));
                Position rookNewPos = new Position(3, row);
                boardArray[row][0] = null;  // Remove rook from old position
                boardArray[row][3] = rook;  // Place rook in new position
                rook.setPosition(rookNewPos);
                rook.setHasMoved(true);
            }
        }

        // Update the board array
        boardArray[from.getRow()][from.getColumn()] = null;
        boardArray[to.getRow()][to.getColumn()] = piece;

        // Update the piece's position and move status
        piece.setPosition(to);
        piece.setHasMoved(true);

        // Handle pawn promotion
        if (move.isPromotion()) {
            // Replace pawn with a queen at the destination
            Queen queen = new Queen(piece.getColor(), to);

            // Remove the pawn
            if (piece.getColor() == PieceColor.WHITE) {
                lightPieces.remove(piece);
                lightPieces.add(queen);
            } else {
                darkPieces.remove(piece);
                darkPieces.add(queen);
            }

            // Update the board array
            boardArray[to.getRow()][to.getColumn()] = queen;
        }

        // Add to move history
        moveSequence.add(move);
        return true;
    }

    public List<Move> getAllLegalMoves(int color) {
        List<Move> legalMoves = new ArrayList<>();
        List<Piece> pieces = color == PieceColor.WHITE ? lightPieces : darkPieces;

        for (Piece piece : pieces) {
            legalMoves.addAll(piece.getLegalMoves(this));
        }

        return legalMoves;
    }

    public boolean isKingInCheck(int color) {
        King king = color == PieceColor.WHITE ? lightKing : darkKing;
        Position kingPosition = king.getPosition();
        List<Piece> opponentPieces = color == PieceColor.WHITE ? darkPieces : lightPieces;

        for (Piece piece : opponentPieces) {
            List<Position> attackPositions = piece.getAttackPositions(this);
            if (attackPositions.contains(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCheckmate(int color) {
        if (!isKingInCheck(color)) {
            return false;
        }

        List<Move> legalMoves = getAllLegalMoves(color);
        return legalMoves.isEmpty();
    }

    public boolean isStalemate(int color) {
        if (isKingInCheck(color)) {
            return false;
        }

        List<Move> legalMoves = getAllLegalMoves(color);
        return legalMoves.isEmpty();
    }

    public Move getLastMove() {
        if (moveSequence.isEmpty()) {
            return null;
        }
        return moveSequence.get(moveSequence.size() - 1);
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveSequence);
    }

    // Check if there's a piece between two positions (for rook, bishop, queen movements)
    public boolean isPieceBetween(Position start, Position end) {
        // Get direction of movement
        int colDir = Integer.compare(end.getColumn(), start.getColumn());
        int rowDir = Integer.compare(end.getRow(), start.getRow());

        Position current = start;

        // Move one step in the direction
        current = new Position(current.getColumn() + colDir, current.getRow() + rowDir);

        // Check all positions between start and end (exclusive)
        while (!current.equals(end)) {
            if (getPiece(current) != null) {
                return true;
            }
            current = new Position(current.getColumn() + colDir, current.getRow() + rowDir);
        }

        return false;
    }

    // Clear the board for testing purposes
    public void clearBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boardArray[row][col] = null;
            }
        }
        lightPieces.clear();
        darkPieces.clear();
        moveSequence.clear();
    }

    // Place a piece on the board for testing
    public void placePieceForTesting(Piece piece) {
        Position pos = piece.getPosition();
        boardArray[pos.getRow()][pos.getColumn()] = piece;

        if (piece.getColor() == PieceColor.WHITE) {
            lightPieces.add(piece);
            if (piece instanceof King) {
                lightKing = (King) piece;
            }
        } else {
            darkPieces.add(piece);
            if (piece instanceof King) {
                darkKing = (King) piece;
            }
        }
    }
}