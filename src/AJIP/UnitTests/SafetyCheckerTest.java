package AJIP.UnitTests;

import AJIP.Engine.BoardAnalyzer;
import AJIP.Engine.MoveValidator;
import AJIP.Engine.SafetyChecker;
import AJIP.Model.*;
import AJIP.Model.ChessPiece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Safety Checker Tests")
class SafetyCheckerTest {

    private ChessBoard board;
    private SafetyChecker safetyChecker;

    @BeforeEach
    void setUp() {
        board = new ChessBoard();
        MoveValidator validator = new MoveValidator(board);
        BoardAnalyzer analyzer = new BoardAnalyzer(board);
        safetyChecker = new SafetyChecker(board, validator, analyzer);
        validator.setSafetyChecker(safetyChecker);
    }

    @Test
    @DisplayName("Should detect king safety on initial board")
    void shouldDetectKingSafetyOnInitialBoard() {
        assertTrue(safetyChecker.IsSquareSafeForKing("white", 7, 4));
        assertTrue(safetyChecker.IsSquareSafeForKing("black", 0, 4));
    }

    @Test
    @DisplayName("Should detect enemy pawn attacks")
    void shouldDetectEnemyPawnAttacks() {
        // Place black pawn that attacks white king position
        board.board[6][3] = new ChessPiece("Pawn", "black");

        assertFalse(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should detect enemy piece attacks")
    void shouldDetectEnemyPieceAttacks() {
        // Move black rook to attack white king
        board.board[1][4] = null; // Remove black pawn
        board.board[2][4] = null;
        board.board[3][4] = null;
        board.board[4][4] = null;
        board.board[5][4] = null;
        board.board[6][4] = null;
        board.board[1][4] = board.board[0][0]; // Move black rook to e8
        board.board[0][0] = null;

        assertFalse(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should detect king proximity")
    void shouldDetectKingProximity() {
        // Place enemy king next to current king position
        board.board[6][4] = new ChessPiece("King", "black");

        assertFalse(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should detect queen attacks")
    void shouldDetectQueenAttacks() {
        // Clear diagonal path and place attacking queen
        board.board[6][5] = null; // Remove white pawn
        board.board[5][6] = new ChessPiece("Queen", "black");

        assertFalse(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should detect bishop attacks")
    void shouldDetectBishopAttacks() {
        // Clear diagonal path and place attacking bishop
        board.board[6][3] = null; // Remove white pawn
        board.board[5][2] = new ChessPiece("Bishop", "black");

        assertFalse(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should detect knight attacks")
    void shouldDetectKnightAttacks() {
        // Place attacking knight
        board.board[5][3] = new ChessPiece("Knight", "black");

        assertFalse(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should detect rook attacks")
    void shouldDetectRookAttacks() {
        // Clear horizontal path and place attacking rook
        board.board[7][3] = null; // Remove white queen
        board.board[7][2] = null; // Remove white bishop
        board.board[7][1] = null; // Remove white knight
        board.board[7][0] = new ChessPiece("Rook", "black");

        assertFalse(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should ignore own pieces")
    void shouldIgnoreOwnPieces() {
        // Place own piece that would "attack" king
        board.board[6][3] = new ChessPiece("Queen", "white");

        assertTrue(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should handle blocked attacks")
    void shouldHandleBlockedAttacks() {
        // Place attacking piece but block the attack
        board.board[1][4] = null; // Remove black pawn
        board.board[5][4] = new ChessPiece("Pawn", "white"); // Blocking piece

        assertTrue(safetyChecker.IsSquareSafeForKing("white", 7, 4));
    }

    @Test
    @DisplayName("Should validate king safety after move")
    void shouldValidateKingSafetyAfterMove() {
        ChessMove move = new ChessMove();
        move.color = "white";

        assertTrue(safetyChecker.IsKingSafe(move));
    }

    @Test
    @DisplayName("Should detect king in check")
    void shouldDetectKingInCheck() {
        // Place attacking piece
        board.board[6][4] = null; // Remove pawn
        board.board[5][4] = new ChessPiece("Rook", "black");

        ChessMove move = new ChessMove();
        move.color = "white";

        assertFalse(safetyChecker.IsKingSafe(move));
    }

    @Test
    @DisplayName("Should handle edge cases for pawn attacks")
    void shouldHandleEdgeCasesForPawnAttacks() {
        // Test pawn attack from edge of board
        board.board[6][7] = new ChessPiece("Pawn", "black");

        assertFalse(safetyChecker.IsSquareSafeForKing("white", 7, 6));
        assertTrue(safetyChecker.IsSquareSafeForKing("white", 7, 7)); // No pawn can attack this
    }
}