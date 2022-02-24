package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;
import java.util.ArrayDeque;
import java.util.Random;

/**
 * The Multiplayer Game class handles the main logic, state and properties of the TetrECS multiplayer game.
 */
public class MultiplayerGame extends Game{
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
    private final Communicator communicator;
    private Random random = new Random();
    private ArrayDeque<GamePiece> incoming = new ArrayDeque();

    /**
     * Create a new Multiplayer game with the specified rows and columns.
     * Connects communicator to send and receive messages from the server.
     * @param communicator
     * @param cols
     * @param rows
     */
    public MultiplayerGame(Communicator communicator, int cols, int rows) {
        super(cols, rows);
        this.communicator = communicator;
        communicator.addListener(message -> Platform.runLater(() -> receiveMessage(message.trim())));
    }


    @Override
    public void initialise() {
        logger.info("Initialising game:)");
        score.set(0);
        level.set(0);
        lives.set(3);
        multiplier.set(1);
        initialPieces();
    }


    /**
     * Receives next piece and current scores from all players
     * @param message
     */
    private void receiveMessage(String message) {
        logger.info("Received message: {}", message);
        String[] components = message.split(" ", 2);
        String command = components[0];
        if (command.equals("PIECE") && components.length > 1) {
            String data = components[1];
            receivePiece(Integer.parseInt(data));
        } else if (command.equals("SCORES") && components.length > 1) {
            String data = components[1];
            receiveScores(data);
        }
    }

    /**
     * Request the initial pieces
     */
    public void initialPieces() {
        for (int i = 0; i < 5; ++i) {
            communicator.send("PIECE");
        }
    }

    /**
     * Request the next piece
     * @return the game piece
     */
    @Override
    public GamePiece spawnPiece() {
        communicator.send("PIECE");
        return incoming.pop();
    }

    private void receivePiece(int block) {
        GamePiece piece = GamePiece.createPiece(block, random.nextInt(3));
        incoming.push(piece);
        if (!started && incoming.size() > 2) {
            nextPiece = spawnPiece();
            nextPiece();
            started = true;
        }
    }

    private void receiveScores(String data) {
        String[] scoreLines;
        scores.clear();
        for (String scoreLine : scoreLines = data.split("\\R")) {
            String[] components = scoreLine.split(":");
            String player = components[0];
            int score = Integer.parseInt(components[1]);
            scores.add(new Pair<>(player, score));
        }
        scores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    }

    /**
     * Update player current board when changed
     * @param gameBlock the block that was clicked
     * @return current board
     */
    @Override
    public boolean blockClicked(GameBlock gameBlock) {
        boolean result = super.blockClicked(gameBlock);
        communicator.send("BOARD " + encode());
        return result;
    }


    public String encode() {
        StringBuilder board = new StringBuilder();
        for (int x = 0; x < cols; ++x) {
            for (int y = 0; y < rows; ++y) {
                board.append(grid.get(x, y) + " ");
            }
        }
        return board.toString().trim();
    }
}
