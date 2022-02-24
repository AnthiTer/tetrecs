package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    protected final int rows;

    protected final int cols;

    protected final Grid grid;

    protected IntegerProperty score = new SimpleIntegerProperty(0);

    protected IntegerProperty level = new SimpleIntegerProperty(0);

    protected IntegerProperty lives = new SimpleIntegerProperty(0);

    protected IntegerProperty multiplier = new SimpleIntegerProperty(0);

    protected StringProperty name = new SimpleStringProperty();

    protected ArrayList<Pair<String, Integer>> scores = new ArrayList();

    protected GamePiece currentPiece;

    protected GamePiece nextPiece;

    protected LineClearedListener lineClearedListener = null;

    protected GameLoopListener gameLoopListener = null;

    protected NextPieceListener nextPieceListener = null;

    private GameOverListener gameOverListener = null;

    protected final ScheduledExecutorService executor;

    protected ScheduledFuture<?> nextLoop;

    protected boolean started = false;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);

        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public ArrayList<Pair<String, Integer>> getScores() {
        return this.scores;
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialise();
        startGameLoop();
    }

    /**
     * Stop the game
     */
    public void stop() {
        logger.info("Stopping game");
        executor.shutdownNow();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialise() {
        logger.info("Initialising game");
        score.set(0);
        level.set(0);
        lives.set(3);
        multiplier.set(1);
        nextPiece = spawnPiece();
        nextPiece();
        started = true;
    }

    /**
     * Replaces the current piece with a new piece
     * @return the swapped current piece
     */
    public GamePiece nextPiece() {
        currentPiece = nextPiece;
        nextPiece = spawnPiece();
        logger.info("Current piece: {}", currentPiece);
        logger.info("Next piece: {}", nextPiece);
        if (nextPieceListener != null)
            nextPieceListener.nextPiece(currentPiece);
        return currentPiece;
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        logger.info("Block clicked: {},{}", x, y);

        if (currentPiece == null) {
            logger.error("No current piece");
            return false;
        }
        boolean added = grid.addPieceCentered(currentPiece, gameBlock.getX(), gameBlock.getY());
        if (!added) {
            return false;
        }
        afterPiece();
        nextPiece();
        return true;
    }

    /**
     * It's called after playing a piece
     * Clears any full vertical/horizontal lines that have been made and when two or more lines are intersecting
     */
    public void afterPiece() {
        int total;
        int lines = 0;
        HashSet<IntegerProperty> clear = new HashSet<>();
        HashSet<GameBlockCoordinate> clearBlocks = new HashSet<>();

        //selects full horizontal lines
        for (int x = 0; x < this.cols; ++x) {
            int i;
            total = this.rows;
            for (i = 0; i < this.rows && this.grid.get(x, i) != 0; ++i) {
                total--;
            }
            if (total != 0) continue;
            lines++;
            for (i = 0; i < this.rows; ++i) {
                clear.add(this.grid.getGridProperty(x, i));
                clearBlocks.add(new GameBlockCoordinate(x, i));
            }
        }
        //selects full vertical lines
        for (int y = 0; y < this.rows; ++y) {
            int i;
            total = this.rows;
            for (i = 0; i < this.cols && this.grid.get(i, y) != 0; ++i) {
                total--;
            }
            if (total != 0) continue;
            lines++;
            for (i = 0; i < this.cols; ++i) {
                clear.add(this.grid.getGridProperty(i, y));
                clearBlocks.add(new GameBlockCoordinate(i, y));
            }
        }
        //when no lines are full multiplier is set back to 1
        if (lines == 0) {
            if (multiplier.get() > 1) {
                logger.info("Multiplier set to 1:(");
                multiplier.set(1);
            }
            return;
        }
        logger.info("Cleared {} lines", lines);

        //score increases according to the lines that are cleared and the multiplier
        increaseScore(lines * clear.size() * 10 * multiplier.get());
        logger.info("Score increased by {}", (lines * clear.size() * 10 * multiplier.get()));

        //multiplier increases by 1
        multiplier.set(multiplier.add(1).get());
        logger.info("Multiplier now at {}", multiplier.get());

        //level increases by 1 when a multiple of 1000 is reached
        level.set(Math.floorDiv(score.get(), 1000));

        for (IntegerProperty square : clear)
            square.set(0);

        //clears full lines
        if (lineClearedListener != null)
            lineClearedListener.lineCleared(clearBlocks);
    }

    public void increaseScore(int amount) {
        this.score.set(this.score.add(amount).get());
    }

    public void setOnGameLoop(GameLoopListener listener) {
        gameLoopListener = listener;
    }

    public void setOnLineCleared(LineClearedListener listener) {
        lineClearedListener = listener;
    }

    public void setOnNextPiece(NextPieceListener listener) {
        nextPieceListener = listener;
    }

    public void setOnGameOver(GameOverListener listener) {
        gameOverListener = listener;
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public int getScore() {
        return scoreProperty().get();
    }

    public IntegerProperty livesProperty() {
        return lives;
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public IntegerProperty multiplierProperty() {
        return multiplier;
    }

    public StringProperty nameProperty() {
        return name;
    }


    /**
     * The game loops after you run out of time
     */
    public void startGameLoop() {
        nextLoop = executor.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        if (gameLoopListener != null)
            gameLoopListener.gameLoop(getTimerDelay());
    }

    /**
     * The game loop is restarted (works when you place a piece in a valid spot, a line is cleared etc.)
     */
    public void restartGameLoop() {
        nextLoop.cancel(false);
        startGameLoop();
    }

    /**
     * Game ends
     */
    public void gameOver() {
        logger.info("Game over:(");
        if (gameOverListener != null)
            Platform.runLater(() -> gameOverListener.gameOver());
    }

    /**
     * When you lose a live, the game loops and multiplier goes back to 1, a new piece appears, and the timer is reset
     */
    public void gameLoop() {
        logger.info("Game Loop!!");
        if (multiplier.get() > 1) {
            logger.info("Multiplier set to 1:(");
            multiplier.set(1);
        }
        decreaseLives();
        nextPiece();
        int nextRun = getTimerDelay();
        if (gameLoopListener != null)
            gameLoopListener.gameLoop(nextRun);
        nextLoop = executor.schedule(this::gameLoop, nextRun, TimeUnit.MILLISECONDS);
    }


    /**
     * Timer is set according to your level (or it can't be smaller than 2.5 seconds)
     * @return timer length
     */
    public int getTimerDelay() {return Math.max(2500, 12000 - 500 * level.get());}

    public GamePiece getNextPiece() { return nextPiece; }

    public GamePiece getCurrentPiece() {
        return currentPiece;
    }

    public void rotateCurrentPiece() {
        currentPiece.rotate();
    }

    public void rotateCurrentPiece(int rotations) {
        currentPiece.rotate(rotations);
    }

    public void increaseLevel() {
        level.set(level.add(1).get());
    }


    /**
     * Lives are decreased by 1, if you're left with no lives, the game is over.
     */
    public void decreaseLives() {
        if (lives.get() > 0) {
            lives.set(lives.subtract(1).get());
            logger.info("Lost a life:(");
        } else {
            gameOver();
        }
    }

    /**
     * Swap the current and following pieces
     */
    public void swapCurrentPiece() {
        GamePiece holdingPiece = currentPiece;
        currentPiece = nextPiece;
        nextPiece = holdingPiece;
    }

    /**
     * Creates a new random GamePiece and places it in a random rotation
     * @return the random piece
     */
    public GamePiece spawnPiece(){
        var random = new Random();
        GamePiece piece = GamePiece.createPiece(random.nextInt(15), random.nextInt(3));
        return piece;
    }

    public Grid getGrid() {
        return grid;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public void requestPieces(int i) {
    }

}
