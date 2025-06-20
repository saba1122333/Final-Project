package AJIP.Parser;

import AJIP.Logger.PGNLogger;
import AJIP.Model.ChessGame;
import AJIP.Model.ChessMove;
import AJIP.Records.ParseResult;
import AJIP.Model.ChessMove;
import AJIP.Records.ParseResult;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A robust PGN file parser that uses a state machine approach
 * to accurately parse chess games regardless of formatting.
 */
public class PGNParser {

    // Record to hold parsed game data
    public record ParsedGameData(List<List<String>> gameList, List<String> originalGameTexts) {
    }

    // State machine states
    private enum ParserState {
        BETWEEN_GAMES,  // Between games or at start of file
        IN_HEADERS,     // Currently reading header tags
        IN_MOVES        // Currently reading moves
    }

    /**
     * Processes a PGN file and extracts all games
     *
     * @param fileName Path to the PGN file
     * @return ParsedGameData containing move lists and original game texts
     */
    private ParsedGameData parsePGNFile(String fileName) {
        List<List<String>> gameList = new ArrayList<>();
        List<String> originalGameTexts = new ArrayList<>();
        List<String> validGameTexts = new ArrayList<>();


        ParseResult startResult = ParseResult.info("File Parse Started", "PGN", "Starting to parse PGN file: " + fileName);
        PGNLogger.info(startResult.getLoggerMessage());

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            ParserState state = ParserState.BETWEEN_GAMES;
            StringBuilder currentHeaders = new StringBuilder();
            StringBuilder currentMoves = new StringBuilder();

            // Patterns to detect structural elements
            Pattern headerPattern = Pattern.compile("\\[(\\w+)\\s+\"(.*)\"]");
            Pattern moveNumberPattern = Pattern.compile("^\\d+\\."); // Starts with digits followed by dot
            Pattern resultPattern = Pattern.compile("(1-0|0-1|1/2-1/2|\\*)\\s*$");

            String line;
            int lineNumber = 0;
            int gameCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip empty lines but don't change state based on them
                if (line.isEmpty()) {
                    continue;
                }

                // Check if this line is a header tag
                Matcher headerMatcher = headerPattern.matcher(line);
                if (headerMatcher.matches()) {
                    // Found a header tag

                    if (state == ParserState.IN_MOVES) {
                        // If we were in moves and found a header, this is a new game
                        // Process the completed game first
                        processMoves(currentMoves, ++gameCount, gameList, originalGameTexts);
                        currentMoves = new StringBuilder();
                        currentHeaders = new StringBuilder();
                    }

                    // Add header to current collection
                    state = ParserState.IN_HEADERS;
                    continue;
                }

                // Detect move section by looking for move numbers or algebraic notation
                Matcher moveNumberMatcher = moveNumberPattern.matcher(line);
                if (moveNumberMatcher.find() || containsChessNotation(line)) {
                    state = ParserState.IN_MOVES;
                    currentMoves.append(line).append(" ");

                    // Check if this line contains a game result indicator
                    Matcher resultMatcher = resultPattern.matcher(line);
                    if (resultMatcher.find()) {
                        // We've reached the end of a game
                        processMoves(currentMoves, ++gameCount, gameList, originalGameTexts);
                        currentMoves = new StringBuilder();
                        state = ParserState.BETWEEN_GAMES;
                    }
                    continue;
                }

                // If we're already in a move section, continue adding lines
                if (state == ParserState.IN_MOVES) {
                    currentMoves.append(line).append(" ");

                    // Check for end of game
                    Matcher resultMatcher = resultPattern.matcher(line);
                    if (resultMatcher.find()) {
                        processMoves(currentMoves, ++gameCount, gameList, originalGameTexts);
                        currentMoves = new StringBuilder();
                        state = ParserState.BETWEEN_GAMES;
                    }
                }
            }

            // Process the final game if there is one in progress
            if (currentMoves.length() > 0) {
                processMoves(currentMoves, ++gameCount, gameList, originalGameTexts);
            }

