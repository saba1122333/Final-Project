package AJIP.Engine;

import AJIP.Model.ChessBoard;
import AJIP.Model.ChessMove;
import AJIP.Model.ChessPiece;
import AJIP.Records.Position;
import AJIP.Records.ValidationResult;

public class SpecialMoveHandler {
    private final ChessBoard chessBoard;
    private final MoveValidator moveValidator;
    private final SafetyChecker safetyChecker;
    private final BoardAnalyzer boardAnalyzer;

    public SpecialMoveHandler(ChessBoard chessBoard, MoveValidator moveValidator, SafetyChecker safetyChecker, BoardAnalyzer boardAnalyzer) {
        this.chessBoard = chessBoard;
        this.moveValidator = moveValidator;
        this.safetyChecker = safetyChecker;
        this.boardAnalyzer = boardAnalyzer;
    }

    public ValidationResult CanCastle(ChessMove move) {
        // Extract the key information
        int kingRow = move.fromRow;
        int kingCol = move.fromCol;
        boolean isKingSideCastling = move.toCol == 6;
        Position from = new Position(move.fromRow, move.fromCol);
        Position to = new Position(move.toRow, move.toCol);

        // 1. Verify the king hasn't moved (using the isMoved flag)
        ChessPiece king = chessBoard.board[kingRow][kingCol];
        if (king == null || !king.getType().equals("King") || king.IsMoved()) {
            return ValidationResult.invalid(move.color, "King", from, to, "King is missing or has moved");  // King is missing or has moved
        }

        // 2. Identify and check the appropriate rook
        int rookCol = isKingSideCastling ? 7 : 0;  // H-file or A-file
        ChessPiece rook = chessBoard.board[kingRow][rookCol];
        if (rook == null || !rook.getType().equals("Rook") || rook.IsMoved()) {
            return ValidationResult.invalid(move.color, "King", from, to, "Rook is missing or has moved");  // Rook is missing or has moved
        }

        // 3. Check if the path between king and rook is clear
        int startCol = Math.min(kingCol, rookCol) + 1;
        int endCol = Math.max(kingCol, rookCol);
        for (int col = startCol; col < endCol; col++) {
            if (chessBoard.board[kingRow][col] != null) {
                return ValidationResult.invalid(move.color, "King", from, to, "Path is blocked");  // Path is blocked
            }
        }

        // 4. Check if the king's path (including destination) is safe
        int step = isKingSideCastling ? 1 : -1;
        for (int col = kingCol; col != move.toCol + step; col += step) {
            if (!safetyChecker.IsSquareSafeForKing(move.color, kingRow, col)) {
                return ValidationResult.invalid(move.color, "King", from, to, "King would move through or into check");  // King would move through or into check
            }
        }

        return ValidationResult.valid(move.color, "King", from, to);  // All castling conditions are met
    }

    public ValidationResult CanCheck(String color, String type, int fromRow, int fromCol, int toRow, int toCol) {
        // En-passant is not yet implemented
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        // 3. Find the opponent's king
        String opponentColor = color.equals("white") ? "black" : "white";
        Position kingsPosition = boardAnalyzer.GetCandidatePositions(opponentColor, "King").size() == 1 ? boardAnalyzer.GetCandidatePositions(opponentColor, "King").get(0) : null;
        if (kingsPosition == null) {
            // opponents king is not found or more than 2 enemy Kings are present at the board
            return ValidationResult.invalid(color, type, from, to, "Opponent king not found or multiple kings present");
        }
        int opponentKingRow = kingsPosition.row();
        int opponentKingCol = kingsPosition.col();

        // we assume  if we enter this board is already updated

        if (!safetyChecker.IsSquareSafeForKing(opponentColor, opponentKingRow, opponentKingCol)) {
            return ValidationResult.valid(color, type, from, to);
        }
        return ValidationResult.invalid(color, type, from, to, "Move does not create check");

    }


    public ValidationResult CanPromote(String color, String type, int fromRow, int fromCol, int toRow, int toCol) {
        // SECURITY CHECK 1: Only pawns can be promoted
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        if (!type.equals("Pawn")) {
            return ValidationResult.invalid(color, type, from, to, "Only pawns can be promoted");
        }

        // SECURITY CHECK 2: Promotion must occur on the correct rank
        int promotionRank = color.equals("white") ? 0 : 7; // Rank 8 for white, rank 1 for black
        if (toRow != promotionRank) {
            return ValidationResult.invalid(color, type, from, to, "Promotion must occur on the correct rank");
        }


        // just promotion
        if (moveValidator.canPawnMove(color, fromRow, fromCol, toRow, toCol).isValid() || moveValidator.canPawnCapture(color, fromRow, fromCol, toRow, toCol, false).isValid()) {
            return ValidationResult.valid(color, type, from, to);
        } else {
            return ValidationResult.invalid(color, type, from, to, "Invalid pawn move for promotion");
        }


    }
}
