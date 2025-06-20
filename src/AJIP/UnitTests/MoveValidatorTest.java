package AJIP.UnitTests;

import AJIP.Engine.BoardAnalyzer;
import AJIP.Engine.MoveValidator;
import AJIP.Engine.SafetyChecker;
import AJIP.Model.ChessBoard;
import AJIP.Model.ChessPiece;
import AJIP.Records.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Move Validator Tests")
class MoveValidatorTest {

    private ChessBoard board;
    private MoveValidator validator;

    @BeforeEach
    void setUp() {
        board = new ChessBoard();
        validator = new MoveValidator(board);
        BoardAnalyzer analyzer = new BoardAnalyzer(board);
        SafetyChecker safetyChecker = new SafetyChecker(board, validator, analyzer);
        validator.setSafetyChecker(safetyChecker);
    }

    @Nested
    @DisplayName("Pawn Movement Tests")
    class PawnMovementTests {

        @Test
        @DisplayName("Should allow valid pawn moves")
        void shouldAllowValidPawnMoves() {
            // One square forward
            ValidationResult result = validator.CanMove("white", "Pawn", 6, 0, 5, 0);
            assertTrue(result.isValid());

            // Two squares forward on first move
            result = validator.CanMove("white", "Pawn", 6, 0, 4, 0);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid pawn moves")
        void shouldRejectInvalidPawnMoves() {
            // Backward move
            ValidationResult result = validator.CanMove("white", "Pawn", 6, 0, 7, 0);
            assertFalse(result.isValid());

            // Sideways move
            result = validator.CanMove("white", "Pawn", 6, 0, 6, 1);
            assertFalse(result.isValid());

            // Three squares forward
            result = validator.CanMove("white", "Pawn", 6, 0, 3, 0);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should handle pawn captures correctly")
        void shouldHandlePawnCaptures() {
            // Place enemy piece diagonally
            board.board[5][1] = new ChessPiece("Pawn", "black");

            ValidationResult result = validator.CanCapture("white", "Pawn", 6, 0, 5, 1, false);
            assertTrue(result.isValid());

            // Cannot capture forward
            result = validator.CanCapture("white", "Pawn", 6, 0, 5, 0, false);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject two-square move after piece has moved")
        void shouldRejectTwoSquareMoveAfterFirstMove() {
            ChessPiece pawn = board.board[6][0];
            pawn.SetMoved();

            ValidationResult result = validator.CanMove("white", "Pawn", 6, 0, 4, 0);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject pawn move to occupied square")
        void shouldRejectPawnMoveToOccupiedSquare() {
            board.board[5][0] = new ChessPiece("Pawn", "black");

            ValidationResult result = validator.CanMove("white", "Pawn", 6, 0, 5, 0);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should handle en passant capture")
        void shouldHandleEnPassantCapture() {
            board.board[3][1] = new ChessPiece("Pawn", "black");

            ValidationResult result = validator.CanCapture("white", "Pawn", 3, 0, 2, 1, true);
            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("Rook Movement Tests")
    class RookMovementTests {

        @BeforeEach
        void clearPath() {
            // Clear some squares for testing
            board.board[6][0] = null; // Remove white pawn
            board.board[1][0] = null; // Remove black pawn
        }

        @Test
        @DisplayName("Should allow valid rook moves")
        void shouldAllowValidRookMoves() {
            // Vertical move
            ValidationResult result = validator.CanMove("white", "Rook", 7, 0, 5, 0);
            assertTrue(result.isValid());

            // Horizontal move
            board.board[7][1] = null; //
            result = validator.CanMove("white", "Rook", 7, 0, 7, 1);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject diagonal rook moves")
        void shouldRejectDiagonalRookMoves() {
            ValidationResult result = validator.CanMove("white", "Rook", 7, 0, 6, 1);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should detect blocked paths")
        void shouldDetectBlockedPaths() {
            // Path blocked by own piece
            ValidationResult result = validator.CanMove("white", "Rook", 7, 1, 6, 0);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should allow rook captures")
        void shouldAllowRookCaptures() {
            board.board[5][0] = new ChessPiece("Pawn", "black");

            ValidationResult result = validator.CanCapture("white", "Rook", 7, 0, 5, 0, false);
            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("Bishop Movement Tests")
    class BishopMovementTests {

        @BeforeEach
        void clearPath() {
            board.board[6][1] = null; // Remove white pawn
            board.board[6][3] = null; // Remove white pawn

        }

        @Test
        @DisplayName("Should allow valid bishop moves")
        void shouldAllowValidBishopMoves() {
            ValidationResult result = validator.CanMove("white", "Bishop", 7, 2, 5, 0);
            assertTrue(result.isValid());

            result = validator.CanMove("white", "Bishop", 7, 2, 4, 5);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject non-diagonal bishop moves")
        void shouldRejectNonDiagonalBishopMoves() {
            ValidationResult result = validator.CanMove("white", "Bishop", 7, 2, 7, 5);
            assertFalse(result.isValid());

            result = validator.CanMove("white", "Bishop", 7, 2, 5, 2);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should detect blocked diagonal paths")
        void shouldDetectBlockedDiagonalPaths() {
            board.board[6][1] = new ChessPiece("Pawn", "white"); // Replace pawn

            ValidationResult result = validator.CanMove("white", "Bishop", 7, 2, 5, 0);
            assertFalse(result.isValid());
        }
    }

    @Nested
    @DisplayName("Knight Movement Tests")
    class KnightMovementTests {

        @Test
        @DisplayName("Should allow valid knight moves")
        void shouldAllowValidKnightMoves() {
            ValidationResult result = validator.CanMove("white", "Knight", 7, 1, 5, 0);
            assertTrue(result.isValid());

            result = validator.CanMove("white", "Knight", 7, 1, 5, 2);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid knight moves")
        void shouldRejectInvalidKnightMoves() {
            // Not L-shaped
            ValidationResult result = validator.CanMove("white", "Knight", 7, 1, 6, 1);
            assertFalse(result.isValid());

            result = validator.CanMove("white", "Knight", 7, 1, 5, 1);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should allow knight to jump over pieces")
        void shouldAllowKnightToJumpOverPieces() {
            // Knight can jump over pawns
            ValidationResult result = validator.CanMove("white", "Knight", 7, 1, 5, 0);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should allow knight captures")
        void shouldAllowKnightCaptures() {
            board.board[5][0] = new ChessPiece("Pawn", "black");

            ValidationResult result = validator.CanCapture("white", "Knight", 7, 1, 5, 0, false);
            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("Queen Movement Tests")
    class QueenMovementTests {

        @BeforeEach
        void clearPath() {
            board.board[6][2] = null; // Remove white pawn in front of queen
            board.board[6][3] = null; // Remove white pawn in front of queen

        }

        @Test
        @DisplayName("Should allow valid queen moves")
        void shouldAllowValidQueenMoves() {
            // Vertical move
            ValidationResult result = validator.CanMove("white", "Queen", 7, 3, 5, 3);
            assertTrue(result.isValid());

            // Diagonal move
            result = validator.CanMove("white", "Queen", 7, 3, 5, 1);
            assertTrue(result.isValid());

            // Horizontal move
            board.board[7][2] = null; // Remove bishop
            result = validator.CanMove("white", "Queen", 7, 3, 7, 2);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid queen moves")
        void shouldRejectInvalidQueenMoves() {
            // Knight-like move
            ValidationResult result = validator.CanMove("white", "Queen", 7, 3, 5, 2);
            assertFalse(result.isValid());
        }
    }

    @Nested
    @DisplayName("King Movement Tests")
    class KingMovementTests {

        @Test
        @DisplayName("Should allow valid king moves")
        void shouldAllowValidKingMoves() {
            board.board[6][4] = null; // Remove pawn in front of king

            ValidationResult result = validator.CanMove("white", "King", 7, 4, 6, 4);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject multi-square king moves")
        void shouldRejectMultiSquareKingMoves() {
            board.board[6][4] = null;
            board.board[5][4] = null;

            ValidationResult result = validator.CanMove("white", "King", 7, 4, 5, 4);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should allow king captures")
        void shouldAllowKingCaptures() {
            board.board[6][4] = new ChessPiece("Pawn", "black");

            ValidationResult result = validator.CanCapture("white", "King", 7, 4, 6, 4, false);
            assertTrue(result.isValid());
        }
    }

    @Test
    @DisplayName("Should reject moves for unknown piece type")
    void shouldRejectMovesForUnknownPieceType() {
        ValidationResult result = validator.CanMove("white", "InvalidPiece", 7, 0, 6, 0);
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should reject captures for unknown piece type")
    void shouldRejectCapturesForUnknownPieceType() {
        ValidationResult result = validator.CanCapture("white", "InvalidPiece", 7, 0, 6, 0, false);
        assertFalse(result.isValid());
    }
}