package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.TimeBar;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import uk.ac.soton.comp1206.utility.Utility;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    protected Game game;
    protected IntegerProperty score = new SimpleIntegerProperty(0);
    protected IntegerProperty highscore = new SimpleIntegerProperty(0);
    protected BooleanProperty keyboardMode = new SimpleBooleanProperty(false);
    protected int keyboardX = 0;
    protected int keyboardY = 0;
    protected StackPane timerStack;
    protected GameBoard board;
    protected TimeBar timer;
    protected GameBoard nextPiece1;
    protected GameBoard nextPiece2;
    protected boolean chatMode = false;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        board.keyboardModeProperty().bind(keyboardMode);
        mainPane.setCenter(board);

        VBox sideBar = new VBox();
        sideBar.setAlignment(Pos.CENTER);
        sideBar.setSpacing(6.0);
        sideBar.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        mainPane.setRight(sideBar);

        GridPane topBar = new GridPane();
        topBar.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
        mainPane.setTop(topBar);

        VBox scoreBox = new VBox();
        scoreBox.setAlignment(Pos.CENTER);
        Text scoreLabel = new Text("Score");
        scoreLabel.getStyleClass().add("heading");
        scoreBox.getChildren().add(scoreLabel);

        //Displays score
        Text scoreField = new Text("0");
        scoreField.getStyleClass().add("score");
        scoreField.textProperty().bind(score.asString());
        scoreBox.getChildren().add(scoreField);
        topBar.add(scoreBox, 0, 0);

        Text title = new Text("Challenge Mode");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.getStyleClass().add("title");
        title.setTextAlignment(TextAlignment.CENTER);
        topBar.add(title, 1, 0);
        GridPane.setFillWidth(title, Boolean.TRUE);
        GridPane.setHgrow(title, Priority.ALWAYS);
        GridPane.setHalignment(title, HPos.CENTER);

        VBox liveBox = new VBox();
        liveBox.setAlignment(Pos.CENTER);
        Text livesLabel = new Text("Lives");
        livesLabel.getStyleClass().add("heading");
        liveBox.getChildren().add(livesLabel);

        //Displays lives
        Text livesField = new Text("0");
        livesField.getStyleClass().add("lives");
        livesField.textProperty().bind(game.livesProperty().asString());
        liveBox.getChildren().add(livesField);
        topBar.add(liveBox, 2, 0);

        Text highscoreLabel = new Text("High Score");
        highscoreLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(highscoreLabel);

        //Displays the local high score
        Text highscoreField = new Text("0");
        highscoreField.getStyleClass().add("hiscore");
        sideBar.getChildren().add(highscoreField);
        highscoreField.textProperty().bind(highscore.asString());

        Text levelLabel = new Text("Level");
        levelLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(levelLabel);

        //Displays lives left
        Text levelField = new Text("0");
        levelField.getStyleClass().add("level");
        sideBar.getChildren().add(levelField);
        levelField.textProperty().bind(game.levelProperty().asString());

        Text multiplierLabel = new Text("Multiplier");
        multiplierLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(multiplierLabel);

        //Displays multiplier of score
        Text multiplierField = new Text("1");
        multiplierField.getStyleClass().add("multiplier");
        sideBar.getChildren().add(multiplierField);
        multiplierField.textProperty().bind(game.multiplierProperty().asString());

        Text nextPieceLabel = new Text("Incoming");
        nextPieceLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(nextPieceLabel);

        //Creates GameBoard of the first piece
        nextPiece1 = new GameBoard(3, 3, (this.gameWindow.getWidth() / 6), (this.gameWindow.getWidth() / 6));
        nextPiece1.setReadOnly(true);
        nextPiece1.showCentre(true);
        nextPiece1.setOnBlockClick(this::rotateBlock);
        sideBar.getChildren().add(nextPiece1);

        //Creates GameBoard of the second piece
        nextPiece2 = new GameBoard(3, 3, (this.gameWindow.getWidth() / 10), (this.gameWindow.getWidth() / 10));
        nextPiece2.setReadOnly(true);
        nextPiece2.setPadding(new Insets(20.0, 0.0, 0.0, 0.0));
        nextPiece2.setOnBlockClick(this::swapBlock);
        sideBar.getChildren().add(nextPiece2);

        //Handle block on GameBoard grid being clicked
        board.setOnRightClick(this::rotateBlock);
        board.setOnBlockClick(this::blockClicked);

        //Displays timebar
        timerStack = new StackPane();
        mainPane.setBottom(timerStack);
        timer = new TimeBar();
        BorderPane.setMargin(timerStack, new Insets(5.0, 5.0, 5.0, 5.0));
        timerStack.getChildren().add(timer);
        StackPane.setAlignment(timer, Pos.CENTER_LEFT);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        keyboardMode.set(false);
        blockAction(gameBlock);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");
        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Starts the game
     */
    public void startGame() {
        logger.info("Start game");
        game.start();
    }

    /**
     * Ends the game
     */
    public void endGame() {
        logger.info("End game");
        game.stop();
        Multimedia.stopAll();
    }

    /**
     * Loops the game and makes the UI of the timebar
     * @param nextLoop
     */
    protected void gameLoop(int nextLoop) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREEN)), new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), timerStack.getWidth())), new KeyFrame(new Duration((double)nextLoop * 0.5), new KeyValue(timer.fillProperty(), Color.YELLOW)), new KeyFrame(new Duration((double)nextLoop * 0.75), new KeyValue(timer.fillProperty(), Color.RED)), new KeyFrame(new Duration(nextLoop), new KeyValue(timer.widthProperty(), 0)));
        timeline.play();
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        Multimedia.startBackgroundMusic("game_start.wav", "game.wav");

        game.scoreProperty().addListener(this::setScore);
        game.setOnLineCleared(this::lineCleared);
        game.setOnGameLoop(this::gameLoop);
        game.setOnNextPiece(this::nextPiece);
        scene.setOnKeyPressed(this::handleKey);

        ArrayList<Pair<String, Integer>> scores = Utility.loadScores();
        highscore.set(scores.get(0).getValue());

        startGame();

        game.livesProperty().addListener((observable, oldV, newV) -> {
            if (oldV.intValue() > newV.intValue()) {
                Multimedia.playAudio("lifelose.wav");
            } else {
                Multimedia.playAudio("lifegain.wav");
            }
        });

        game.levelProperty().addListener((observable, oldV, newV) -> {
            if (newV.intValue() > oldV.intValue())
                Multimedia.playAudio("level.wav");
        });

        game.setOnGameOver(() -> {
            endGame();
            gameWindow.startScores(game);
        });
    }

    /**
     * Defines the next pieces that will be displayed
     * @param nextPiece
     */
    protected void nextPiece(GamePiece nextPiece) {
        logger.info("Next piece: " + nextPiece);
        nextPiece1.setPiece(nextPiece);
        nextPiece2.setPiece(game.getNextPiece());
    }

    /**
     * When a line is cleared, it highlights the blocks and they're removed
     * @param blocks
     */
    protected void lineCleared(Collection<GameBlockCoordinate> blocks) {
        for (GameBlockCoordinate block : blocks) {
            board.highlight(board.getBlock(block.getX(), block.getY()));
        }
        Multimedia.playAudio("clear.wav");
    }


    /**
     * The current piece and the next piece (that are displayed) swap
     * @param gameBlock
     */
    protected void swapBlock(GameBlock gameBlock) {
        swapBlock();
    }

    protected void swapBlock() {
        logger.info("Swapped block");
        Multimedia.playAudio("rotate.wav");
        game.swapCurrentPiece();
        nextPiece1.setPiece(game.getCurrentPiece());
        nextPiece2.setPiece(game.getNextPiece());
    }

    /**
     * The current piece rotates in its gameboard by 90 degrees
     * @param gameBlock
     */
    protected void rotateBlock(GameBlock gameBlock) {
        rotateBlock();
    }

    protected void rotateBlock() {
        rotateBlock(1);
    }

    protected void rotateBlock(int rotations) {
        logger.info("Rotated block");
        Multimedia.playAudio("rotate.wav");
        game.rotateCurrentPiece(rotations);
        nextPiece1.setPiece(game.getCurrentPiece());
    }

    /**
     * The functions of the keys when they're pressed in the keyboard
     * @param keyEvent
     */
    protected void handleKey(KeyEvent keyEvent) {
        if (this.chatMode) {
            return;
        }
        keyboardMode.set(true);
        if (keyEvent.getCode().equals(KeyCode.LEFT) || keyEvent.getCode().equals(KeyCode.A)) {
            if (keyboardX > 0)
                keyboardX--;
        } else if (keyEvent.getCode().equals(KeyCode.RIGHT) || keyEvent.getCode().equals(KeyCode.D)) {
            if (keyboardX < game.getCols() - 1)
                keyboardX++;
        } else if (keyEvent.getCode().equals(KeyCode.UP) || keyEvent.getCode().equals(KeyCode.W)) {
            if (keyboardY > 0)
                keyboardY--;
        } else if (keyEvent.getCode().equals(KeyCode.DOWN) || keyEvent.getCode().equals(KeyCode.S)) {
            if (keyboardY < game.getRows() - 1)
                keyboardY++;
        } else if (keyEvent.getCode().equals(KeyCode.ENTER) || keyEvent.getCode().equals(KeyCode.X)) {
            blockClicked(board.getBlock(keyboardX, keyboardY));
        } else if (keyEvent.getCode().equals(KeyCode.Q) || keyEvent.getCode().equals(KeyCode.Z) || keyEvent.getCode().equals(KeyCode.OPEN_BRACKET)) {
            rotateBlock(3);
        } else if (keyEvent.getCode().equals(KeyCode.E) || keyEvent.getCode().equals(KeyCode.C) || keyEvent.getCode().equals(KeyCode.CLOSE_BRACKET)) {
            rotateBlock();
        } else if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            endGame();
            gameWindow.startMenu();
        } else if (keyEvent.getCode().equals(KeyCode.T)) {
            startChat();
        }
        board.hover(board.getBlock(keyboardX, keyboardY));
    }

    protected void startChat() { }


    /**
     * Sets the score of the game
     * If the local highscore is surpassed, the current score takes the place of the highscore
     * @param observable
     * @param oldValue
     * @param newValue
     */
    protected void setScore(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
       if (newValue.intValue() > highscore.get()) {
            highscore.set(newValue.intValue());
        }
        Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(score, oldValue)), new KeyFrame(new Duration(500.0), new KeyValue(score, newValue)));
        timeline.play();
    }

    /**
     * Sets the sounds and functions when the game pieces are placed in the gameboard.
     * If a game piece is put in a valid location, the game loop restarts and plays the place sound.
     * If not, the it just plays the fail sound.
     * @param gameBlock
     */
    protected void blockAction(GameBlock gameBlock) {
        if (game.blockClicked(gameBlock)) {
            logger.info("Placed {}", gameBlock);
            Multimedia.playAudio("place.wav");
            game.restartGameLoop();
        } else {
            logger.info("Can't place {}", gameBlock);
            Multimedia.playAudio("fail.wav");
        }
    }
}