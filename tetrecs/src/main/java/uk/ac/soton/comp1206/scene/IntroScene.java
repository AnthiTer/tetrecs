package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.Menu;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public class IntroScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(IntroScene.class);

    private ImageView imageView;

    private ImageView ecsLogo;

    private MediaPlayer player;

    private Menu gameMenu;

    private SequentialTransition sequence;

    /**
     * Create a new Intro scene
     * @param gameWindow the Game Window
     */
    public IntroScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Intro Scene");
        Multimedia.playAudio("intro.mp3");
    }

    /**
     * Initialise the scene
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                Multimedia.stopAll();
                sequence.stop();
                gameWindow.startMenu();
            }
        });
    }

    /**
     * Build the Intro window
     */
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        StackPane introPane = new StackPane();
        introPane.setMaxWidth(gameWindow.getWidth());
        introPane.setMaxHeight(gameWindow.getHeight());
        introPane.getStyleClass().add("intro");

        //ECS logo and the fade-in/fade-out effects
        ImageView logo = new ImageView(Multimedia.getImage("ECSGames.png"));
        logo.setFitWidth((gameWindow.getWidth() / 2.5));
        logo.setPreserveRatio(true);
        logo.setOpacity(0);
        introPane.getChildren().add(logo);

        root.getChildren().add(introPane);

        FadeTransition fadeIn = new FadeTransition(new Duration(1500), logo);

        fadeIn.setToValue(1.0D);
        PauseTransition pause = new PauseTransition(new Duration(1000));

        FadeTransition fadeOut = new FadeTransition(new Duration(300), logo);
        fadeOut.setToValue(0);

        sequence = new SequentialTransition(fadeIn, pause, fadeOut);
        sequence.play();

        sequence.setOnFinished(e -> gameWindow.startMenu());
    }

}
