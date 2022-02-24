package uk.ac.soton.comp1206.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickListener;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    private final int cols;

    private final int rows;

    private final double width;

    private final double height;

    final Grid grid;

    GameBlock[][] blocks;

    private BlockClickedListener blockClickedListener;

    private RightClickListener rightClickedListener;

    private boolean readOnly = false;

    private boolean showCentre = false;

    private BooleanProperty keyboardMode = new SimpleBooleanProperty(false);

    private GameBlock hover;

    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        //Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols,rows);

        //Build the GameBoard
        build();
    }

    public void addPiece(GamePiece piece, int placeX, int placeY) {
        grid.playPiece(piece, placeX, placeY);
    }

    public void clear() {
        grid.clear();
    }

    public void setPiece(GamePiece piece) {
        clear();
        addPiece(piece);
    }

    public void addPiece(GamePiece piece) {
        addPiece(piece, 0, 0);
    }

    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        logger.info("Building grid: {} x {}",cols,rows);
        getStyleClass().add("gameboard");
        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                createBlock(x,y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                blockClicked(block);
            } else {
                rightClick(block);
            }
        });
        block.setOnMouseEntered(e -> hover(block));
        block.setOnMouseExited(e -> unhover(block));

        return block;
    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        blockClickedListener = listener;
    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnRightClick(RightClickListener listener) {
        rightClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param block block clicked on
     */
    private void blockClicked(GameBlock block) {
        logger.info("Block clicked: {}", block);

        if(blockClickedListener != null) {
            blockClickedListener.blockClicked(block);
        }
    }

    /**
     * Triggered when a block is right clicked. Call the attached listener.
     * @param block block right clicked on
     */
    public void rightClick(GameBlock block) {
        if (rightClickedListener != null)
            rightClickedListener.rightClicked();
    }

    public void hover(GameBlock block) {
        if (readOnly)
            return;
        if (keyboardMode.get() && hover != null)
            unhover(hover);
        hover = block;
        block.setHovering(true);
    }

    public void unhover(GameBlock block) {
        if (readOnly)
            return;
        block.setHovering(false);
    }

    public void highlight(GameBlock block) {
        block.highlight();
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Show the centre of each game piece
     * @param showCentre
     */
    public void showCentre(boolean showCentre) {
        this.showCentre = showCentre;
        if (showCentre) {
            double midX = Math.ceil(rows / 2.0) - 1.0;
            double midY = Math.ceil(cols / 2.0) - 1.0;
            blocks[(int) midX][(int) midY].setCentre(true);
        }
    }

    public BooleanProperty keyboardModeProperty() {
        return keyboardMode;
    }

}
