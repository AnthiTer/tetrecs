package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.Menu;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private BorderPane mainPane;

    private Menu gameMenu;

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Logo of the menu
        var image = new ImageView(Multimedia.getImage("TetrECS.png"));
        image.setFitWidth(gameWindow.getHeight());
        image.setPreserveRatio(true);
        mainPane.setCenter(image);

        //Animation of logo
        RotateTransition rotate = new RotateTransition(new Duration(3000), image);
        rotate.setCycleCount(-1);
        rotate.setFromAngle(-3);
        rotate.setToAngle(3);
        rotate.setAutoReverse(true);
        rotate.play();

        //components of the menu scene
        gameMenu = new Menu(250, 150);
        BorderPane.setAlignment(gameMenu, Pos.CENTER);
        gameMenu.add("Single Player", gameWindow::startChallenge);
        gameMenu.add("Multi Player", gameWindow::startLobby);
        gameMenu.add("How to Play", gameWindow::startInstructions);
        gameMenu.add("Exit", () -> App.getInstance().shutdown());

        mainPane.setBottom(gameMenu);
    }

    /**
     * Initialise the menu scene
     */
    @Override
    public void initialise() {
        Multimedia.startBackgroundMusic("menu.mp3");
        scene.setOnKeyPressed(this::handleKey);
    }


    /**
     * The functions of the keys when they're pressed in the keyboard
     * @param keyEvent
     */
    private void handleKey(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            App.getInstance().shutdown();
        } else if (keyEvent.getCode().equals(KeyCode.DOWN) || keyEvent.getCode().equals(KeyCode.S)) {
            gameMenu.down();
        } else if (keyEvent.getCode().equals(KeyCode.UP) || keyEvent.getCode().equals(KeyCode.W)) {
            gameMenu.up();
        } else if (keyEvent.getCode().equals(KeyCode.ENTER) || keyEvent.getCode().equals(KeyCode.SPACE)) {
            gameMenu.select();
        }
    }
}