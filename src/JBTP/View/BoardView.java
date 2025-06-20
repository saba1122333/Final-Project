package JBTP.View;

import Model.Board;
import Model.Position;

import java.util.List;

/**
 * Interface for chess board views
 */
public interface BoardView {
    /**
     * Updates the view to display the current board state
     *
     * @param board The chess board model to display
     */
    void updateBoard(Board board);

    /**
     * Sets the current player in the view
     *
     * @param color The color of the current player (as defined in PieceColor)
     */
    void setCurrentPlayer(int color);

    /**
     * Displays a game over message
     *
     * @param message The game over message to display
     */
    void showGameOverMessage(String message);

    /**
     * Highlights the squares that were part of the most recent move
     *
     * @param fromPosition The source position of the move
     * @param toPosition The destination position of the move
     */
    void highlightLastMove(Position fromPosition, Position toPosition);

    /**
     * Highlights legal moves for a selected piece
     *
     * @param selectedPosition The position of the selected piece
     * @param legalMovePositions List of positions where the piece can legally move
     */
    void showLegalMoves(Position selectedPosition, List<Position> legalMovePositions);

    /**
     * Highlights a king that is in check
     *
     * @param kingPosition The position of the king in check
     * @param isInCheck Whether the king is in check
     */
    void showCheck(Position kingPosition, boolean isInCheck);

    /**
     * Updates the game status display
     *
     * @param statusMessage The status message to display
     */
    void updateStatus(String statusMessage);

    /**
     * Clears all visual highlights from the board
     */
    void clearHighlights();

}