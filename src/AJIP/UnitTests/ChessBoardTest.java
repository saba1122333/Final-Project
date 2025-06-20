package AJIP.UnitTests;


import AJIP.Model.ChessBoard;
import AJIP.Model.ChessPiece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Chess Board Tests")
class ChessBoardTest {

    private ChessBoard board;

    @BeforeEach
    void setUp() {
        board = new ChessBoard();
    }

    @Test
    @DisplayName("Should initialize board with correct piece placement")
    void shouldInitializeBoardWithCorrectPiecePlacement() {
        // Check black pieces on row 0
        assertEquals("Rook", board.board[0][0].getType());
        assertEquals("black", board.board[0][0].getColor());
        assertEquals("Knight", board.board[0][1].getType());
        assertEquals("Bishop", board.board[0][2].getType());
        assertEquals("Queen", board.board[0][3].getType());
        assertEquals("King", board.board[0][4].getType());
        assertEquals("Bishop", board.board[0][5].getType());
        assertEquals("Knight", board.board[0][6].getType());
        assertEquals("Rook", board.board[0][7].getType());

        // Check white pieces on row 7
        assertEquals("Rook", board.board[7][0].getType());
        assertEquals("white", board.board[7][0].getColor());
        assertEquals("Knight", board.board[7][1].getType());
        assertEquals("Bishop", board.board[7][2].getType());
        assertEquals("Queen", board.board[7][3].getType());
        assertEquals("King", board.board[7][4].getType());
        assertEquals("Bishop", board.board[7][5].getType());
        assertEquals("Knight", board.board[7][6].getType());
        assertEquals("Rook", board.board[7][7].getType());
    }

    @Test
    @DisplayName("Should initialize all pawns correctly")
    void shouldInitializeAllPawnsCorrectly() {
        // Check black pawns on row 1
        for (int col = 0; col < 8; col++) {
            assertEquals("Pawn", board.board[1][col].getType());
            assertEquals("black", board.board[1][col].getColor());
            assertFalse(board.board[1][col].IsMoved());
        }

        // Check white pawns on row 6
        for (int col = 0; col < 8; col++) {
            assertEquals("Pawn", board.board[6][col].getType());
            assertEquals("white", board.board[6][col].getColor());
            assertFalse(board.board[6][col].IsMoved());
        }
    }

    @Test
    @DisplayName("Should have empty squares in middle rows")
    void shouldHaveEmptySquaresInMiddleRows() {
        // Check rows 2-5 are empty
        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                assertNull(board.board[row][col]);
            }
        }
    }

    @Test
    @DisplayName("Should verify all pieces are unmoved initially")
    void shouldVerifyAllPiecesAreUnmovedInitially() {
        // Check all pieces are unmoved
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board.board[row][col] != null) {
                    assertFalse(board.board[row][col].IsMoved());
                }
            }
        }
    }

    @Test
    @DisplayName("Should reset board correctly")
    void shouldResetBoardCorrectly() {
        // Modify board state
        board.board[4][4] = new ChessPiece("Queen", "white");
        board.board[0][0] = null;
        board.board[7][4].SetMoved(); // Mark king as moved

        // Reset board
        board.ResetBoard();

        // Verify reset
        assertNull(board.board[4][4]);
        assertNotNull(board.board[0][0]);
        assertEquals("Rook", board.board[0][0].getType());
        assertEquals("black", board.board[0][0].getColor());
        assertFalse(board.board[7][4].IsMoved()); // King should be unmoved again
    }

    @Test
    @DisplayName("Should maintain piece colors correctly")
    void shouldMaintainPieceColorsCorrectly() {
        // Check all pieces on rows 0-1 are black
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 8; col++) {
                assertEquals("black", board.board[row][col].getColor());
            }
        }

        // Check all pieces on rows 6-7 are white
        for (int row = 6; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                assertEquals("white", board.board[row][col].getColor());
            }
        }
    }

    @Test
    @DisplayName("Should have correct board dimensions")
    void shouldHaveCorrectBoardDimensions() {
        assertEquals(8, board.board.length);
        for (int row = 0; row < 8; row++) {
            assertEquals(8, board.board[row].length);
        }
    }

    @Test
    @DisplayName("Should allow piece modification")
    void shouldAllowPieceModification() {
        // Test that board allows modifications
        ChessPiece originalPiece = board.board[6][0];
        ChessPiece newPiece = new ChessPiece("Queen", "white");

        board.board[6][0] = newPiece;
        assertEquals(newPiece, board.board[6][0]);
        assertNotEquals(originalPiece, board.board[6][0]);
    }

    @Test
    @DisplayName("Should maintain references to pieces")
    void shouldMaintainReferencesToPieces() {
        ChessPiece piece = board.board[6][0];
        piece.SetMoved();

        // Same reference should show the change
        assertTrue(board.board[6][0].IsMoved());
    }

    @Test
    @DisplayName("Should handle piece removal")
    void shouldHandlePieceRemoval() {
        assertNotNull(board.board[6][0]);

        board.board[6][0] = null;
        assertNull(board.board[6][0]);
    }
}
