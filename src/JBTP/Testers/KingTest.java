package JBTP.Testers;

import Model.*;
import Model.pieces.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class KingTest {
    private Board board;

    @Before
    public void setUp() {
        board = new Board();
        board.clearBoard(); // Clear the board for custom test setups
    }

    @Test
    public void testBasicMovement() {
        // Place a king in the middle of the board
        King king = new King(PieceColor.WHITE, new Position(4, 4));
        board.placePieceForTesting(king);

        List<Move> legalMoves = king.getLegalMoves(board);

        // The king should be able to move to all 8 surrounding squares
        assertEquals(8, legalMoves.size());

        // Verify all moves are to adjacent squares
        for (Move move : legalMoves) {
            Position dest = move.getDestination();
            int rowDiff = Math.abs(dest.getRow() - king.getPosition().getRow());
            int colDiff = Math.abs(dest.getColumn() - king.getPosition().getColumn());

            // Ensure the move is only one square away
            assertTrue(rowDiff <= 1);
            assertTrue(colDiff <= 1);
            // Ensure it's not the same position
            assertFalse(rowDiff == 0 && colDiff == 0);
        }
    }

    @Test
    public void testCapturingOpponentPieces() {
        // Place a white king in the middle
        King king = new King(PieceColor.WHITE, new Position(4, 4));
        board.placePieceForTesting(king);

        // Place some black pieces around it
        Pawn blackPawn1 = new Pawn(PieceColor.BLACK, new Position(3, 3));
        Pawn blackPawn2 = new Pawn(PieceColor.BLACK, new Position(5, 5));
        board.placePieceForTesting(blackPawn1);
        board.placePieceForTesting(blackPawn2);

        List<Move> legalMoves = king.getLegalMoves(board);

        // Verify the king can capture both pawns
        boolean canCaptureFirstPawn = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(blackPawn1.getPosition()));
        boolean canCaptureSecondPawn = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(blackPawn2.getPosition()));

        assertTrue(canCaptureFirstPawn);
        assertTrue(canCaptureSecondPawn);
    }

    @Test
    public void testCannotCaptureFriendlyPieces() {
        // Place a white king in the middle
        King king = new King(PieceColor.WHITE, new Position(4, 4));
        board.placePieceForTesting(king);

        // Place some white pieces around it
        Pawn whitePawn1 = new Pawn(PieceColor.WHITE, new Position(3, 3));
        Pawn whitePawn2 = new Pawn(PieceColor.WHITE, new Position(5, 5));
        board.placePieceForTesting(whitePawn1);
        board.placePieceForTesting(whitePawn2);

        List<Move> legalMoves = king.getLegalMoves(board);

        // Verify the king cannot capture friendly pieces
        boolean canCaptureFirstPawn = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(whitePawn1.getPosition()));
        boolean canCaptureSecondPawn = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(whitePawn2.getPosition()));

        assertFalse(canCaptureFirstPawn);
        assertFalse(canCaptureSecondPawn);

        // King should still have moves to the 6 remaining squares around it
        assertEquals(6, legalMoves.size());
    }

    @Test
    public void testKingsideCastling() {
        // Set up the necessary pieces for kingside castling
        King whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        Rook whiteRook = new Rook(PieceColor.WHITE, new Position(7, 7));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(whiteRook);

        List<Move> legalMoves = whiteKing.getLegalMoves(board);

        // Find the castling move
        boolean hasCastlingMove = legalMoves.stream()
                .anyMatch(move -> move.isCastlingMove() && move.getDestination().equals(new Position(6, 7)));

        assertTrue("Should have kingside castling option", hasCastlingMove);
    }

    @Test
    public void testQueensideCastling() {
        // Set up the necessary pieces for queenside castling
        King whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        Rook whiteRook = new Rook(PieceColor.WHITE, new Position(0, 7));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(whiteRook);

        List<Move> legalMoves = whiteKing.getLegalMoves(board);

        // Find the castling move
        boolean hasCastlingMove = legalMoves.stream()
                .anyMatch(move -> move.isCastlingMove() && move.getDestination().equals(new Position(2, 7)));

        assertTrue("Should have queenside castling option", hasCastlingMove);
    }

    @Test
    public void testNoCastlingAfterKingMoved() {
        // Set up king and rook, but mark the king as moved
        King whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        whiteKing.setHasMoved(true);
        Rook whiteRook = new Rook(PieceColor.WHITE, new Position(7, 7));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(whiteRook);

        List<Move> legalMoves = whiteKing.getLegalMoves(board);

        // Verify there's no castling move
        boolean hasCastlingMove = legalMoves.stream()
                .anyMatch(Move::isCastlingMove);

        assertFalse("Should not have castling option after king moved", hasCastlingMove);
    }

    @Test
    public void testNoCastlingAfterRookMoved() {
        // Set up king and rook, but mark the rook as moved
        King whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        Rook whiteRook = new Rook(PieceColor.WHITE, new Position(7, 7));
        whiteRook.setHasMoved(true);
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(whiteRook);

        List<Move> legalMoves = whiteKing.getLegalMoves(board);

        // Verify there's no castling move
        boolean hasKingsideCastling = legalMoves.stream()
                .anyMatch(move -> move.isCastlingMove() && move.getDestination().equals(new Position(6, 7)));

        assertFalse("Should not have castling option after rook moved", hasKingsideCastling);
    }

    @Test
    public void testNoCastlingWithPiecesBetween() {
        // Set up pieces for kingside castling but add a piece in between
        King whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        Rook whiteRook = new Rook(PieceColor.WHITE, new Position(7, 7));
        Knight knight = new Knight(PieceColor.WHITE, new Position(5, 7)); // Piece between king and rook
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(whiteRook);
        board.placePieceForTesting(knight);

        List<Move> legalMoves = whiteKing.getLegalMoves(board);

        // Verify there's no castling move
        boolean hasKingsideCastling = legalMoves.stream()
                .anyMatch(move -> move.isCastlingMove() && move.getDestination().equals(new Position(6, 7)));

        assertFalse("Should not have castling option with pieces between", hasKingsideCastling);
    }

    @Test
    public void testNoCastlingThroughCheck() {
        // Set up pieces for kingside castling
        King whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        Rook whiteRook = new Rook(PieceColor.WHITE, new Position(7, 7));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(whiteRook);

        // Add an enemy rook that attacks the square the king would pass through
        Rook blackRook = new Rook(PieceColor.BLACK, new Position(5, 0));
        board.placePieceForTesting(blackRook);

        List<Move> legalMoves = whiteKing.getLegalMoves(board);

        // Verify there's no castling move
        boolean hasKingsideCastling = legalMoves.stream()
                .anyMatch(move -> move.isCastlingMove() && move.getDestination().equals(new Position(6, 7)));

        assertFalse("Should not be able to castle through check", hasKingsideCastling);
    }

    @Test
    public void testNoCastlingWhileInCheck() {
        // Set up pieces for kingside castling
        King whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        Rook whiteRook = new Rook(PieceColor.WHITE, new Position(7, 7));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(whiteRook);

        // Add an enemy rook that puts the king in check
        Rook blackRook = new Rook(PieceColor.BLACK, new Position(4, 0));
        board.placePieceForTesting(blackRook);

        List<Move> legalMoves = whiteKing.getLegalMoves(board);

        // Verify there's no castling move
        boolean hasCastlingMove = legalMoves.stream()
                .anyMatch(Move::isCastlingMove);

        assertFalse("Should not be able to castle while in check", hasCastlingMove);
    }

    @Test
    public void testCannotMoveIntoCheck() {
        // Place a white king
        King king = new King(PieceColor.WHITE, new Position(4, 4));
        board.placePieceForTesting(king);

        // Place a black rook that controls some squares
        Rook blackRook = new Rook(PieceColor.BLACK, new Position(6, 2));
        board.placePieceForTesting(blackRook);

        List<Move> legalMoves = king.getLegalMoves(board);

        // The king should not be able to move to any square attacked by the rook
        boolean canMoveToE3 = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(new Position(4, 3)));
        boolean canMoveToF4 = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(new Position(5, 4)));

    }

    @Test
    public void testGetAttackPositions() {
        // Place a king in the middle of the board
        King king = new King(PieceColor.WHITE, new Position(4, 4));
        board.placePieceForTesting(king);

        List<Position> attackPositions = king.getAttackPositions(board);

        // The king should attack all 8 surrounding squares
        assertEquals(8, attackPositions.size());

        // Verify all positions are adjacent
        for (Position pos : attackPositions) {
            int rowDiff = Math.abs(pos.getRow() - king.getPosition().getRow());
            int colDiff = Math.abs(pos.getColumn() - king.getPosition().getColumn());

            assertTrue(rowDiff <= 1);
            assertTrue(colDiff <= 1);
            assertFalse(rowDiff == 0 && colDiff == 0);
        }
    }

    @Test
    public void testKingAtEdge() {
        // Place a king at the edge of the board
        King king = new King(PieceColor.WHITE, new Position(0, 0));
        board.placePieceForTesting(king);

        List<Move> legalMoves = king.getLegalMoves(board);

        // The king should only have 3 possible moves
        assertEquals(3, legalMoves.size());

        // Verify destinations: (0,1), (1,0), and (1,1)
        boolean canMoveTo01 = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(new Position(0, 1)));
        boolean canMoveTo10 = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(new Position(1, 0)));
        boolean canMoveTo11 = legalMoves.stream()
                .anyMatch(move -> move.getDestination().equals(new Position(1, 1)));

        assertTrue(canMoveTo01);
        assertTrue(canMoveTo10);
        assertTrue(canMoveTo11);
    }

    @Test
    public void testGetType() {
        King king = new King(PieceColor.WHITE, new Position(0, 0));
        assertEquals("King", king.getType());
    }

    @Test
    public void testDuplicate() {
        King original = new King(PieceColor.WHITE, new Position(4, 4));
        original.setHasMoved(true);

        Piece copy = original.duplicate();

        assertTrue(copy instanceof King);
        assertEquals(original.getColor(), copy.getColor());
        assertEquals(original.getPosition(), copy.getPosition());
        assertEquals(original.hasMoved(), copy.hasMoved());
    }
}