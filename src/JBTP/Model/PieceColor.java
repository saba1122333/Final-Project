package JBTP.Model;

public class PieceColor {
    public static final int WHITE = 1;
    public static final int BLACK = 0;

    private PieceColor() {
        // Private constructor to prevent instantiation
    }

    public static String colorName(int color) {
        return color == WHITE ? "White" : "Black";
    }

    public static int opponent(int color) {
        return color == WHITE ? BLACK : WHITE;
    }
}