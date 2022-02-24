package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.*;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import java.util.ArrayList;

/**
 * The MultiPlayer challenge scene. Holds the UI for the multiplayer challenge mode in the game.
 */
public class MultiplayerScene extends ChallengeScene{

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
    private final Communicator communicator;
    private ObservableList<Pair<String, Integer>> scoreList;
    private ArrayList<Pair<String, Integer>> scores = new ArrayList();
    private ScoreBox leaderboard;
    private StringProperty name = new SimpleStringProperty();
    private Text receivedMsg;
    private TextField sendMsg;

    /**
     * Create a new MultiPlayer challenge scene
     * @param gameWindow
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Starting multiplayer scene");
        communicator = gameWindow.getCommunicator();
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise(){
        logger.info("Initialising Multiplayer");
        Multimedia.startBackgroundMusic("game_start.wav", "game.wav");

        game.scoreProperty().addListener(this::setScore);
        game.setOnLineCleared(this::lineCleared);
        game.setOnGameLoop(this::gameLoop);
        game.setOnNextPiece(this::nextPiece);
        scene.setOnKeyPressed(this::handleKey);

        startGame();

        communicator.addListener(message -> Platform.runLater(() -> receiveMessage(message.trim())));
        updateName();
        updateScores();

        game.livesProperty().addListener((observable, oldValue, newValue) -> {
            MultiplayerScene.this.sendMessage("LIVES " + newValue);
            if (oldValue.intValue() > newValue.intValue()) {
                Multimedia.playAudio("lifelose.wav");
            } else {
                Multimedia.playAudio("lifegain.wav");
            }
        });

        game.levelProperty().addListener((observable, oldV, newV) -> {
            if (newV.intValue() > oldV.intValue())
                Multimedia.playAudio("level.wav");
        });

        this.game.scoreProperty().addListener((observable, oldValue, newValue) -> MultiplayerScene.this.sendMessage("SCORE " + newValue));


        game.setOnGameOver(() -> {
            endGame();
            gameWindow.startScores(game);
        });
    }

    /**
     * Build the Multiplayer window
     */
    @Override
    public void build(){
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

        VBox mainBoard = new VBox();
        mainBoard.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(mainBoard, Pos.CENTER);
        mainPane.setCenter(mainBoard);

        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        board.keyboardModeProperty().bind(keyboardMode);
        mainBoard.getChildren().add(board);
        VBox.setVgrow(mainBoard, Priority.ALWAYS);

        receivedMsg = new Text("Press T to send a chat message");
        TextFlow messageFlow = new TextFlow();
        messageFlow.setTextAlignment(TextAlignment.CENTER);
        messageFlow.getChildren().add(receivedMsg);
        messageFlow.getStyleClass().add("messages");
        mainBoard.getChildren().add(messageFlow);

        //functions of the chat in the multiplayer game
        sendMsg = new TextField();
        sendMsg.setVisible(false);
        sendMsg.setEditable(false);
        sendMsg.getStyleClass().add("messageBox");
        sendMsg.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                this.sendMsg("");
            }
            if (!e.getCode().equals(KeyCode.ENTER)) {
                return;
            }
            sendMsg(sendMsg.getText());
            sendMsg.clear();
        });
        mainBoard.getChildren().add(sendMsg);

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
        scoreLabel.textProperty().bind(name);
        scoreLabel.getStyleClass().add("heading");
        scoreBox.getChildren().add(scoreLabel);

        //Displays your score
        Text scoreField = new Text("0");
        scoreField.getStyleClass().add("score");
        scoreField.textProperty().bind(score.asString());
        scoreBox.getChildren().add(scoreField);
        topBar.add(scoreBox, 0, 0);

        Text title = new Text("Multiplayer Mode");
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

        //Displays your lives
        Text livesField = new Text("0");
        livesField.getStyleClass().add("lives");
        livesField.textProperty().bind(game.livesProperty().asString());
        liveBox.getChildren().add(livesField);
        topBar.add(liveBox, 2, 0);

        Text hiscoreLabel = new Text("Versus");
        hiscoreLabel.getStyleClass().add("heading");
        sideBar.getChildren().add(hiscoreLabel);

        scoreList = FXCollections.observableArrayList(scores);
        SimpleListProperty<Pair<String, Integer>> scoreWrapper = new SimpleListProperty<>(scoreList);

        //extends the scorelist(for multiplayer)
        leaderboard = new ScoreBox();
        leaderboard.getStyleClass().add("leaderboard");
        leaderboard.setAutoReveal(true);
        leaderboard.setScoresToShow(5);
        leaderboard.scoreProperty().bind(scoreWrapper);
        leaderboard.nameProperty().bind(name);
        sideBar.getChildren().add(leaderboard);

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
        sideBar.getChildren().add(this.nextPiece2);

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
     * Setup the game object and model
     */
    @Override
    public void setupGame() {
        logger.info("Starting a new multiplayer challenge");
        game = new MultiplayerGame(communicator, 5, 5);
    }

    /**
     * Ends the game
     */
    @Override
    public void endGame() {
        logger.info("End game");
        super.endGame();
        sendMessage("DIE");
    }

    private void updateName() {
        sendMessage("NICK");
    }

    private void updateScores() {
        sendMessage("SCORES");
    }

    private void sendMessage(String message) {
        communicator.send(message);
    }

    private void receiveMessage(String message) {
        logger.info("Received message: {}", message);
        String[] components = message.split(" ", 2);
        String command = components[0];
        if (command.equals("SCORES") && components.length > 1) {
            String data = components[1];
            receiveScores(data);
        } else if (command.equals("NICK") && components.length > 1) {
            String name = components[1];
            if (!name.contains(":")) {
                setName(components[1]);
            }
        } else if (command.equals("MSG")) {
            String data = components[1];
            receiveMsg(data);
        }
    }

    private void receiveMsg(String data) {
        logger.info("Receieved chat: " + data);
        String[] components = data.split(":", 2);
        String username = components[0];
        if (username.equals(name.get())) {
            chatMode = false;
        }
        String msg = components[1];
        this.receivedMsg.setText("<" + username + " > " + msg);
        Multimedia.playAudio("message.wav");
    }

    private void setName(String name) {
        logger.info("Name: " + name);
        this.name.set(name);
        game.nameProperty().set(name);
    }

    private void receiveScores(String data) {
        logger.info("Received scores: {}", data);
        String[] scoreLines;
        scores.clear();
        for (String scoreLine : scoreLines = data.split("\\R")) {
            String[] components = scoreLine.split(":");
            String player = components[0];
            int score = Integer.parseInt(components[1]);
            String lives = components[2];
            if (lives.equals("DEAD")) {
                leaderboard.kill(player);
            }
            scores.add(new Pair<>(player, score));
        }
        scores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        scoreList.clear();
        scoreList.addAll(scores);
    }

    @Override
    protected void startChat() {
        chatMode = true;
        Platform.runLater(() -> {
            sendMsg.setVisible(true);
            sendMsg.setEditable(true);
            sendMsg.requestFocus();
        });
    }

    private void sendMsg(String message) {
        sendMsg.setEditable(false);
        sendMsg.setVisible(false);
        sendMessage("MSG " + message);
    }
}
