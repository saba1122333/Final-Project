package AJIP.Records;

/**
 * Result of validating a chess move
 */
public record ValidationResult(boolean isValid, String color, String pieceType,
                               Position from, Position to, String error) {

    /**
     * Create a successful validation result
     */
    public static ValidationResult valid(String color, String pieceType,  Position from, Position to) {
        return new ValidationResult(true, color, pieceType, from, to, "");
    }

    /**
     * Create a failed validation result
     */
    public static ValidationResult invalid(String color, String pieceType,  Position from, Position to, String error) {
        return new ValidationResult(false, color, pieceType, from, to, error);
    }

    /**
     * Create a failed validation result without positions
     */
    public static ValidationResult invalid(String color, String pieceType, String error) {
        return new ValidationResult(false, color, pieceType, null, null, error);
    }

    /**
     * Get formatted logger message
     */
    public String getLoggerMessage() {
        String status = isValid ? "valid" : "invalid";
        String fromPos = from != null ? from.toNotation() : "?";
        String toPos = to != null ? to.toNotation() : "?";

        String message = String.format("%s %s %s from %s to %s",
                status, color, pieceType, fromPos, toPos);

        return error.isEmpty() ? message : message + " - " + error;
    }

    /**
     * Get error message or empty string if valid
     */
    public String getErrorMessage() {
        return error != null ? error : "";
    }
}