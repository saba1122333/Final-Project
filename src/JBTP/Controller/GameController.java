package JBTP.Controller;

import JBTP.Model.*;
import JBTP.View.GameWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main controller for the chess game that connects the model and view.
 * Handles game logic, user interactions, and time control.
 */
public class GameController {
    private GameState gameState;
    private GameWindow view;
    private String gameMode;
    private Timer clockTimer;
    private boolean clockRunning;

    /**
     * Constructs a new GameController.
     */
    public GameController() {
        this.gameState = new GameState();
        this.clockRunning = false;
    }

    /**
     * Sets the game view.
     *
     * @param view The GameWindow view
     */
    public void setView(GameWindow view) {
        this.view = view;
    }

    /**
     * Starts a new game with the specified game mode.
     *
     * @param gameMode The game mode to use
     */
    public void startNewGame(String gameMode) {
        this.gameMode = gameMode;
        gameState.resetGame();

        // Stop any existing timer
        stopClock();

        // Start the clock if it's a timed game
        if (gameState.isTimedGame()) {
            startClock();
        }

        updateView();

        // If playing against computer and computer goes first, make computer move
        if (gameMode.equals("Computer vs Computer") ||
                (gameMode.equals("Player vs Computer") && gameState.getCurrentPlayerColor() == PieceColor.BLACK)) {
            makeComputerMove();
        }
    }

    /**
     * Starts a new timed game with the specified mode and time control.
     *
     * @param gameMode The game mode to use
     * @param hours Hours for each player's clock
     * @param minutes Minutes for each player's clock
     * @param seconds Seconds for each player's clock
     */
    public void startTimedGame(String gameMode, int hours, int minutes, int seconds) {
        this.gameMode = gameMode;
        gameState = new GameState(hours, minutes, seconds);

        // Start the clock
        startClock();

        updateView();

        // If playing against computer and computer goes first, make computer move
        if (gameMode.equals("Computer vs Computer") ||
                (gameMode.equals("Player vs Computer") && gameState.getCurrentPlayerColor() == PieceColor.BLACK)) {
            makeComputerMove();
        }
    }

    /**
     * Makes a move from the source position to the target position.
     *
     * @param fromPosition The source position
     * @param toPosition The target position
     * @return True if the move was successful, false otherwise
     */
    public boolean makeMove(Position fromPosition, Position toPosition) {
        // Get the piece at the source position
        Board board = gameState.getBoard();
        Piece piece = board.getPiece(fromPosition);

        if (piece == null || piece.getColor() != gameState.getCurrentPlayerColor()) {
            return false;
        }

        // Find a legal move that matches the from and to positions
        List<Move> legalMoves = getLegalMovesForPiece(piece);
        Move moveToMake = null;

        for (Move move : legalMoves) {
            if (move.getOrigin().equals(fromPosition) && move.getDestination().equals(toPosition)) {
                moveToMake = move;
                break;
            }
        }

        if (moveToMake == null) {
            return false;
        }

        // Make the move
        boolean successful = gameState.makeMove(moveToMake);

        if (successful) {
            updateView();

            // Check if the game is over
            if (gameState.isGameOver()) {
                stopClock();
                view.showGameOver(gameState.getGameResult());
                return true;
            }

            // If playing against computer, make computer move
            if ((gameMode.equals("Player vs Computer") && gameState.getCurrentPlayerColor() == PieceColor.BLACK) ||
                    gameMode.equals("Computer vs Computer")) {
                makeComputerMove();
            }
        }

        return successful;
    }

