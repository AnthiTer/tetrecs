package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoreBox;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import uk.ac.soton.comp1206.utility.Utility;
import java.util.*;

/**
 * The Score scene. Holds the UI that displays the scores after each game.
 */
public class ScoreScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(ScoreScene.class);

    private final Game game;

    private Timer timer;

    private VBox scoreBox;

    private Text hiscoreText;

    private Communicator communicator;

    private boolean newScore = false;

    private boolean newRemoteScore = false;

    private Pair<String, Integer> myScore;

    private BorderPane mainPane;

    private ScoreBox highscoreBlock1;

    private ScoreBox highscoreBlock2;

    private ArrayList<Pair<String, Integer>> remoteScores = new ArrayList<>();

    private ObservableList<Pair<String, Integer>> scoreList;

    private ObservableList<Pair<String, Integer>> remoteScoreList;

    private StringProperty myName = new SimpleStringProperty("");

    private BooleanProperty showScores = new SimpleBooleanProperty(false);

    private boolean waitingForScores = true;


    /**
     * /**
     * Create a new Score scene
     * @param gameWindow
     * @param game
     */
    public ScoreScene(GameWindow gameWindow, Game game) {
        super(gameWindow);
        logger.info("Creating Score Scene");
        communicator = gameWindow.getCommunicator();
        this.game = game;
    }

    /**
     * Initialise the scene and show the scores.
     */
    @Override
    public void initialise() {
        Multimedia.playAudio("explode.wav");
        Multimedia.startBackgroundMusic("end.wav", false);
        communicator.addListener(message -> Platform.runLater(() -> receiveMessage(message.trim())));
        if (!game.getScores().isEmpty()) {
            myName.set(game.nameProperty().getValue());
        }
        communicator.send("HISCORES");
    }

    /**
     * Build the Score window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        //Set window and background
        StackPane scorePane = new StackPane();
        scorePane.setMaxWidth(gameWindow.getWidth());
        scorePane.setMaxHeight(gameWindow.getHeight());
        scorePane.getStyleClass().add("menu-background");
        root.getChildren().add(scorePane);

        mainPane = new BorderPane();
        scorePane.getChildren().add(mainPane);

        scoreBox = new VBox();
        scoreBox.setAlignment(Pos.TOP_CENTER);
        scoreBox.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));
        scoreBox.setSpacing(20.0);
        mainPane.setCenter(scoreBox);

        //Game logo
        ImageView image = new ImageView(Multimedia.getImage("TetrECS.png"));
        image.setFitWidth(gameWindow.getWidth() * 0.7);
        image.setPreserveRatio(true);
        scoreBox.getChildren().add(image);

        Text gameOverText = new Text("Game Over");
        gameOverText.setTextAlignment(TextAlignment.CENTER);
        VBox.setVgrow(gameOverText, Priority.ALWAYS);
        gameOverText.getStyleClass().add("bigtitle");
        scoreBox.getChildren().add(gameOverText);

        hiscoreText = new Text("High Scores");
        hiscoreText.setTextAlignment(TextAlignment.CENTER);
        VBox.setVgrow(hiscoreText, Priority.ALWAYS);
        hiscoreText.getStyleClass().add("title");
        hiscoreText.setFill(Color.LIGHTSEAGREEN);
        scoreBox.getChildren().add(hiscoreText);

        GridPane scoreGrid = new GridPane();
        scoreGrid.visibleProperty().bind(showScores);
        scoreGrid.setAlignment(Pos.CENTER);
        scoreGrid.setHgap(100.0);
        scoreBox.getChildren().add(scoreGrid);

        Text localScoresLabel = new Text("Local Scores");
        localScoresLabel.setTextAlignment(TextAlignment.CENTER);
        localScoresLabel.getStyleClass().add("heading");
        scoreGrid.add(localScoresLabel, 0, 0);

        Text remoteScoresLabel = new Text("Online Scores");
        remoteScoresLabel.setTextAlignment(TextAlignment.CENTER);
        remoteScoresLabel.getStyleClass().add("heading");
        scoreGrid.add(remoteScoresLabel, 1, 0);

        //Local score box
        highscoreBlock1 = new ScoreBox();
        Button button1 = new Button("Button");
        highscoreBlock1.getChildren().add(button1);
        GridPane.setHalignment(highscoreBlock1, HPos.CENTER);
        scoreGrid.add(highscoreBlock1, 0, 1);

        //Online score box
        highscoreBlock2 = new ScoreBox();
        Button button2 = new Button("Button");
        highscoreBlock2.getChildren().add(button2);
        GridPane.setHalignment(highscoreBlock2, HPos.CENTER);
        scoreGrid.add(highscoreBlock2, 1, 1);

        //If the score list is empty or scores.txt isn't created, then generate some high scores from Utility class
        if (game.getScores().isEmpty()) {
            scoreList = FXCollections.observableArrayList(Utility.loadScores());
        } else {
            //If there are already highscores, add the new one
            scoreList = FXCollections.observableArrayList(game.getScores());
            localScoresLabel.setText("This game");
        }

        scoreList.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        remoteScoreList = FXCollections.observableArrayList(remoteScores);

        SimpleListProperty<Pair<String, Integer>> wrapper = new SimpleListProperty(scoreList);
        highscoreBlock1.scoreProperty().bind(wrapper);
        highscoreBlock1.nameProperty().bind(myName);

        SimpleListProperty<Pair<String, Integer>> wrapper2 = new SimpleListProperty(remoteScoreList);
        highscoreBlock2.scoreProperty().bind(wrapper2);
        highscoreBlock2.nameProperty().bind(myName);
    }

    public void startTimer(int delay) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        TimerTask task = new TimerTask(){
            @Override
            public void run() {
                Platform.runLater(ScoreScene.this::returnToMenu);
            }
        };
        timer = new Timer();
        timer.schedule(task, delay);
    }

    /**
     * Return to main menu after timer ends
     */
    public void returnToMenu() {
        if (newScore)
            return;
        if (timer != null)
            timer.cancel();
        gameWindow.startMenu();
    }

    /**
     * Checks if there are local or online highscores
     */
    public void checkForHiScore() {
        logger.info("Checking for high score");
        if (!game.getScores().isEmpty()) {
            reveal();
            return;
        }

        int currentScore = game.getScore();
        int counter = 0;
        int remoteCounter = 0;
        int lowestScore = 0;

        if (scoreList.size() > 0) {
            lowestScore = (Integer) ((Pair) scoreList.get(scoreList.size() - 1)).getValue();
        }
        if (scoreList.size() < 10) {
            newScore = true;
        }
        int lowestScoreRemote = 0;
        if (remoteScores.size() > 0) {
            lowestScoreRemote = remoteScores.get(remoteScores.size() - 1).getValue();
        }
        if (remoteScores.size() < 10) {
            newRemoteScore = true;
        }
        if (currentScore > lowestScore) {
            for (Pair pair : scoreList) {
                if ((Integer) pair.getValue() < currentScore) {
                    newScore = true;
                    logger.info("New local high score:)");
                    break;
                }
                counter++;
            }
        }
        if (currentScore > lowestScoreRemote) {
            for (Pair pair : remoteScores) {
                if ((Integer) pair.getValue() < currentScore) {
                    newRemoteScore = true;
                    logger.info("New remote high score:)");
                    break;
                }
                ++remoteCounter;
            }
        }
        //If you have a local or remote highscore, a screen is shown to write your name and add it to the list
        if (newScore || newRemoteScore) {
            hiscoreText.setText("You got a High Score!");

            TextField name = new TextField();
            name.setPromptText("Enter your name");
            name.setPrefWidth(gameWindow.getWidth() / 2);
            name.requestFocus();
            scoreBox.getChildren().add(2, name);

            Button button = new Button("Submit");
            button.setDefaultButton(true);
            scoreBox.getChildren().add(3, button);

            int addResult = counter;
            int addRemoteResult = remoteCounter;

            button.setOnAction(e -> {
                String myName = name.getText().replace(":", "");
                this.myName.set(myName);
                scoreBox.getChildren().remove(2);
                scoreBox.getChildren().remove(2);
                myScore = new Pair<>(myName, currentScore);
                if (newScore) {
                    scoreList.add(addResult, myScore);
                }
                if (newRemoteScore) {
                    remoteScoreList.add(addRemoteResult, myScore);
                }
                communicator.send("HISCORE " + myName + ":" + currentScore);
                Utility.writeScores(scoreList);
                communicator.send("HISCORES");
                newScore = false;
                newRemoteScore = false;
            });
        } else {
            logger.info("No high score:(");
            reveal();
        }
    }

    /**
     * Reveal the High Scores (both local and online)
     */
    public void reveal() {
        startTimer(20000);
        scene.setOnKeyPressed(e -> returnToMenu());
        showScores.set(true);
        highscoreBlock1.reveal();
        highscoreBlock2.reveal();
    }

    /**
     * Receive online scores from communicator
     * @param message
     */
    private void receiveMessage(String message) {
        logger.info("Received message: {}", message);
        String[] components = message.split(" ", 2);
        String command = components[0];
        if (command.equals("HISCORES"))
            if (components.length > 1) {
                String data = components[1];
                receiveScores(data);
            } else {
                receiveScores("");
            }
    }

    /**
     * Checks online score list (if you have possibly beaten another ones highscore)
     * @param data
     */
    private void receiveScores(String data) {
        logger.info("Received scores: {}", data);
        remoteScores.clear();
        String[] scoreLines = data.split("\\R");
        for (String scoreLine : scoreLines) {
            String[] components = scoreLine.split(":", 2);
            String player = components[0];
            int score = Integer.parseInt(components[1]);
            remoteScores.add(new Pair<>(player, score));
        }
        remoteScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        remoteScoreList.clear();
        remoteScoreList.addAll(remoteScores);
        if (waitingForScores) {
            checkForHiScore();
            waitingForScores = false;
            return;
        }
        reveal();
    }
}
