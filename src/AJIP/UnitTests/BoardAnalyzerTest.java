package AJIP.UnitTests;

import AJIP.Engine.BoardAnalyzer;
import AJIP.Model.ChessBoard;
import AJIP.Records.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board Analyzer Tests")
class BoardAnalyzerTest {

    private ChessBoard board;
    private BoardAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        board = new ChessBoard();
        analyzer = new BoardAnalyzer(board);
    }

    @Test
    @DisplayName("Should find all pawns of a color")
    void shouldFindAllPawns() {
        List<Position> whitePawns = analyzer.GetCandidatePositions("white", "Pawn");
        List<Position> blackPawns = analyzer.GetCandidatePositions("black", "Pawn");

        assertEquals(8, whitePawns.size());
        assertEquals(8, blackPawns.size());

        // Verify white pawns are on row 6
        assertTrue(whitePawns.stream().allMatch(pos -> pos.row() == 6));
        // Verify black pawns are on row 1
        assertTrue(blackPawns.stream().allMatch(pos -> pos.row() == 1));
    }

    @Test
    @DisplayName("Should find king positions")
    void shouldFindKings() {
        List<Position> whiteKing = analyzer.GetCandidatePositions("white", "King");
        List<Position> blackKing = analyzer.GetCandidatePositions("black", "King");

        assertEquals(1, whiteKing.size());
        assertEquals(1, blackKing.size());
        assertEquals(new Position(7, 4), whiteKing.get(0));
        assertEquals(new Position(0, 4), blackKing.get(0));
    }

    @Test
    @DisplayName("Should find all rooks")
    void shouldFindAllRooks() {
        List<Position> whiteRooks = analyzer.GetCandidatePositions("white", "Rook");
        List<Position> blackRooks = analyzer.GetCandidatePositions("black", "Rook");

        assertEquals(2, whiteRooks.size());
        assertEquals(2, blackRooks.size());
        assertTrue(whiteRooks.contains(new Position(7, 0)));
        assertTrue(whiteRooks.contains(new Position(7, 7)));
    }

    @Test
    @DisplayName("Should find all knights")
    void shouldFindAllKnights() {
        List<Position> whiteKnights = analyzer.GetCandidatePositions("white", "Knight");
        List<Position> blackKnights = analyzer.GetCandidatePositions("black", "Knight");

        assertEquals(2, whiteKnights.size());
        assertEquals(2, blackKnights.size());
        assertTrue(whiteKnights.contains(new Position(7, 1)));
        assertTrue(whiteKnights.contains(new Position(7, 6)));
    }

    @Test
    @DisplayName("Should find all bishops")
    void shouldFindAllBishops() {
        List<Position> whiteBishops = analyzer.GetCandidatePositions("white", "Bishop");
        List<Position> blackBishops = analyzer.GetCandidatePositions("black", "Bishop");

        assertEquals(2, whiteBishops.size());
        assertEquals(2, blackBishops.size());
        assertTrue(whiteBishops.contains(new Position(7, 2)));
        assertTrue(whiteBishops.contains(new Position(7, 5)));
    }

    @Test
    @DisplayName("Should find queens")
    void shouldFindQueens() {
        List<Position> whiteQueen = analyzer.GetCandidatePositions("white", "Queen");
        List<Position> blackQueen = analyzer.GetCandidatePositions("black", "Queen");

        assertEquals(1, whiteQueen.size());
        assertEquals(1, blackQueen.size());
        assertEquals(new Position(7, 3), whiteQueen.get(0));
        assertEquals(new Position(0, 3), blackQueen.get(0));
    }

    @Test
    @DisplayName("Should return empty list for non-existent pieces")
    void shouldReturnEmptyForNonExistent() {
        // Clear a square and verify
        board.board[6][0] = null;
        List<Position> pieces = analyzer.GetCandidatePositions("white", "Pawn");
        assertEquals(7, pieces.size()); // One less pawn
    }

    @Test
    @DisplayName("Should return empty list for unknown piece type")
    void shouldReturnEmptyForUnknownPieceType() {
        List<Position> pieces = analyzer.GetCandidatePositions("white", "InvalidPiece");
        assertTrue(pieces.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list for invalid color")
    void shouldReturnEmptyForInvalidColor() {
        List<Position> pieces = analyzer.GetCandidatePositions("red", "Pawn");
        assertTrue(pieces.isEmpty());
    }
}