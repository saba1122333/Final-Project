package AJIP;

import AJIP.Controller.GameMasterController;
import AJIP.Model.ChessGame;
import AJIP.Parser.PGNParser;
import AJIP.Processor.ParallelProcessor;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        List<String> files = List.of(
                "src/AJIP/testPgns/Philidor.pgn",
                "src/AJIP/testPgns/Chess Lessons.pgn",
                "src/AJIP/testPgns/WikiExample.pgn",
              "src/AJIP/testPgns/customGame.pgn"

        );
        new ParallelProcessor().processFiles(files);

    }
}