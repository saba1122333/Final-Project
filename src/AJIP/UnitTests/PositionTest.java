package AJIP.UnitTests;

import AJIP.Records.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Position Record Tests")
class PositionTest {

    @Test
    @DisplayName("Should create valid positions")
    void shouldCreateValidPositions() {
        Position pos = new Position(3, 4);
        assertEquals(3, pos.row());
        assertEquals(4, pos.col());
        assertTrue(pos.isValid());
    }

    @Test
    @DisplayName("Should create position at board corners")
    void shouldCreatePositionAtBoardCorners() {
        Position topLeft = new Position(0, 0);
        Position topRight = new Position(0, 7);
        Position bottomLeft = new Position(7, 0);
        Position bottomRight = new Position(7, 7);

        assertTrue(topLeft.isValid());
        assertTrue(topRight.isValid());
        assertTrue(bottomLeft.isValid());
        assertTrue(bottomRight.isValid());
    }

    @Test
    @DisplayName("Should reject negative row values")
    void shouldRejectNegativeRowValues() {
        assertThrows(IllegalArgumentException.class, () -> new Position(-1, 4));
        assertThrows(IllegalArgumentException.class, () -> new Position(-5, 0));
    }

    @Test
    @DisplayName("Should reject negative column values")
    void shouldRejectNegativeColumnValues() {
        assertThrows(IllegalArgumentException.class, () -> new Position(3, -1));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, -3));
    }

    @Test
    @DisplayName("Should reject row values greater than 7")
    void shouldRejectRowValuesGreaterThan7() {
        assertThrows(IllegalArgumentException.class, () -> new Position(8, 4));
        assertThrows(IllegalArgumentException.class, () -> new Position(10, 0));
    }

    @Test
    @DisplayName("Should reject column values greater than 7")
    void shouldRejectColumnValuesGreaterThan7() {
        assertThrows(IllegalArgumentException.class, () -> new Position(3, 8));
        assertThrows(IllegalArgumentException.class, () -> new Position(0, 10));
    }

    @Test
    @DisplayName("Should convert from chess notation correctly")
    void shouldConvertFromChessNotationCorrectly() {
        Position e4 = Position.fromNotation("e4");
        assertEquals(4, e4.row());
        assertEquals(4, e4.col());

        Position a1 = Position.fromNotation("a1");
        assertEquals(7, a1.row());
        assertEquals(0, a1.col());

        Position h8 = Position.fromNotation("h8");
        assertEquals(0, h8.row());
        assertEquals(7, h8.col());

        Position d5 = Position.fromNotation("d5");
        assertEquals(3, d5.row());
        assertEquals(3, d5.col());
    }

    @Test
    @DisplayName("Should convert to chess notation correctly")
    void shouldConvertToChessNotationCorrectly() {
        assertEquals("e4", new Position(4, 4).toNotation());
        assertEquals("a1", new Position(7, 0).toNotation());
        assertEquals("h8", new Position(0, 7).toNotation());
        assertEquals("d5", new Position(3, 3).toNotation());
        assertEquals("f2", new Position(6, 5).toNotation());
    }

    @Test
    @DisplayName("Should handle round-trip notation conversion")
    void shouldHandleRoundTripNotationConversion() {
        String[] notations = {"a1", "e4", "h8", "d5", "b3", "f7", "c6", "g2"};

        for (String notation : notations) {
            Position pos = Position.fromNotation(notation);
            assertEquals(notation, pos.toNotation());
        }
    }

    @Test
    @DisplayName("Should reject invalid notation strings")
    void shouldRejectInvalidNotationStrings() {
        assertThrows(IllegalArgumentException.class, () -> Position.fromNotation(""));
        assertThrows(IllegalArgumentException.class, () -> Position.fromNotation("e"));
        assertThrows(IllegalArgumentException.class, () -> Position.fromNotation("e45"));
        assertThrows(IllegalArgumentException.class, () -> Position.fromNotation("i4"));
        assertThrows(IllegalArgumentException.class, () -> Position.fromNotation("e9"));
        assertThrows(IllegalArgumentException.class, () -> Position.fromNotation("z0"));
    }

    @Test
    @DisplayName("Should validate boundary positions correctly")
    void shouldValidateBoundaryPositionsCorrectly() {
        assertTrue(new Position(0, 0).isValid());
        assertTrue(new Position(7, 7).isValid());
        assertTrue(new Position(3, 4).isValid());
    }

    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        Position pos1 = new Position(3, 4);
        Position pos2 = new Position(3, 4);
        Position pos3 = new Position(4, 3);

        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }

    @Test
    @DisplayName("Should handle all files correctly")
    void shouldHandleAllFilesCorrectly() {
        char[] files = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        for (int i = 0; i < files.length; i++) {
            String notation = files[i] + "4";
            Position pos = Position.fromNotation(notation);
            assertEquals(i, pos.col());
            assertEquals(notation, pos.toNotation());
        }
    }

    @Test
    @DisplayName("Should handle all ranks correctly")
    void shouldHandleAllRanksCorrectly() {
        for (int rank = 1; rank <= 8; rank++) {
            String notation = "e" + rank;
            Position pos = Position.fromNotation(notation);
            assertEquals(8 - rank, pos.row());
            assertEquals(notation, pos.toNotation());
        }
    }

    @Test
    @DisplayName("Should create position with toString representation")
    void shouldCreatePositionWithToStringRepresentation() {
        Position pos = new Position(3, 4);
        String str = pos.toString();
        assertTrue(str.contains("3"));
        assertTrue(str.contains("4"));
    }
}