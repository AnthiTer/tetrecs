package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    private BorderPane mainPane;

    /**
     * Create a new Instructions scene
     * @param gameWindow the Game Window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        scene.setOnKeyPressed(e -> gameWindow.startMenu());
    }

    /**
     * Build the Instructions window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var instructionsPane = new StackPane();
        instructionsPane.setMaxWidth(this.gameWindow.getWidth());
        instructionsPane.setMaxHeight(this.gameWindow.getHeight());
        instructionsPane.getStyleClass().add("menu-background");
        root.getChildren().add(instructionsPane);

        mainPane = new BorderPane();
        instructionsPane.getChildren().add(mainPane);

        var vBox = new VBox();
        BorderPane.setAlignment(vBox, Pos.CENTER);
        vBox.setAlignment(Pos.TOP_CENTER);
        mainPane.setCenter(vBox);

        Text instructions = new Text("Instructions");
        instructions.getStyleClass().add("heading");
        vBox.getChildren().add(instructions);

        //Explanation of the game
        Text instructionText = new Text("TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
        TextFlow instructionFlow = new TextFlow(new Node[] {instructionText});
        instructionText.getStyleClass().add("instructions");
        instructionText.setTextAlignment(TextAlignment.CENTER);
        instructionFlow.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().add(instructionFlow);

        //Insert image with instructions and use of buttons
        ImageView instructionImage = new ImageView(getClass().getResource("/images/Instructions.png").toExternalForm());
        instructionImage.setFitWidth(gameWindow.getWidth() / 1.5);
        instructionImage.setPreserveRatio(true);
        vBox.getChildren().add(instructionImage);

        Text pieces = new Text("Game Pieces");
        pieces.getStyleClass().add("heading");
        vBox.getChildren().add(pieces);

        var gridPane = new GridPane();
        vBox.getChildren().add(gridPane);

        //set game pieces instructions and text on centre
        double padding = (gameWindow.getWidth() - gameWindow.getWidth() / 13 * 5 ) / 2.0;
        gridPane.setPadding(new Insets(0.0, padding, 0.0, padding));
        gridPane.setVgap(10.0);
        gridPane.setHgap(10.0);

        //grid pane of game pieces (game boards)
        int x = 0;
        int y = 0;
        for (int i = 0; i < 15; i++) {
            GamePiece piece = GamePiece.createPiece(i);
            GameBoard gameBoard = new GameBoard(3, 3, (gameWindow.getWidth() / 15), (gameWindow.getWidth() / 15));
            gameBoard.setPiece(piece);
            gameBoard.setReadOnly(true);
            gridPane.add(gameBoard, x, y);
            x++;
            if (x == 5) {
                x = 0;
                y++;
            }
        }
    }
}
