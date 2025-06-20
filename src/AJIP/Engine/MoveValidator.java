package AJIP.Engine;

import AJIP.Model.ChessBoard;
import AJIP.Model.ChessPiece;
import AJIP.Records.Position;
import AJIP.Records.ValidationResult;

public class MoveValidator {
    private final ChessBoard chessBoard;
    private SafetyChecker safetyChecker;

    public MoveValidator(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }

    public void setSafetyChecker(SafetyChecker safetyChecker) {
        this.safetyChecker = safetyChecker;
    }

    public ValidationResult CanMove(String color, String type, int fromRow, int fromCol, int toRow, int toCol) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        return switch (type) {
            case "Pawn" -> canPawnMove(color, fromRow, fromCol, toRow, toCol);
            case "King" -> canKingMove(color, fromRow, fromCol, toRow, toCol,false);
            case "Queen" -> canQueenMove(color, fromRow, fromCol, toRow, toCol,false);
            case "Bishop" -> canBishopMove(color, fromRow, fromCol, toRow, toCol,false);
            case "Knight" -> canKnightMove(color, fromRow, fromCol, toRow, toCol,false);
            case "Rook" -> canRookMove(color, fromRow, fromCol, toRow, toCol,false);
            default -> ValidationResult.invalid(color, type, from, to, "Unknown piece type");
        };
    }

    public ValidationResult CanCapture(String color, String type, int fromRow, int fromCol, int toRow, int toCol, boolean isEnPassant) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        return switch (type) {
            case "Pawn" -> canPawnCapture(color, fromRow, fromCol, toRow, toCol, isEnPassant);
            case "King" -> canKingMove(color, fromRow, fromCol, toRow, toCol, true);
            case "Queen" -> canQueenMove(color, fromRow, fromCol, toRow, toCol, true);
            case "Bishop" -> canBishopMove(color, fromRow, fromCol, toRow, toCol, true);
            case "Knight" -> canKnightMove(color, fromRow, fromCol, toRow, toCol, true);
            case "Rook" -> canRookMove(color, fromRow, fromCol, toRow, toCol, true);
            default -> ValidationResult.invalid(color, type, from, to, "Unknown piece type");
        };
    }

    public ValidationResult canPawnMove(String color, int fromRow, int fromCol, int toRow, int toCol) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        // Cannot stay in place
        if (fromRow == toRow && fromCol == toCol) {
            return ValidationResult.invalid(color, "Pawn", from, to, "Pawn cannot stay in place");
        }

        // Must move in same column (no diagonal moves for regular movement)
        if (fromCol != toCol) {
            return ValidationResult.invalid(color, "Pawn", from, to, "Pawn cannot move diagonally");
        }

        // Determine movement direction based on color
        int direction = color.equals("white") ? -1 : 1;  // White moves up (-1), Black moves down (+1)
        int moveDistance = (toRow - fromRow) * direction;

        // Must move forward
        if (moveDistance <= 0) {
            return ValidationResult.invalid(color, "Pawn", from, to, "Pawn cannot move backward");
        }

        ChessPiece pawn = chessBoard.board[fromRow][fromCol];

        // Validate move distance (1 or 2 squares)
        if (moveDistance > 2 || (moveDistance == 2 && pawn.IsMoved())) {
            return ValidationResult.invalid(color, "Pawn", from, to, "Invalid pawn move distance");
        }

        // Check for clear path
        if (moveDistance == 1) {
            // One-square move: check destination is empty
            if (chessBoard.board[toRow][toCol] != null) {
                return ValidationResult.invalid(color, "Pawn", from, to, "Destination square occupied");
            }
        } else {
            // Two-square move: check both intermediate and destination squares are empty
            int intermediateRow = fromRow + direction;
            if (chessBoard.board[intermediateRow][toCol] != null || chessBoard.board[toRow][toCol] != null) {
                return ValidationResult.invalid(color, "Pawn", from, to, "Path blocked");
            }
        }

        return ValidationResult.valid(color, "Pawn", from, to);
    }

    public ValidationResult canPawnCapture(String color, int fromRow, int fromCol, int toRow, int toCol, boolean isEnPassant) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        // Must be exactly 1 square diagonally
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (rowDiff != 1 || colDiff != 1) {
            return ValidationResult.invalid(color, "Pawn", from, to, "Pawn capture must be diagonal");
        }

        // Must move in correct direction
        int direction = color.equals("white") ? -1 : 1;
        if ((toRow - fromRow) * direction <= 0) {
            return ValidationResult.invalid(color, "Pawn", from, to, "Pawn cannot capture backward");
        }

        // Check for en passant capture
        if (isEnPassant) {
            // For en passant, the captured pawn is on the same row as the capturing pawn
            ChessPiece capturedPawn = chessBoard.board[fromRow][toCol];
            if (capturedPawn == null || !capturedPawn.getType().equals("Pawn") ||
                    capturedPawn.getColor().equals(color)) {
                return ValidationResult.invalid(color, "Pawn", from, to, "Invalid en passant capture");
            }
            return ValidationResult.valid(color, "Pawn", from, to);
        }

        // Regular capture: verify there's an opponent's piece at destination
        ChessPiece targetPiece = chessBoard.board[toRow][toCol];
        if (targetPiece == null || targetPiece.getColor().equals(color)) {
            return ValidationResult.invalid(color, "Pawn", from, to, "No enemy piece to capture");
        }

        return ValidationResult.valid(color, "Pawn", from, to);
    }

    public ValidationResult canRookMove(String color, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Cannot stay in place
        if (rowDiff == 0 && colDiff == 0) {
            return ValidationResult.invalid(color, "Rook", from, to, "Rook cannot stay in place");
        }

        // Must move in straight line (horizontal or vertical)
        if (rowDiff != 0 && colDiff != 0) {
            return ValidationResult.invalid(color, "Rook", from, to, "Rook must move in straight line");
        }

        // Check horizontal movement
        if (fromRow == toRow) {
            int startCol = Math.min(fromCol, toCol) + 1;
            int endCol = Math.max(fromCol, toCol);
            for (int col = startCol; col < endCol; col++) {
                if (chessBoard.board[fromRow][col] != null) {
                    return ValidationResult.invalid(color, "Rook", from, to, "Path blocked");
                }
            }
        }


        // Check vertical movement
        if (fromCol == toCol) {
            int startRow = Math.min(fromRow, toRow) + 1;
            int endRow = Math.max(fromRow, toRow);
            for (int row = startRow; row < endRow; row++) {
                if (chessBoard.board[row][fromCol] != null) {
                    return ValidationResult.invalid(color, "Rook", from, to, "Path blocked");
                }
            }
        }

        // Cannot capture own piece
        ChessPiece targetPiece = chessBoard.board[toRow][toCol];

        if (isCapture) {
            if (targetPiece == null || targetPiece.getColor().equals(color)) {
                return ValidationResult.invalid(color, "Rook", from, to, "No enemy piece to capture");
            }
        } else {
            if (targetPiece != null) {
                return ValidationResult.invalid(color, "Rook", from, to, "Cannot move In square is occupied");
            }
        }


        return ValidationResult.valid(color, "Rook", from, to);
    }

    public ValidationResult canBishopMove(String color, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Cannot stay in place
        if (rowDiff == 0 && colDiff == 0) {
            return ValidationResult.invalid(color, "Bishop", from, to, "Bishop cannot stay in place");
        }

        // Must be diagonal movement
        if (rowDiff != colDiff) {
            return ValidationResult.invalid(color, "Bishop", from, to, "Bishop must move diagonally");
        }

        // Determine direction
        int rowStep = (toRow > fromRow) ? 1 : -1;
        int colStep = (toCol > fromCol) ? 1 : -1;

        // Check path is clear
        int row = fromRow + rowStep;
        int col = fromCol + colStep;
        while (row != toRow) {
            if (chessBoard.board[row][col] != null) {
                return ValidationResult.invalid(color, "Bishop", from, to, "Path blocked");
            }
            row += rowStep;
            col += colStep;
        }

        ChessPiece targetPiece = chessBoard.board[toRow][toCol];

        if (isCapture) {
            if (targetPiece == null || targetPiece.getColor().equals(color)) {
                return ValidationResult.invalid(color, "Bishop", from, to, "No enemy piece to capture");
            }
        } else {
            if (targetPiece != null) {
                return ValidationResult.invalid(color, "Bishop", from, to, "Cannot move In square is occupied");
            }
        }

        return ValidationResult.valid(color, "Bishop", from, to);
    }

    public ValidationResult canQueenMove(String color, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        // Queen combines rook and bishop movement
        ValidationResult rookResult = canRookMove(color, fromRow, fromCol, toRow, toCol, isCapture);
        if (rookResult.isValid()) {
            return ValidationResult.valid(color, "Queen", from, to);
        }

        ValidationResult bishopResult = canBishopMove(color, fromRow, fromCol, toRow, toCol, isCapture);
        if (bishopResult.isValid()) {
            return ValidationResult.valid(color, "Queen", from, to);
        }


        return ValidationResult.invalid(color, "Queen", from, to, "Invalid queen movement");
    }

    public ValidationResult canKnightMove(String color, int fromRow, int fromCol, int toRow, int toCol, boolean isCapture) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Cannot stay in place
        if (rowDiff == 0 && colDiff == 0) {
            return ValidationResult.invalid(color, "Knight", from, to, "Knight cannot stay in place");
        }

        // Must be L-shaped movement
        boolean isValidKnightMove = (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
        if (!isValidKnightMove) {
            return ValidationResult.invalid(color, "Knight", from, to, "Knight must move in L-shape");
        }

        // Cannot capture own piece
        ChessPiece targetPiece = chessBoard.board[toRow][toCol];

        if (isCapture) {
            if (targetPiece == null || targetPiece.getColor().equals(color)) {
                return ValidationResult.invalid(color, "Knight", from, to, "No enemy piece to capture");
            }
        } else {
            if (targetPiece != null) {
                return ValidationResult.invalid(color, "Knight", from, to, "Cannot move In square is occupied");
            }
        }


        return ValidationResult.valid(color, "Knight", from, to);
    }

    public ValidationResult canKingMove(String color, int fromRow, int fromCol, int toRow, int toCol,
                                        boolean isCapture) {
        Position from = new Position(fromRow, fromCol);
        Position to = new Position(toRow, toCol);

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Cannot stay in place
        if (rowDiff == 0 && colDiff == 0) {
            return ValidationResult.invalid(color, "King", from, to, "King cannot stay in place");
        }

        // King can only move one square
        if (rowDiff > 1 || colDiff > 1) {
            return ValidationResult.invalid(color, "King", from, to, "King can only move one square");
        }

        // Cannot capture own piece
        ChessPiece targetPiece = chessBoard.board[toRow][toCol];

        if (isCapture) {
            if (targetPiece == null || targetPiece.getColor().equals(color)) {
                return ValidationResult.invalid(color, "King", from, to, "No enemy piece to capture");
            }
        } else {
            if (targetPiece != null) {
                return ValidationResult.invalid(color, "King", from, to, "Cannot move In square is occupied");
            }
        }


        // Check if destination square is safe
        if (safetyChecker != null && !safetyChecker.IsSquareSafeForKing(color, toRow, toCol)) {
            return ValidationResult.invalid(color, "King", from, to, "King cannot move into check");
        }


        return ValidationResult.valid(color, "King", from, to);
    }
}