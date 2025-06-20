package AJIP.Processor;

import AJIP.Controller.GameMasterController;
import AJIP.Logger.PGNLogger;
import AJIP.Model.ChessGame;
import AJIP.Parser.PGNParser;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelProcessor {

    private final BlockingQueue<ChessGame> gameQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger filesRemaining = new AtomicInteger();

    public void processFiles(List<String> files) {
        ExecutorService parsers = Executors.newFixedThreadPool(4);
        ExecutorService evaluators = Executors.newFixedThreadPool(4);

        filesRemaining.set(files.size());

        // Start 4 evaluator threads
        for (int i = 0; i < 4; i++) {
            evaluators.submit(this::evaluateGames);
        }

        // Submit parser tasks for each file
        for (String file : files) {
            parsers.submit(() -> parseFile(file));
        }

        // Wait for completion
        parsers.shutdown();
        try {
            parsers.awaitTermination(30, TimeUnit.MINUTES);
            evaluators.shutdown();
            evaluators.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void parseFile(String fileName) {
        try {
            PGNParser parser = new PGNParser();
            List<ChessGame> games = parser.getParsedGames(fileName);

            // Add all games to queue
            for (ChessGame game : games) {
                gameQueue.offer(game);
            }

            PGNLogger.info("Parsed " + games.size() + " games from " + fileName);

        } catch (Exception e) {
            PGNLogger.error("Error parsing " + fileName, e);
        } finally {
            // Signal when all files are parsed
            if (filesRemaining.decrementAndGet() == 0) {
                // Add stop signals for evaluators
                for (int i = 0; i < 4; i++) {
                    gameQueue.offer(new ChessGame(null, "STOP"));
                }
            }
        }
    }

    private void evaluateGames() {
        GameMasterController controller = new GameMasterController(true);

        while (true) {
            try {
                ChessGame game = gameQueue.take();

                // Stop signal
                if (game.getOriginalText().equals("STOP")) {
                    break;
                }

                // Evaluate the game
                controller.Evaluate(game);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                PGNLogger.error("Error evaluating game", e);
            }
        }
    }

}