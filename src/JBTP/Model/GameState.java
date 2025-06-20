package JBTP.Model;

import java.util.List;

/**
 * Represents the current state of a chess game, including board position,
 * player turns, game history, timers, and end conditions.
 */
public class GameState {
    private Board chessBoard;
    private int currentPlayerColor;
    private boolean gameOver;
    private String result;
    private int nonCaptureMoveCounter; // For 50-move rule
    private int moveNumber;
    private Clock whiteTimer;
    private Clock blackTimer;
    private boolean timedGame;

    /**
     * Constructs a new chess game state with default settings.
     */
    public GameState() {
        this.chessBoard = new Board();
        this.currentPlayerColor = PieceColor.WHITE;
        this.gameOver = false;
        this.result = "";
        this.nonCaptureMoveCounter = 0;
        this.moveNumber = 1;
        // Default time of 15 minutes per player
        this.whiteTimer = new Clock(0, 15, 0);
        this.blackTimer = new Clock(0, 15, 0);
        this.timedGame = false;
    }

    /**
     * Constructs a chess game state with custom time controls.
     *
     * @param hours Hours for each player's clock
     * @param minutes Minutes for each player's clock
     * @param seconds Seconds for each player's clock
     */
    public GameState(int hours, int minutes, int seconds) {
        this();
        this.whiteTimer = new Clock(hours, minutes, seconds);
        this.blackTimer = new Clock(hours, minutes, seconds);
        this.timedGame = true;
    }

    /**
     * Gets the current chess board.
     *
     * @return The current board state
     */
    public Board getBoard() {
        return chessBoard;
    }

    /**
     * Gets the color of the player whose turn it is.
     *
     * @return The current player's color
     */
    public int getCurrentPlayerColor() {
        return currentPlayerColor;
    }

    /**
     * Checks if the game has ended.
     *
     * @return True if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Gets the result of the game if it has ended.
     *
     * @return A string describing the game result
     */
    public String getGameResult() {
        return result;
    }

    /**
     * Gets the clock for the specified player color.
     *
     * @param color The player's color
     * @return The player's clock
     */
    public Clock getClock(int color) {
        return (color == PieceColor.WHITE) ? whiteTimer : blackTimer;
    }

    /**
     * Checks if time controls are active for this game.
     *
     * @return True if the game uses time controls
     */
    public boolean isTimedGame() {
        return timedGame;
    }

    /**
     * Enables or disables time controls for this game.
     *
     * @param timedGame True to enable time controls
     */
    public void setTimedGame(boolean timedGame) {
        this.timedGame = timedGame;
    }

    /**
     * Sets custom time controls for both players.
     *
     * @param hours Hours for each player's clock
     * @param minutes Minutes for each player's clock
     * @param seconds Seconds for each player's clock
     */
    public void setTimeControl(int hours, int minutes, int seconds) {
        this.whiteTimer = new Clock(hours, minutes, seconds);
        this.blackTimer = new Clock(hours, minutes, seconds);
        this.timedGame = true;
    }

    /**
     * Decrements the clock of the current player.
     * Should be called regularly when the game is in progress.
     *
     * @return True if the player has run out of time
     */
    public boolean decrementCurrentPlayerClock() {
        if (!timedGame || gameOver) {
            return false;
        }

        Clock activeClock = (currentPlayerColor == PieceColor.WHITE) ? whiteTimer : blackTimer;
        activeClock.decr();

        if (activeClock.outOfTime()) {
            gameOver = true;
            int winner = PieceColor.opponent(currentPlayerColor);
            result = PieceColor.colorName(winner) + " wins on time";
            return true;
        }
        return false;
    }