            ParseResult completeResult = ParseResult.info("File Parse Completed", "PGN",
                    "Successfully parsed " + gameList.size() + " games from " + fileName);
            PGNLogger.info(completeResult.getLoggerMessage());

        } catch (IOException e) {
            ParseResult errorResult = ParseResult.pgnFailure("File Read Error", 0,
                    "Error reading PGN file: " + e.getMessage(), fileName);
            PGNLogger.error(errorResult.getLoggerMessage());
        }

        return new ParsedGameData(gameList, originalGameTexts);
    }

    /**
     * Check if a line contains chess algebraic notation
     * This is a simple heuristic detector
     */
    private boolean containsChessNotation(String line) {
        // Look for common patterns in algebraic notation
        Pattern piecePattern = Pattern.compile("[KQRBN][a-h][1-8]");
        Pattern pawnMovePattern = Pattern.compile("[a-h][1-8]");
        Pattern capturePattern = Pattern.compile("[KQRBNa-h]x[a-h][1-8]");
        Pattern castlePattern = Pattern.compile("O-O(-O)?");

        return piecePattern.matcher(line).find() ||
                pawnMovePattern.matcher(line).find() ||
                capturePattern.matcher(line).find() ||
                castlePattern.matcher(line).find();
    }

    /**
     * Process moves and add to the collections
     */
    private void processMoves(StringBuilder moveSection, int gameNumber,
                              List<List<String>> gameList, List<String> originalGameTexts) {
        String movesText = moveSection.toString().trim();


        // Remove game result indicators
        movesText = movesText.replaceAll("1-0|0-1|1/2-1/2|\\*", "");

        // Remove comments in curly braces and parentheses
        movesText = movesText.replaceAll("\\{[^}]*\\}", "");
        movesText = movesText.replaceAll("\\([^)]*\\)", "");

        // Split the move text by move numbers using regex
        // This captures the move number as group 1 and the move content as group 2
        Pattern moveNumberPattern = Pattern.compile("(\\d+\\.)(.*?)(?=\\d+\\.|$)");
        Matcher matcher = moveNumberPattern.matcher(movesText);

        List<String[]> potentialMoves = new ArrayList<>();
        List<String> moveList = new ArrayList<>();
        while (matcher.find()) {
            // Get the content after the move number (group 2)
            String moveContent = matcher.group(2).trim();
            // Split this content by whitespace to get individual moves
            potentialMoves.add(moveContent.split("\\s+"));
        }

        // Check if we found any moves
        if (potentialMoves.isEmpty()) {
            ParseResult noMovesResult = ParseResult.pgnFailure("Game Parse Failed", gameNumber,
                    "No recognizable moves found", moveSection.toString());
            PGNLogger.error(noMovesResult.getLoggerMessage());
            return;
        }

        for (int i = 0; i < potentialMoves.size(); i++) {
            String[] move = potentialMoves.get(i);
            // Regular move pairs (all except the last one)
            if (i != potentialMoves.size() - 1) {
                // Must have exactly 2 moves (white and black)
                if (move.length != 2) {
                    String errorDetails = "Expected 2 moves at move " + (i + 1) + " but found " + move.length + ": " + Arrays.toString(move);
                    ParseResult errorResult = ParseResult.pgnFailure("Move Pair Validation Failed", gameNumber,
                            errorDetails, moveSection.toString());
                    PGNLogger.error(errorResult.getLoggerMessage());
                    return;
                }
            }
            // Special case: the last move pair
            else {
                // Can have either 1 move (white only) or 2 moves (white and black)
                if (move.length != 1 && move.length != 2) {
                    String errorDetails = "Expected 1 or 2 moves in last move but found " + move.length + ": " + Arrays.toString(move);
                    ParseResult errorResult = ParseResult.pgnFailure("Last Move Validation Failed", gameNumber,
                            errorDetails, moveSection.toString());
                    PGNLogger.error(errorResult.getLoggerMessage());
                    return;
                }
            }

            for (String s : move) {
                moveList.add(s.trim());
            }
        }

        ParseResult successResult = ParseResult.pgnSuccess("Game Parsed Successfully", gameNumber,
                moveList.size() + " moves");
        PGNLogger.info(successResult.getLoggerMessage());

        gameList.add(moveList);
        originalGameTexts.add(moveSection.toString().trim());

    }

    /**
     * Get ChessGame objects with both parsed moves and original text
     */
    public List<ChessGame> getParsedGames(String fileName) {
        ParsedGameData parsedData = parsePGNFile(fileName);
        List<ChessGame> games = new ArrayList<>();

        for (int i = 0; i < parsedData.gameList().size(); i++) {
            List<String> moveList = parsedData.gameList().get(i);
            String originalText = parsedData.originalGameTexts().get(i);

            // Parse moves using the new method that includes game context
            List<ChessMove> chessMoves = ChessMovesParser.parseWithContext(moveList, originalText, i + 1);
            if (!chessMoves.isEmpty()) {
                games.add(new ChessGame(chessMoves, originalText));

            }
        }
        return games;
    }
}