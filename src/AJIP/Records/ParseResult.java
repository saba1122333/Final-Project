package AJIP.Records;

/**
 * Result of parsing operations for PGN files and chess moves
 */
public record ParseResult(boolean success, String operation, String context,
                          int lineNumber, int gameNumber, String error, String data) {

    /**
     * Create a successful parse result for PGN parsing
     */
    public static ParseResult pgnSuccess(String operation, int gameNumber, String data) {
        return new ParseResult(true, operation, "PGN", -1, gameNumber, "", data);
    }

    /**
     * Create a successful parse result for move parsing
     */
    public static ParseResult moveSuccess(String operation, String moveNotation) {
        return new ParseResult(true, operation, "Move", -1, -1, "", moveNotation);
    }

    /**
     * Create a failed parse result for PGN parsing
     */
    public static ParseResult pgnFailure(String operation, int gameNumber, String error, String data) {
        return new ParseResult(false, operation, "PGN", -1, gameNumber, error, data);
    }

    /**
     * Create a failed parse result for PGN parsing with line number
     */
    public static ParseResult pgnFailure(String operation, int lineNumber, int gameNumber, String error, String data) {
        return new ParseResult(false, operation, "PGN", lineNumber, gameNumber, error, data);
    }

    /**
     * Create a failed parse result for move parsing
     */
    public static ParseResult moveFailure(String operation, String moveNotation, String error) {
        return new ParseResult(false, operation, "Move", -1, -1, error, moveNotation);
    }

    /**
     * Create an info-level parse result (neither success nor failure)
     */
    public static ParseResult info(String operation, String context, String message) {
        return new ParseResult(true, operation, context, -1, -1, "", message);
    }

    /**
     * Get formatted logger message
     */
    public String getLoggerMessage() {
        StringBuilder message = new StringBuilder();

        // Add context and operation
        message.append("[").append(context).append(" ").append(operation).append("]");

        // Add game/line context if available
        if (gameNumber > 0) {
            message.append(" Game #").append(gameNumber);
        }
        if (lineNumber > 0) {
            message.append(" Line ").append(lineNumber);
        }

        // Add status
        if (!success && !error.isEmpty()) {
            message.append(" FAILED: ").append(error);
        } else {
            message.append(" SUCCESS");
        }

        // Add data context if available
        if (!data.isEmpty()) {

            message.append(" - Data: ").append(data);

        }

        return message.toString();
    }

    /**
     * Get short summary for successful operations
     */
    public String getSummaryMessage() {
        if (success) {
            return String.format("[%s %s] %s", context, operation,
                    gameNumber > 0 ? "Game #" + gameNumber : data);
        } else {
            return getLoggerMessage();
        }
    }

    /**
     * Check if parsing was successful
     */
    public boolean isValid() {
        return success;
    }

    /**
     * Get error message or empty string if successful
     */
    public String getErrorMessage() {
        return error != null ? error : "";
    }
}