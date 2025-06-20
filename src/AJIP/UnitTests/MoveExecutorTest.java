package AJIP.UnitTests;

import AJIP.Engine.MoveExecutor;
import AJIP.Model.ChessBoard;
import AJIP.Model.ChessMove;
import AJIP.Model.ChessPiece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Move Executor Tests")
class MoveExecutorTest {

    private ChessBoard board;
    private MoveExecutor executor;

    @BeforeEach
    void setUp() {
        board = new ChessBoard();
        executor = new MoveExecutor(board);
    }

    @Test
    @DisplayName("Should execute regular moves correctly")
    void shouldExecuteRegularMovesCorrectly() {
        ChessMove move = new ChessMove();
        move.toRow = 5;
        move.toCol = 0;

        ChessPiece originalPiece = board.board[6][0];
        executor.ExecuteMoveOrCapture(move, 6, 0);

        assertNull(board.board[6][0]);
        assertEquals(originalPiece, board.board[5][0]);
        assertTrue(originalPiece.IsMoved());
    }

    @Test
    @DisplayName("Should execute captures correctly")
    void shouldExecuteCapturesCorrectly() {
        // Place enemy piece
        board.board[5][0] = new ChessPiece("Pawn", "black");

        ChessMove move = new ChessMove();
        move.toRow = 5;
        move.toCol = 0;

        ChessPiece capturingPiece = board.board[6][0];
        executor.ExecuteMoveOrCapture(move, 6, 0);

        assertNull(board.board[6][0]);
        assertEquals(capturingPiece, board.board[5][0]);
        assertTrue(capturingPiece.IsMoved());
    }

    @Test
    @DisplayName("Should mark piece as moved on first move")
    void shouldMarkPieceAsMovedOnFirstMove() {
        ChessMove move = new ChessMove();
        move.toRow = 5;
        move.toCol = 0;

        ChessPiece piece = board.board[6][0];
        assertFalse(piece.IsMoved());

        executor.ExecuteMoveOrCapture(move, 6, 0);

        assertTrue(piece.IsMoved());
    }

    @Test
    @DisplayName("Should not change moved status if already moved")
    void shouldNotChangeMovedStatusIfAlreadyMoved() {
        ChessMove move = new ChessMove();
        move.toRow = 5;
        move.toCol = 0;

        ChessPiece piece = board.board[6][0];
        piece.SetMoved(); // Mark as moved

        executor.ExecuteMoveOrCapture(move, 6, 0);

        assertTrue(piece.IsMoved()); // Should still be marked as moved
    }

    @Test
    @DisplayName("Should execute kingside castling correctly")
    void shouldExecuteKingsideCastlingCorrectly() {
        // Clear path
        board.board[7][5] = null;
        board.board[7][6] = null;

        ChessMove castling = new ChessMove();
        castling.fromRow = 7;
        castling.fromCol = 4;
        castling.toRow = 7;
        castling.toCol = 6;

        ChessPiece king = board.board[7][4];
        ChessPiece rook = board.board[7][7];

        executor.ExecuteCastling(castling);

        assertEquals(king, board.board[7][6]);
        assertEquals(rook, board.board[7][5]);
        assertNull(board.board[7][4]);
        assertNull(board.board[7][7]);
        assertTrue(king.IsMoved());
        assertTrue(rook.IsMoved());
    }

    @Test
    @DisplayName("Should execute queenside castling correctly")
    void shouldExecuteQueensideCastlingCorrectly() {
        // Clear path
        board.board[7][1] = null;
        board.board[7][2] = null;
        board.board[7][3] = null;

        ChessMove castling = new ChessMove();
        castling.fromRow = 7;
        castling.fromCol = 4;
        castling.toRow = 7;
        castling.toCol = 2;

        ChessPiece king = board.board[7][4];
        ChessPiece rook = board.board[7][0];

        executor.ExecuteCastling(castling);

        assertEquals(king, board.board[7][2]);
        assertEquals(rook, board.board[7][3]);
        assertNull(board.board[7][4]);
        assertNull(board.board[7][0]);
        assertTrue(king.IsMoved());
        assertTrue(rook.IsMoved());
    }

    @Test
    @DisplayName("Should execute promotion correctly")
    void shouldExecutePromotionCorrectly() {
        ChessMove promotion = new ChessMove();
        promotion.color = "white";
        promotion.toRow = 0;
        promotion.toCol = 0;
        promotion.promotionPiece = "Queen";

        executor.ExecutePromotion(promotion, 1, 0);

        assertNull(board.board[1][0]);
        ChessPiece promotedPiece = board.board[0][0];
        assertNotNull(promotedPiece);
        assertEquals("Queen", promotedPiece.getType());
        assertEquals("white", promotedPiece.getColor());
    }

    @Test
    @DisplayName("Should default to Queen promotion when promotion piece is null")
    void shouldDefaultToQueenPromotionWhenPromotionPieceIsNull() {
        ChessMove promotion = new ChessMove();
        promotion.color = "white";
        promotion.toRow = 0;
        promotion.toCol = 0;
        promotion.promotionPiece = null; // Should default to Queen

        executor.ExecutePromotion(promotion, 1, 0);

        ChessPiece promotedPiece = board.board[0][0];
        assertEquals("Queen", promotedPiece.getType());
    }

    @Test
    @DisplayName("Should promote to specified piece type")
    void shouldPromoteToSpecifiedPieceType() {
        ChessMove promotion = new ChessMove();
        promotion.color = "black";
        promotion.toRow = 7;
        promotion.toCol = 0;
        promotion.promotionPiece = "Knight";

        executor.ExecutePromotion(promotion, 6, 0);

        ChessPiece promotedPiece = board.board[7][0];
        assertEquals("Knight", promotedPiece.getType());
        assertEquals("black", promotedPiece.getColor());
    }

    @Test
    @DisplayName("Should execute check move correctly")
    void shouldExecuteCheckMoveCorrectly() {
        ChessMove move = new ChessMove();
        move.toRow = 5;
        move.toCol = 0;

        ChessPiece movingPiece = board.board[6][0];
        board.board[5][0] = new ChessPiece("Pawn", "black"); // Place piece to capture

        executor.ExecuteCheck(move, 6, 0);

        assertEquals(movingPiece, board.board[5][0]);
        assertNull(board.board[6][0]);
    }

    @Test
    @DisplayName("Should handle move execution for different piece types")
    void shouldHandleMoveExecutionForDifferentPieceTypes() {
        // Test knight move
        ChessMove knightMove = new ChessMove();
        knightMove.toRow = 5;
        knightMove.toCol = 2;

        ChessPiece knight = board.board[7][1];
        executor.ExecuteMoveOrCapture(knightMove, 7, 1);

        assertEquals(knight, board.board[5][2]);
        assertNull(board.board[7][1]);
        assertTrue(knight.IsMoved());
    }
}
