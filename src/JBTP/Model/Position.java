package JBTP.Model;

public class Position {
    private final int column;
    private final int row;

    public Position(int column, int row) {
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    // Create a new position with offset
    public Position offset(int colOffset, int rowOffset) {
        return new Position(column + colOffset, row + rowOffset);
    }

    // Check if position is within chess board bounds
    public boolean isValid() {
        return column >= 0 && column < 8 && row >= 0 && row < 8;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return column == position.column && row == position.row;
    }

    @Override
    public int hashCode() {
        return 31 * column + row;
    }

    @Override
    public String toString() {
        char file = (char) ('a' + column);
        int rank = 8 - row;
        return "" + file + rank;
    }
}