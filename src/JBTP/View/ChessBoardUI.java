package JBTP.View;

import Controller.GameController;
import Model.Board;
import Model.Piece;
import Model.Position;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChessBoardUI extends JPanel implements BoardView {
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 60;

    private ChessSquareView[][] squares;
    private GameController controller;
    private Position selectedPosition;
    private List<Position> legalMovePositions;
    private boolean isFlipped = false;

    public ChessBoardUI(GameController controller) {
        this.controller = controller;
        setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE, BOARD_SIZE * SQUARE_SIZE));

        squares = new ChessSquareView[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isLight = (row + col) % 2 != 0;
                Position position = new Position(col, row);
                ChessSquareView square = new ChessSquareView(position, isLight, this);
                squares[row][col] = square;
                add(square);
            }
        }
    }

    public void updateBoard() {
        Board board = controller.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Position position = new Position(col, row);
                Piece piece = board.getPiece(position);

                int displayRow = isFlipped ? (BOARD_SIZE - 1 - row) : row;
                int displayCol = isFlipped ? (BOARD_SIZE - 1 - col) : col;

                squares[displayRow][displayCol].setPiece(piece);
            }
        }
        repaint();
    }

    @Override
    public void updateBoard(Board board) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Position position = new Position(col, row);
                Piece piece = board.getPiece(position);

                int displayRow = isFlipped ? (BOARD_SIZE - 1 - row) : row;
                int displayCol = isFlipped ? (BOARD_SIZE - 1 - col) : col;

                squares[displayRow][displayCol].setPiece(piece);
            }
        }
        repaint();
    }

    @Override
    public void setCurrentPlayer(int color) {
        // Implement if needed - could highlight player's pieces or update status
    }

    @Override
    public void showGameOverMessage(String message) {
        // GameWindow handles this with JOptionPane
    }

    @Override
    public void highlightLastMove(Position fromPosition, Position toPosition) {
        // Reset previous highlights
        clearHighlights();

        // Apply new highlights
        if (fromPosition != null) {
            getSquareView(fromPosition).setHighlighted(true);
        }
        if (toPosition != null) {
            getSquareView(toPosition).setHighlighted(true);
        }
    }

    @Override
    public void showLegalMoves(Position selectedPosition, List<Position> legalMovePositions) {
        this.selectedPosition = selectedPosition;
        this.legalMovePositions = legalMovePositions;

        // Highlight the selected square
        if (selectedPosition != null) {
            getSquareView(selectedPosition).setSelected(true);
        }

        // Highlight legal move squares
        if (legalMovePositions != null) {
            for (Position pos : legalMovePositions) {
                getSquareView(pos).setLegalMove(true);
            }
        }

        repaint();
    }

    @Override
    public void showCheck(Position kingPosition, boolean isInCheck) {
        if (kingPosition != null && isInCheck) {
            getSquareView(kingPosition).setInCheck(true);
        }
    }

    @Override
    public void updateStatus(String statusMessage) {
        // GameWindow handles this
    }

    @Override
    public void clearHighlights() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                squares[row][col].clearState();
            }
        }
        repaint();
    }

    public void clearSelection() {
        selectedPosition = null;
        legalMovePositions = null;
        clearHighlights();
    }

    public void handleSquareClick(Position position) {
        if (selectedPosition == null) {
            // No piece selected yet - check if this square has a piece of the current player
            Piece piece = controller.getPieceAt(position);
            if (piece != null && piece.getColor() == controller.getCurrentTurn()) {
                // Select this piece and show its legal moves
                List<Position> legalMoves = controller.getLegalMovePositions(position);
                showLegalMoves(position, legalMoves);
            }
        } else {
            // A piece is already selected
            if (position.equals(selectedPosition)) {
                // Clicking the same piece again - deselect it
                clearSelection();
            } else if (legalMovePositions != null && legalMovePositions.contains(position)) {
                // Clicking a legal move destination - move the piece
                controller.makeMove(selectedPosition, position);
                clearSelection();
            } else {
                // Clicking a different square - check if it's a new piece of the current player
                Piece piece = controller.getPieceAt(position);
                if (piece != null && piece.getColor() == controller.getCurrentTurn()) {
                    // Select this new piece instead
                    clearSelection();
                    List<Position> legalMoves = controller.getLegalMovePositions(position);
                    showLegalMoves(position, legalMoves);
                } else {
                    // Invalid move - just deselect
                    clearSelection();
                }
            }
        }
    }

    private ChessSquareView getSquareView(Position position) {
        int row = position.getRow();
        int col = position.getColumn();

        if (isFlipped) {
            row = BOARD_SIZE - 1 - row;
            col = BOARD_SIZE - 1 - col;
        }

        return squares[row][col];
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public void setFlipped(boolean flipped) {
        this.isFlipped = flipped;
        removeAll();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int displayRow = flipped ? (BOARD_SIZE - 1 - row) : row;
                int displayCol = flipped ? (BOARD_SIZE - 1 - col) : col;

                add(squares[displayRow][displayCol]);
            }
        }

        updateBoard();
        revalidate();
        repaint();
    }
}