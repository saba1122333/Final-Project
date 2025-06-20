package AJIP.Model;

import java.util.List;

public class ChessGame {
    public List<ChessMove> moves;
    public String originalText;


    public ChessGame(List<ChessMove> moves, String movesText) {
        this.moves = moves;
        this.originalText = movesText;
    }


    public List<ChessMove> getMoves() {
        return moves;
    }

    public String getOriginalText() {
        return originalText;
    }
}
