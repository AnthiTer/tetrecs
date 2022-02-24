package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Local and Online HighScore Boxes (for single player and multiplayer
 */
public class ScoreBox extends VBox{

    private static final Logger logger = LogManager.getLogger(ScoreBox.class);

    public final SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty();

    private ArrayList<HBox> scoreBoxes = new ArrayList<>();

    private int scoresToShow = 10;

    private boolean autoReveal = false;

    private StringProperty name = new SimpleStringProperty();

    private ArrayList<String> deadPlayers = new ArrayList<>();

    public ScoreBox() {
        getStyleClass().add("scorelist");
        setAlignment(Pos.CENTER);
        setSpacing(2.0);

        scores.addListener((ListChangeListener<? super Pair<String, Integer>>) c -> updateList());
        name.addListener(e -> updateList());
    }

    public void setAutoReveal(boolean autoReveal) {
        this.autoReveal = autoReveal;
    }

    public void setScoresToShow(int amount) {
        this.scoresToShow = amount;
    }


    /**
     * Animation to reveal the highscores
     */
    public void reveal() {
        logger.info("Revealing {} scores", scoreBoxes.size());
        ArrayList transitions = new ArrayList();

        for (HBox scoreBox : scoreBoxes) {
            FadeTransition fader = new FadeTransition(new Duration(300.0), scoreBox);
            fader.setFromValue(0.0);
            fader.setToValue(1.0);
            transitions.add(fader);
        }

        SequentialTransition transition = new SequentialTransition((Animation[])transitions.toArray(Animation[]::new));
        transition.play();
    }

    /**
     * Update the lists of highscores
     */
    public void updateList() {
        logger.info("Updating score list");
        scoreBoxes.clear();

        getChildren().clear();
        int counter = 0;
        for (Pair pair : scores) {
            counter++;
            if (counter > scoresToShow)
                break;
            HBox scoreBox = new HBox();
            scoreBox.setOpacity(0.0);
            scoreBox.getStyleClass().add("scoreitem");
            scoreBox.setAlignment(Pos.CENTER);
            scoreBox.setSpacing(10.0);

            Color colour = GameBlock.COLOURS[counter];
            Text player = new Text(pair.getKey() + ":");
            player.getStyleClass().add("scorer");

            if (pair.getKey().equals(name.get())) {
                player.getStyleClass().add("myscore");
            }
            if (deadPlayers.contains(pair.getKey())) {
                player.getStyleClass().add("deadscore");
            }

            player.setTextAlignment(TextAlignment.CENTER);
            player.setFill(colour);
            HBox.setHgrow(player, Priority.ALWAYS);

            Text points = new Text(((Integer)pair.getValue()).toString());
            points.getStyleClass().add("points");
            points.setTextAlignment(TextAlignment.CENTER);
            points.setFill(colour);
            HBox.setHgrow(points, Priority.ALWAYS);

            scoreBox.getChildren().addAll(player, points);
            getChildren().add(scoreBox);
            scoreBoxes.add(scoreBox);
        }

        if (this.autoReveal)
            reveal();
    }

    public ListProperty<Pair<String, Integer>> scoreProperty() {
        return scores;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void kill(String player) {
        deadPlayers.add(player);
    }
}
