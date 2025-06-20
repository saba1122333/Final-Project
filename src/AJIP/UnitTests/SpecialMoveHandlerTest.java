package AJIP.UnitTests;

import AJIP.Engine.*;

import AJIP.Model.*;
import AJIP.Records.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Special Move Handler Tests")
class SpecialMoveHandlerTest {

    private ChessBoard board;
    private SpecialMoveHandler specialMoveHandler;

    @BeforeEach
    void setUp() {
        board = new ChessBoard();
        MoveValidator validator = new MoveValidator(board);
        BoardAnalyzer analyzer = new BoardAnalyzer(board);
        SafetyChecker safetyChecker = new SafetyChecker(board, validator, analyzer);
        specialMoveHandler = new SpecialMoveHandler(board, validator, safetyChecker, analyzer);
        validator.setSafetyChecker(safetyChecker);
    }

    @Nested
    @DisplayName("Castling Tests")
    class CastlingTests {

        @BeforeEach
        void clearCastlingPath() {
            // Clear pieces between king and rook for kingside castling
            board.board[7][5] = null; // Bishop
            board.board[7][6] = null; // Knight
        }

        @Test
        @DisplayName("Should allow valid kingside castling")
        void shouldAllowValidKingsideCastling() {
            ChessMove castling = createCastlingMove("white", 7, 4, 7, 6);
            ValidationResult result = specialMoveHandler.CanCastle(castling);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should allow valid queenside castling")
        void shouldAllowValidQueensideCastling() {
            // Clear queenside path
            board.board[7][1] = null; // Knight
            board.board[7][2] = null; // Bishop
            board.board[7][3] = null; // Queen

            ChessMove castling = createCastlingMove("white", 7, 4, 7, 2);
            ValidationResult result = specialMoveHandler.CanCastle(castling);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject castling when king has moved")
        void shouldRejectCastlingWhenKingHasMoved() {
            board.board[7][4].SetMoved();

            ChessMove castling = createCastlingMove("white", 7, 4, 7, 6);
            ValidationResult result = specialMoveHandler.CanCastle(castling);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject castling when rook has moved")
        void shouldRejectCastlingWhenRookHasMoved() {
            board.board[7][7].SetMoved();

            ChessMove castling = createCastlingMove("white", 7, 4, 7, 6);
            ValidationResult result = specialMoveHandler.CanCastle(castling);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject castling when path is blocked")
        void shouldRejectCastlingWhenPathIsBlocked() {
            // Leave bishop in place
            board.board[7][5] = board.board[7][2]; // Place a piece in the way

            ChessMove castling = createCastlingMove("white", 7, 4, 7, 6);
            ValidationResult result = specialMoveHandler.CanCastle(castling);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject castling when king would pass through check")
        void shouldRejectCastlingWhenKingPassesThroughCheck() {
            // Place attacking rook that would attack king's path
            board.board[6][5] = null; // Remove black pawn
            board.board[2][5] = board.board[0][0]; // Move black rook to attack f-file

            ChessMove castling = createCastlingMove("white", 7, 4, 7, 6);
            ValidationResult result = specialMoveHandler.CanCastle(castling);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject castling with missing king")
        void shouldRejectCastlingWithMissingKing() {
            board.board[7][4] = null; // Remove king

            ChessMove castling = createCastlingMove("white", 7, 4, 7, 6);
            ValidationResult result = specialMoveHandler.CanCastle(castling);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject castling with missing rook")
        void shouldRejectCastlingWithMissingRook() {
            board.board[7][7] = null; // Remove rook

            ChessMove castling = createCastlingMove("white", 7, 4, 7, 6);
            ValidationResult result = specialMoveHandler.CanCastle(castling);
            assertFalse(result.isValid());
        }

        private ChessMove createCastlingMove(String color, int fromRow, int fromCol, int toRow, int toCol) {
            ChessMove move = new ChessMove();
            move.color = color;
            move.pieceType = "King";
            move.fromRow = fromRow;
            move.fromCol = fromCol;
            move.toRow = toRow;
            move.toCol = toCol;
            move.isCastling = true;
            return move;
        }
    }

    @Nested
    @DisplayName("Promotion Tests")
    class PromotionTests {

        @Test
        @DisplayName("Should allow valid pawn promotion for white")
        void shouldAllowValidPawnPromotionForWhite() {
            // Place white pawn on 7th rank
            board.board[1][0] = board.board[6][0]; // Move white pawn to 7th rank
            board.board[0][0] = null;
            ValidationResult result = specialMoveHandler.CanPromote("white", "Pawn", 1, 0, 0, 0);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should allow valid pawn promotion for black")
        void shouldAllowValidPawnPromotionForBlack() {
            // Place black pawn on 2nd rank
            board.board[6][0] = board.board[1][0]; // Move black pawn to 2nd rank
            board.board[7][0] = null;

            ValidationResult result = specialMoveHandler.CanPromote("black", "Pawn", 6, 0, 7, 0);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject promotion for non-pawns")
        void shouldRejectPromotionForNonPawns() {
            ValidationResult result = specialMoveHandler.CanPromote("white", "Queen", 1, 0, 0, 0);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject promotion on wrong rank for white")
        void shouldRejectPromotionOnWrongRankForWhite() {
            ValidationResult result = specialMoveHandler.CanPromote("white", "Pawn", 6, 0, 5, 0);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should reject promotion on wrong rank for black")
        void shouldRejectPromotionOnWrongRankForBlack() {
            ValidationResult result = specialMoveHandler.CanPromote("black", "Pawn", 1, 0, 2, 0);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should allow promotion capture")
        void shouldAllowPromotionCapture() {
            // Place white pawn on 7th rank and enemy piece on 8th rank
            board.board[1][0] = board.board[6][0]; // Move white pawn
            board.board[6][0] = null;
            board.board[0][1] = board.board[1][1]; // Move black pawn to be captured
            board.board[1][1] = null;

            ValidationResult result = specialMoveHandler.CanPromote("white", "Pawn", 1, 0, 0, 1);
            assertTrue(result.isValid());
        }
    }

    @Nested
    @DisplayName("Check Detection Tests")
    class CheckDetectionTests {

        @Test
        @DisplayName("Should detect valid check")
        void shouldDetectValidCheck() {
            // Clear path and move queen to give check
            board.board[6][4] = null; // Remove white pawn
            board.board[6][4] = board.board[0][3]; // Remove white pawn
            board.board[0][3] = null;

            ValidationResult result = specialMoveHandler.CanCheck("black", "Queen", 6, 4, 7, 4);
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should reject invalid check")
        void shouldRejectInvalidCheck() {
            // Move that doesn't give check
            ValidationResult result = specialMoveHandler.CanCheck("white", "Pawn", 6, 0, 5, 0);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should handle missing opponent king")
        void shouldHandleMissingOpponentKing() {
            board.board[0][4] = null; // Remove black king

            ValidationResult result = specialMoveHandler.CanCheck("white", "Queen", 7, 3, 5, 3);
            assertFalse(result.isValid());
        }

        @Test
        @DisplayName("Should handle multiple opponent kings")
        void shouldHandleMultipleOpponentKings() {
            board.board[0][3] = board.board[0][4]; // Duplicate black king

            ValidationResult result = specialMoveHandler.CanCheck("white", "Queen", 7, 3, 5, 3);
            assertFalse(result.isValid());
        }
    }
}