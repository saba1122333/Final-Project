package AJIP.Controller;

import AJIP.Model.ChessBoard;
import AJIP.Engine.*;
import AJIP.Model.ChessGame;
import AJIP.Model.ChessMove;
import AJIP.Records.Position;
import AJIP.Records.ValidationResult;
import AJIP.Logger.*;


import java.util.List;

public class GameMasterController {
    ChessBoard chessBoard;
    final boolean forMultipleGames;
    StringBuilder errorReport;

    // Extracted components
    private final MoveValidator moveValidator;
    private final SafetyChecker safetyChecker;
    private final SpecialMoveHandler specialMoveHandler;
    private final BoardAnalyzer boardAnalyzer;
    private final MoveExecutor moveExecutor;

    public GameMasterController(boolean forMultipleGames) {
        this.chessBoard = new ChessBoard();
        this.forMultipleGames = forMultipleGames;
        // Initialize components
        this.moveValidator = new MoveValidator(chessBoard);
        this.boardAnalyzer = new BoardAnalyzer(chessBoard);
        this.safetyChecker = new SafetyChecker(chessBoard, moveValidator, boardAnalyzer);
        this.specialMoveHandler = new SpecialMoveHandler(chessBoard, moveValidator, safetyChecker, boardAnalyzer);
        this.moveExecutor = new MoveExecutor(chessBoard);
        moveValidator.setSafetyChecker(safetyChecker);
    }

    public void Evaluate(ChessGame chessGame) {
        boolean isGameCorrupt = false;
        List<ChessMove> chessMoveList = chessGame.getMoves();
        String movesText = chessGame.getOriginalText();

        GameLogger.info("Started evolution of game: " + movesText);

        for (ChessMove move : chessMoveList) {
            if (!MakeMove(move, movesText)) {
                isGameCorrupt = true;
                break;
            }
        }

        if (!isGameCorrupt) {
            GameLogger.info("Evaluation was successful " + movesText);
        }
//        chessBoard.logBoard();

        if (forMultipleGames) {
            chessBoard.ResetBoard();
        }
    }

    private boolean MakeMove(ChessMove move, String movesText) {
        boolean moveExecuted = false;
        errorReport = new StringBuilder();
        var candidatePositions = boardAnalyzer.GetCandidatePositions(move.color, move.pieceType);

        if (candidatePositions.isEmpty()) {
            errorReport = new StringBuilder("No " + move.color + " candidate found on the board for: " + move.notation +
//                    "\n" + "Terminating evaluation visualizing last position. MovesText: " + movesText);
            "\n" + "Terminating evaluation. MovesText: " + movesText);

            GameLogger.error(errorReport.toString());
            return false;
        }

        for (Position candidateLocation : candidatePositions) {
            int fromRow = candidateLocation.row();
            int fromCol = candidateLocation.col();

            // Apply disambiguation filters
            if (move.disambiguationFile != null && move.fromCol != fromCol) {
                continue;
            }
            if (move.disambiguationRank != null && move.fromRow != fromRow) {
                continue;
            }

            // Validate the complete move considering all its aspects
            ValidationResult result = validateCompleteMove(move, fromRow, fromCol);

            if (!result.isValid()) {
                errorReport.append(result.getLoggerMessage()).append("; \n ");
                continue; // Try next candidate
            }

            // Check king safety before executing move
            if (!safetyChecker.IsKingSafe(move)) {
                errorReport.append("Move would leave king in check; ");
                break; // Try next candidate
            }

            // Execute the move based on its type
            moveExecuted = true;
            var log = result.getLoggerMessage();
            break; // Move successfully executed, exit loop
        }

        if (!moveExecuted) {
            GameLogger.error("Failed to execute move: " + move.notation + ". Errors: \n " +
                   errorReport.toString() +//  "Terminating evaluation visualizing last position. MovesText: " + movesText);
                    "\n" + "Terminating evaluation. MovesText: " + movesText);

        }

        return moveExecuted;
    }

    private ValidationResult validateCompleteMove(ChessMove move, int fromRow, int fromCol) {
        // Handle castling first as it's a special case
        if (move.isCastling) {
            ValidationResult castlingResult = specialMoveHandler.CanCastle(move);
            if (!castlingResult.isValid()) {
                return castlingResult;
            }
            moveExecutor.ExecuteCastling(move);

            // Continue to check for other flags like isCheck
            if (move.isCheck) {
                return specialMoveHandler.CanCheck(move.color, move.pieceType,
                        move.fromRow, move.fromCol, move.toRow, move.toCol);
            }
            return castlingResult;
        }

        // Handle promotion (executes immediately to change board state)
        if (move.isPromotion) {
            ValidationResult promotionResult = specialMoveHandler.CanPromote(
                    move.color, move.pieceType, fromRow, fromCol, move.toRow, move.toCol);
            if (!promotionResult.isValid()) {
                return promotionResult;
            }
            moveExecutor.ExecutePromotion(move, fromRow, fromCol);

            // If promotion claims check, validate on the new board state
            if (move.isCheck) {
                return specialMoveHandler.CanCheck(move.color, move.promotionPiece, // Assuming Queen promotion
                        fromRow, fromCol, move.toRow, move.toCol);
            }
            return promotionResult;
        }

        // Handle regular moves/captures
        ValidationResult baseResult;
        if (move.isCapture) {
            baseResult = moveValidator.CanCapture(move.color, move.pieceType,
                    fromRow, fromCol, move.toRow, move.toCol, false);
        } else {
            baseResult = moveValidator.CanMove(move.color, move.pieceType,
                    fromRow, fromCol, move.toRow, move.toCol);
        }

        if (!baseResult.isValid()) {
            return baseResult;
        }

        moveExecutor.ExecuteMoveOrCapture(move, fromRow, fromCol);

        // Check validation happens on the final board state
        if (move.isCheck) {
            return specialMoveHandler.CanCheck(move.color, move.pieceType,
                    fromRow, fromCol, move.toRow, move.toCol);
        }

        return baseResult;
    }
}