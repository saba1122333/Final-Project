package JBTP.View;

import Model.Piece;
import Model.PieceColor;
import Model.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChessSquareView extends JPanel {
    private static final Color LIGHT_SQUARE = new Color(240, 217, 181);
    private static final Color DARK_SQUARE = new Color(181, 136, 99);
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 0, 100);
    private static final Color SELECTED_COLOR = new Color(0, 255, 0, 100);
    private static final Color LEGAL_MOVE_COLOR = new Color(0, 0, 255, 100);
    private static final Color CHECK_COLOR = new Color(255, 0, 0, 100);

    // Unicode chess symbols
    private static final String WHITE_KING = "♔";
    private static final String WHITE_QUEEN = "♕";
    private static final String WHITE_ROOK = "♖";
    private static final String WHITE_BISHOP = "♗";
    private static final String WHITE_KNIGHT = "♘";
    private static final String WHITE_PAWN = "♙";
    private static final String BLACK_KING = "♚";
    private static final String BLACK_QUEEN = "♛";
    private static final String BLACK_ROOK = "♜";
    private static final String BLACK_BISHOP = "♝";
    private static final String BLACK_KNIGHT = "♞";
    private static final String BLACK_PAWN = "♟";

    private Position position;
    private Piece piece;
    private boolean isLightSquare;
    private boolean isHighlighted;
    private boolean isSelected;
    private boolean isLegalMove;
    private boolean isInCheck;
    private ChessBoardUI boardUI;

    public ChessSquareView(Position position, boolean isLight, ChessBoardUI boardUI) {
        this.position = position;
        this.isLightSquare = isLight;
        this.boardUI = boardUI;
        this.isHighlighted = false;
        this.isSelected = false;
        this.isLegalMove = false;
        this.isInCheck = false;

        setPreferredSize(new Dimension(60, 60));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setOpaque(true);
        setBackground(isLight ? LIGHT_SQUARE : DARK_SQUARE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boardUI.handleSquareClick(position);
            }
        });
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        repaint();
    }

    public void setHighlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
        repaint();
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }

    public void setLegalMove(boolean legalMove) {
        this.isLegalMove = legalMove;
        repaint();
    }

    public void setInCheck(boolean inCheck) {
        this.isInCheck = inCheck;
        repaint();
    }

    public void clearState() {
        this.isHighlighted = false;
        this.isSelected = false;
        this.isLegalMove = false;
        this.isInCheck = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw base square color
        g.setColor(isLightSquare ? LIGHT_SQUARE : DARK_SQUARE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw highlights
        if (isHighlighted) {
            g.setColor(HIGHLIGHT_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (isSelected) {
            g.setColor(SELECTED_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (isLegalMove) {
            g.setColor(LEGAL_MOVE_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (isInCheck) {
            g.setColor(CHECK_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // Draw piece
        if (piece != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Set font size for the chess piece
            Font font = new Font("Arial Unicode MS", Font.PLAIN, 40);
            g2d.setFont(font);

            // Get piece symbol
            String symbol = getUnicodeSymbol(piece);

            // Calculate centering
            FontMetrics metrics = g2d.getFontMetrics(font);
            int x = (getWidth() - metrics.stringWidth(symbol)) / 2;
            int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

            // Draw the piece symbol
            g2d.setColor(Color.BLACK);
            g2d.drawString(symbol, x, y);
        }
    }

    private String getUnicodeSymbol(Piece piece) {
        if (piece == null) return "";

        String type = piece.getType();
        boolean isWhite = piece.getColor() == PieceColor.WHITE;

        switch (type) {
            case "King":
                return isWhite ? WHITE_KING : BLACK_KING;
            case "Queen":
                return isWhite ? WHITE_QUEEN : BLACK_QUEEN;
            case "Rook":
                return isWhite ? WHITE_ROOK : BLACK_ROOK;
            case "Bishop":
                return isWhite ? WHITE_BISHOP : BLACK_BISHOP;
            case "Knight":
                return isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
            case "Pawn":
                return isWhite ? WHITE_PAWN : BLACK_PAWN;
            default:
                return type; // Fallback to text representation
        }
    }
}