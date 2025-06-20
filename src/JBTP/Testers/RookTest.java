package JBTP.Testers;

import JBTP.Model.*;
import JBTP.Model.pieces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RookTest {
    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
        board.clearBoard(); // Start with an empty board for controlled testing

        // Add kings to the board to avoid NullPointerException in isKingInCheck
        King whiteKing = new King(PieceColor.WHITE, new Position(0, 0));
        King blackKing = new King(PieceColor.BLACK, new Position(7, 7));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(blackKing);
    }

    @Test
    public void testRookMovement() {
        // Place a rook in the center of the board
        Rook rook = new Rook(PieceColor.WHITE, new Position(3, 3));
        board.placePieceForTesting(rook);

        // Get all legal moves
        List<Move> legalMoves = rook.getLegalMoves(board);

        // The rook should have 14 possible moves from this position on an empty board
        // (7 horizontal + 7 vertical)
        assertEquals(14, legalMoves.size());

        // Test that the rook can move horizontally
        assertTrue(containsMove(legalMoves, new Position(0, 3))); // Left
        assertTrue(containsMove(legalMoves, new Position(7, 3))); // Right

        // Test that the rook can move vertically
        assertTrue(containsMove(legalMoves, new Position(3, 0))); // Up
        assertTrue(containsMove(legalMoves, new Position(3, 7))); // Down

        // Test that the rook cannot move diagonally
        assertFalse(containsMove(legalMoves, new Position(4, 4))); // Diagonal
        assertFalse(containsMove(legalMoves, new Position(2, 2))); // Diagonal
    }

    @Test
    public void testRookCapture() {
        // Place a white rook and a black pawn on the board
        Rook rook = new Rook(PieceColor.WHITE, new Position(3, 3));
        Pawn enemyPawn = new Pawn(PieceColor.BLACK, new Position(3, 0));
        board.placePieceForTesting(rook);
        board.placePieceForTesting(enemyPawn);

        // Get all legal moves
        List<Move> legalMoves = rook.getLegalMoves(board);

        // The rook should be able to capture the pawn
        assertTrue(containsMove(legalMoves, new Position(3, 0)));

        // Place a friendly pawn in the way
        Pawn friendlyPawn = new Pawn(PieceColor.WHITE, new Position(3, 1));
        board.placePieceForTesting(friendlyPawn);

        // Get updated legal moves
        legalMoves = rook.getLegalMoves(board);

        // The rook should not be able to capture or move past the friendly pawn
        assertFalse(containsMove(legalMoves, new Position(3, 0)));
        assertFalse(containsMove(legalMoves, new Position(3, 1)));
    }

    @Test
    public void testRookBlockedByPieces() {
        // Place a rook and place pieces around it
        Rook rook = new Rook(PieceColor.WHITE, new Position(3, 3));
        board.placePieceForTesting(rook);

        // Place pawns in the four cardinal directions
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(3, 2))); // North
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(4, 3))); // East
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(3, 4))); // South
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(2, 3))); // West

        // Get legal moves
        List<Move> legalMoves = rook.getLegalMoves(board);

        // The rook should be completely blocked
        assertEquals(0, legalMoves.size());
    }

    @Test
    public void testRookAttackPositions() {
        // Place a rook in the center of the board
        Rook rook = new Rook(PieceColor.WHITE, new Position(3, 3));
        board.placePieceForTesting(rook);

        // Get all attack positions
        List<Position> attackPositions = rook.getAttackPositions(board);

        // The rook should attack 14 positions on an empty board
        assertEquals(14, attackPositions.size());

        // Place a piece in the path of the rook
        board.placePieceForTesting(new Pawn(PieceColor.BLACK, new Position(3, 5)));

        // Get updated attack positions
        attackPositions = rook.getAttackPositions(board);

        // The rook should attack the square with the pawn, but not beyond it
        assertTrue(attackPositions.contains(new Position(3, 5)));
        assertFalse(attackPositions.contains(new Position(3, 6)));
        assertFalse(attackPositions.contains(new Position(3, 7)));
    }

    @Test
    public void testRookCheck() {
        // Set up a new board with kings
        board.clearBoard();
        King whiteKing = new King(PieceColor.WHITE, new Position(0, 0));
        King blackKing = new King(PieceColor.BLACK, new Position(3, 0));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(blackKing);

        // Place a white rook in position to check the black king
        Rook rook = new Rook(PieceColor.WHITE, new Position(3, 3));
        board.placePieceForTesting(rook);

        // Check if the king is in check
        assertTrue(board.isKingInCheck(PieceColor.BLACK));

        // Place a piece blocking the check
        board.placePieceForTesting(new Pawn(PieceColor.BLACK, new Position(3, 1)));

        // The king should no longer be in check
        assertFalse(board.isKingInCheck(PieceColor.BLACK));
    }

    @Test
    public void testRookCastling() {
        // Clear the board and set up kings
        board.clearBoard();
        King whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        King blackKing = new King(PieceColor.BLACK, new Position(4, 0));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(blackKing);

        // Set up a king and rook in initial positions
        Rook rook = new Rook(PieceColor.WHITE, new Position(7, 7)); // Kingside rook
        board.placePieceForTesting(rook);

        // Verify that castling is possible
        assertTrue(rook.canParticipateInCastling(board, whiteKing));

        // Move the rook and verify castling is no longer possible
        rook.setHasMoved(true);
        assertFalse(rook.canParticipateInCastling(board, whiteKing));
        rook.setHasMoved(false); // Reset for next test

        // Move the king and verify castling is no longer possible
        whiteKing.setHasMoved(true);
        assertFalse(rook.canParticipateInCastling(board, whiteKing));
        whiteKing.setHasMoved(false); // Reset for next test

        // Put a piece between the king and rook
        board.placePieceForTesting(new Knight(PieceColor.WHITE, new Position(6, 7)));
        assertFalse(rook.canParticipateInCastling(board, whiteKing));

        // Clear the board and set up a king in check
        board.clearBoard();
        whiteKing = new King(PieceColor.WHITE, new Position(4, 7));
        blackKing = new King(PieceColor.BLACK, new Position(4, 0));
        rook = new Rook(PieceColor.WHITE, new Position(7, 7));
        Queen enemyQueen = new Queen(PieceColor.BLACK, new Position(4, 1));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(blackKing);
        board.placePieceForTesting(rook);
        board.placePieceForTesting(enemyQueen);

        // Verify castling is not possible when the king is in check
        assertFalse(rook.canParticipateInCastling(board, whiteKing));
    }

    @Test
    public void testRookDuplicate() {
        // Create a rook and set hasMoved to true
        Rook original = new Rook(PieceColor.WHITE, new Position(3, 3));
        original.setHasMoved(true);

        // Duplicate the rook
        Piece duplicate = original.duplicate();

        // Check that the duplicate has the same properties
        assertTrue(duplicate instanceof Rook);
        assertEquals(original.getColor(), duplicate.getColor());
        assertEquals(original.getPosition().getColumn(), duplicate.getPosition().getColumn());
        assertEquals(original.getPosition().getRow(), duplicate.getPosition().getRow());
        assertEquals(original.hasMoved(), duplicate.hasMoved());
    }

    // Helper method to check if a position is in the list of legal moves
    private boolean containsMove(List<Move> moves, Position position) {
        for (Move move : moves) {
            if (move.getDestination().equals(position)) {
                return true;
            }
        }
        return false;
    }
}