    /**
     * Attempts to make a move on the board.
     *
     * @param move The move to execute
     * @return True if the move was successful
     */
    public boolean makeMove(Move move) {
        // Verify it's the moving piece's turn
        if (move.getMovingPiece().getColor() != currentPlayerColor) {
            return false;
        }

        // Execute the move on the board
        boolean moveSuccessful = chessBoard.executeMove(move);
        if (!moveSuccessful) {
            return false;
        }

        // Update move counters
        if (move.getMovingPiece().getType().equals("Pawn") || move.getTakenPiece() != null) {
            nonCaptureMoveCounter = 0;
        } else {
            nonCaptureMoveCounter++;
        }

        // Update full move number when Black moves
        if (currentPlayerColor == PieceColor.BLACK) {
            moveNumber++;
        }

        // Switch active player
        currentPlayerColor = PieceColor.opponent(currentPlayerColor);

        // Check game end conditions
        checkEndConditions();

        return true;
    }

    /**
     * Checks various endgame conditions.
     */
    private void checkEndConditions() {
        // Check for checkmate
        if (chessBoard.isCheckmate(currentPlayerColor)) {
            gameOver = true;
            int winner = PieceColor.opponent(currentPlayerColor);
            result = PieceColor.colorName(winner) + " wins by checkmate";
        }
        // Check for stalemate
        else if (chessBoard.isStalemate(currentPlayerColor)) {
            gameOver = true;
            result = "Draw by stalemate";
        }
        // Check for 50-move rule
        else if (nonCaptureMoveCounter >= 100) { // 50 moves = 100 half-moves
            gameOver = true;
            result = "Draw by 50-move rule";
        }
        // Check for insufficient material (simplified implementation)
        else if (isInsufficientMaterial()) {
            gameOver = true;
            result = "Draw by insufficient material";
        }
    }

    /**
     * Checks if there is insufficient material for checkmate.
     * Simplified version that only checks the most common cases.
     *
     * @return True if there is insufficient material
     */
    private boolean isInsufficientMaterial() {
        // Get both player's pieces
        List<Piece> whitePieces = chessBoard.getPiecesByColor(PieceColor.WHITE);
        List<Piece> blackPieces = chessBoard.getPiecesByColor(PieceColor.BLACK);

        // King vs King
        if (whitePieces.size() == 1 && blackPieces.size() == 1) {
            return true;
        }

        // King + Knight vs King or King + Bishop vs King
        if ((whitePieces.size() == 2 && blackPieces.size() == 1) ||
                (whitePieces.size() == 1 && blackPieces.size() == 2)) {

            List<Piece> sideWithTwoPieces = (whitePieces.size() == 2) ? whitePieces : blackPieces;

            // Check if second piece is either Knight or Bishop
            for (Piece piece : sideWithTwoPieces) {
                if (!piece.getType().equals("King")) {
                    return piece.getType().equals("Knight") || piece.getType().equals("Bishop");
                }
            }
        }

        return false;
    }

    /**
     * Resets the game to its initial state.
     */
    public void resetGame() {
        this.chessBoard = new Board();
        this.currentPlayerColor = PieceColor.WHITE;
        this.gameOver = false;
        this.result = "";
        this.nonCaptureMoveCounter = 0;
        this.moveNumber = 1;

        // Reset timers if it's a timed game
        if (timedGame) {
            // Get current time settings
            int hours = whiteTimer.getHours();
            int minutes = whiteTimer.getMinutes();
            int seconds = whiteTimer.getSeconds();

            // Create new timers with the same settings
            this.whiteTimer = new Clock(hours, minutes, seconds);
            this.blackTimer = new Clock(hours, minutes, seconds);
        }
    }

    /**
     * Surrenders the game for the current player.
     */
    public void surrender() {
        gameOver = true;
        int winner = PieceColor.opponent(currentPlayerColor);
        result = PieceColor.colorName(winner) + " wins by resignation";
    }

    /**
     * Checks if the king of a specific color is in check.
     *
     * @param color The color to check
     * @return True if the specified king is in check
     */
    public boolean isInCheck(int color) {
        return chessBoard.isKingInCheck(color);
    }
}