    /**
     * Makes a computer move using a simple AI.
     */
    private void makeComputerMove() {
        // Simple AI: choose a random legal move
        List<Move> legalMoves = getAllLegalMoves(gameState.getCurrentPlayerColor());

        if (!legalMoves.isEmpty()) {
            // For now, just pick the first legal move
            // This could be improved with a proper chess AI
            Move computerMove = legalMoves.get(0);

            // Make the move after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(500); // 0.5 second delay
                    gameState.makeMove(computerMove);
                    updateView();

                    // Check if the game is over
                    if (gameState.isGameOver()) {
                        stopClock();
                        view.showGameOver(gameState.getGameResult());
                    } else if (gameMode.equals("Computer vs Computer")) {
                        // Continue with next computer move
                        makeComputerMove();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Updates the view to reflect the current game state.
     */
    private void updateView() {
        if (view != null) {
            view.refreshBoard();

            // Update status message
            String statusMessage = PieceColor.colorName(gameState.getCurrentPlayerColor()) + "'s turn";

            // Add check notification if applicable
            if (gameState.isInCheck(gameState.getCurrentPlayerColor())) {
                statusMessage += " (CHECK)";
            }

            // Add time information for timed games
            if (gameState.isTimedGame()) {
                String whiteTime = gameState.getClock(PieceColor.WHITE).getTime();
                String blackTime = gameState.getClock(PieceColor.BLACK).getTime();
                statusMessage += " | White: " + whiteTime + " | Black: " + blackTime;
            }

            view.updateStatus(statusMessage);
        }
    }

    /**
     * Gets the piece at the specified position.
     *
     * @param position The position to check
     * @return The piece at the position, or null if empty
     */
    public Piece getPieceAt(Position position) {
        return gameState.getBoard().getPiece(position);
    }

    /**
     * Gets all legal moves for the piece at the specified position.
     *
     * @param position The position of the piece
     * @return A list of positions representing legal moves
     */
    public List<Position> getLegalMovePositions(Position position) {
        Piece piece = getPieceAt(position);

        if (piece == null) {
            return new ArrayList<>();
        }

        List<Move> legalMoves = getLegalMovesForPiece(piece);
        List<Position> movePositions = new ArrayList<>();

        for (Move move : legalMoves) {
            movePositions.add(move.getDestination());
        }

        return movePositions;
    }

    /**
     * Gets all legal moves for the specified piece.
     *
     * @param piece The piece to check
     * @return A list of legal moves
     */
    private List<Move> getLegalMovesForPiece(Piece piece) {
        return piece.getLegalMoves(gameState.getBoard());
    }

    /**
     * Gets all legal moves for the specified color.
     *
     * @param color The color to get moves for
     * @return A list of all legal moves
     */
    private List<Move> getAllLegalMoves(int color) {
        return gameState.getBoard().getAllLegalMoves(color);
    }

    /**
     * Forfeits the current game for the current player.
     */
    public void surrender() {
        gameState.surrender();
        stopClock();
        view.showGameOver(gameState.getGameResult());
    }

    /**
     * Gets the current board state.
     *
     * @return The chess board
     */
    public Board getBoard() {
        return gameState.getBoard();
    }

    /**
     * Gets the color of the player whose turn it is.
     *
     * @return The color of the current player
     */
    public int getCurrentTurn() {
        return gameState.getCurrentPlayerColor();
    }

    /**
     * Checks if the game is over.
     *
     * @return True if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    /**
     * Sets time control for the game.
     *
     * @param hours Hours for each player's clock
     * @param minutes Minutes for each player's clock
     * @param seconds Seconds for each player's clock
     */
    public void setTimeControl(int hours, int minutes, int seconds) {
        gameState.setTimeControl(hours, minutes, seconds);
        if (gameState.isTimedGame() && !clockRunning) {
            startClock();
        }
        updateView();
    }

    /**
     * Starts the chess clock timer.
     */
    private void startClock() {
        if (clockTimer != null) {
            clockTimer.cancel();
        }

        clockTimer = new Timer();
        clockRunning = true;

        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameState.decrementCurrentPlayerClock()) {
                    // Time ran out
                    stopClock();
                    view.showGameOver(gameState.getGameResult());
                }
                updateView();
            }
        }, 1000, 1000); // Update every second
    }

    /**
     * Stops the chess clock timer.
     */
    private void stopClock() {
        if (clockTimer != null) {
            clockTimer.cancel();
            clockTimer = null;
            clockRunning = false;
        }
    }

    /**
     * Pauses or resumes the game clock.
     *
     * @return True if clock is now running, false if paused
     */
    public boolean toggleClockPause() {
        if (clockRunning) {
            stopClock();
        } else if (gameState.isTimedGame() && !gameState.isGameOver()) {
            startClock();
        }
        return clockRunning;
    }
}