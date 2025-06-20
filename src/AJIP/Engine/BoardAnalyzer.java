package AJIP.Engine;

import AJIP.Model.ChessBoard;
import AJIP.Model.ChessPiece;
import AJIP.Records.Position;

import java.util.List;
import java.util.stream.IntStream;

public class BoardAnalyzer {

    private final  ChessBoard chessBoard;

    public BoardAnalyzer(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }



    public List<Position> GetCandidatePositions(String color, String type) {
        return IntStream.range(0, 8).
                boxed().
                flatMap(
                        row -> IntStream.range(0, 8).
                                mapToObj(col -> new Position(row, col))).
                filter(pos -> {
                    ChessPiece candidate = chessBoard.board[pos.row()][pos.col()];
                    return candidate != null &&
                            candidate.getType().equals(type) &&
                            candidate.getColor().equals(color);

                }).toList();

    }


}
