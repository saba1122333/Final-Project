package JBTP.Testers;

import Model.*;
import Model.pieces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueenTest {
    private Board board;

    @BeforeEach
    public void setUp() {
        board = new Board();
        board.clearBoard(); // Start with an empty board for controlled testing

        // Place kings on the board to prevent NullPointerException in isKingInCheck
        King whiteKing = new King(PieceColor.WHITE, new Position(7, 7));
        King blackKing = new King(PieceColor.BLACK, new Position(0, 0));
        board.placePieceForTesting(whiteKing);
        board.placePieceForTesting(blackKing);
    }

    @Test
    public void testQueenMovement() {
        // Place a queen in the center of the board
        Queen queen = new Queen(PieceColor.WHITE, new Position(3, 3));
        board.placePieceForTesting(queen);

        // Get all legal moves
        List<Move> legalMoves = queen.getLegalMoves(board);

        // The queen should have 27 possible moves from this position on an empty board
        // (7 horizontal + 7 vertical + 13 diagonal)
        assertEquals(26, legalMoves.size());

        // Test that the queen can move horizontally
        assertTrue(containsMove(legalMoves, new Position(0, 3))); // Left
        assertTrue(containsMove(legalMoves, new Position(7, 3))); // Right

        // Test that the queen can move vertically
        assertTrue(containsMove(legalMoves, new Position(3, 0))); // Up
        assertTrue(containsMove(legalMoves, new Position(3, 7))); // Down

        // Test that the queen can move diagonally
        assertTrue(containsMove(legalMoves, new Position(0, 0))); // Up-left
        assertTrue(containsMove(legalMoves, new Position(6, 0))); // Up-right
        assertTrue(containsMove(legalMoves, new Position(0, 6))); // Down-left
        assertFalse(containsMove(legalMoves, new Position(7, 7))); // Down-right
    }

    @Test
    public void testQueenCapture() {
        // Place a white queen and a black pawn on the board
        Queen queen = new Queen(PieceColor.WHITE, new Position(3, 3));
        Pawn enemyPawn = new Pawn(PieceColor.BLACK, new Position(3, 0));
        board.placePieceForTesting(queen);
        board.placePieceForTesting(enemyPawn);

        // Get all legal moves
        List<Move> legalMoves = queen.getLegalMoves(board);

        // The queen should be able to capture the pawn
        assertTrue(containsMove(legalMoves, new Position(3, 0)));

        // Place a friendly pawn in the way
        Pawn friendlyPawn = new Pawn(PieceColor.WHITE, new Position(3, 1));
        board.placePieceForTesting(friendlyPawn);

        // Get updated legal moves
        legalMoves = queen.getLegalMoves(board);

        // The queen should not be able to capture or move past the friendly pawn
        assertFalse(containsMove(legalMoves, new Position(3, 0)));
        assertFalse(containsMove(legalMoves, new Position(3, 1)));
    }

    @Test
    public void testQueenBlockedByPieces() {
        // Place a queen in the center and surround it with pawns
        Queen queen = new Queen(PieceColor.WHITE, new Position(3, 3));
        board.placePieceForTesting(queen);

        // Place pawns in all directions around the queen
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(2, 2)));
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(3, 2)));
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(4, 2)));
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(2, 3)));
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(4, 3)));
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(2, 4)));
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(3, 4)));
        board.placePieceForTesting(new Pawn(PieceColor.WHITE, new Position(4, 4)));

        // Get legal moves
        List<Move> legalMoves = queen.getLegalMoves(board);

        // The queen should be completely blocked
        assertEquals(0, legalMoves.size());
    }

    @Test
    public void testQueenAttackPositions() {
        // Place a queen in the center of the board
        Queen queen = new Queen(PieceColor.WHITE, new Position(3, 3));
        board.placePieceForTesting(queen);

        // Get all attack positions
        List<Position> attackPositions = queen.getAttackPositions(board);

        // The queen should attack 27 positions on an empty board
        assertEquals(27, attackPositions.size());

        // Place a piece in the path of the queen
        board.placePieceForTesting(new Pawn(PieceColor.BLACK, new Position(3, 5)));

        // Get updated attack positions
        attackPositions = queen.getAttackPositions(board);

        // The queen should attack the square with the pawn, but not beyond it
        assertTrue(attackPositions.contains(new Position(3, 5)));
        assertFalse(attackPositions.contains(new Position(3, 6)));
        assertFalse(attackPositions.contains(new Position(3, 7)));
    }

    @Test
    public void testQueenCheck() {
        // Place a white queen and a black king on the board
        Queen queen = new Queen(PieceColor.WHITE, new Position(3, 3));

        // Remove the existing black king first to avoid duplicate kings
        King oldBlackKing = board.getKing(PieceColor.BLACK);
        if (oldBlackKing != null) {
            // Remove the king from the board array using its position
            Position oldKingPos = oldBlackKing.getPosition();

        }

        // Place a new black king where we want it for this test
        King enemyKing = new King(PieceColor.BLACK, new Position(3, 0));

        board.placePieceForTesting(queen);
        board.placePieceForTesting(enemyKing);

        // Check if the king is in check
        assertTrue(board.isKingInCheck(PieceColor.BLACK));

        // Place a piece blocking the check
        board.placePieceForTesting(new Pawn(PieceColor.BLACK, new Position(3, 1)));

        // The king should no longer be in check
        assertFalse(board.isKingInCheck(PieceColor.BLACK));
    }

    @Test
    public void testQueenDuplicate() {
        // Create a queen and set hasMoved to true
        Queen original = new Queen(PieceColor.WHITE, new Position(3, 3));
        original.setHasMoved(true);

        // Duplicate the queen
        Piece duplicate = original.duplicate();

        // Check that the duplicate has the same properties
        assertTrue(duplicate instanceof Queen);
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