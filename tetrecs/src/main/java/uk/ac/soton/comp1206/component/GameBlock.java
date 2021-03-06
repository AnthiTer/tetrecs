package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;
    private final double width;
    private final double height;
    private final int x;
    private final int y;
    private final IntegerProperty value = new SimpleIntegerProperty(0);
    private boolean centre = false;
    private boolean hovering;
    private Highlight timer;

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
        //If the block has a centre, paint it
        if (centre) {
             paintCentre();
        }
        //If you hover over a block, paint it
        if (hovering) {
            paintHover();
        }
    }

    /**
     * Paint the centre of the block canvas
     */
    public void paintCentre() {
        var gc = getGraphicsContext2D();
        gc.setFill(Color.color(1.0, 1.0, 1.0, 0.5));
        gc.fillOval(width / 4.0, height / 4.0, width / 2.0, height / 2.0);
    }
    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0, 0, width, height);

        Color start = Color.color(0.0D, 0.0D, 0.0D, 0.3D);
        Color end = Color.color(0.0D, 0.0D, 0.0D, 0.7D);

        //Fill
        gc.setFill(new LinearGradient(0.0, 0.0, 1.0, 1.0, true, CycleMethod.REFLECT, new Stop(0.0, start), new Stop(1.0, end)));
        gc.fillRect(0.0, 0.0, width, height);

        //Border
        gc.setStroke(Color.color(1.0, 1.0, 1.0, 0.5));
        gc.strokeRect(0.0, 0.0, width, height);
    }

        /**
         * Paint this canvas with the given colour
         * @param colour the colour to paint
         */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        gc.clearRect(0.0, 0.0, width, height);
        gc.setFill(colour);
        gc.fillRect(0.0, 0.0, width, height);
        gc.setFill(Color.color(1.0, 1.0, 1.0, 0.1));
        gc.fillPolygon(new double[]{0.0, 0.0, width}, new double[]{0.0, height, height}, 3);
        gc.setFill(Color.color(1.0, 1.0, 1.0, 0.3));
        gc.fillRect(0.0, 0.0, width, 3.0);
        gc.setFill(Color.color(1.0, 1.0, 1.0, 0.3));
        gc.fillRect(0.0, 0.0, 3.0, height);
        gc.setFill(Color.color(0.0, 0.0, 0.0, 0.3));
        gc.fillRect(width - 3.0, 0.0, width, height);
        gc.setFill(Color.color(0.0, 0.0, 0.0, 0.3));
        gc.fillRect(0.0, height - 3.0, width, height);
        gc.setStroke(Color.color(0.0, 0.0, 0.0, 0.5));
        gc.strokeRect(0.0, 0.0, width, height);
    }

    /**
     * Paint the block canvas when you hover on top of it
     */
    public void paintHover() {
        var gc = getGraphicsContext2D();
        gc.setFill(Color.color(1.0, 1.0, 1.0, 0.5));
        gc.fillRect(0.0, 0.0, width, height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) { value.bind(input); }

    public void setCentre(boolean centre) {
        this.centre = true;
        paint();
    }

    public void setHovering(boolean hovering) {
        this.hovering = hovering;
        paint();
    }

    public void highlight() {
        timer = new Highlight();
        timer.start();
    }


    /**
     * Class to set the functions of the fade out method that lashes and then fades out to indicate a cleared block.
     * (useful to have it inside the GameBlock class so its functions are accessible)
     */
    public class Highlight extends AnimationTimer {
        double opacity = 1.0D;

        public void handle(long now) {
            fadeOut();
        }

        private void fadeOut() {
            GameBlock.this.paintEmpty();
            opacity -= 0.02;
            if (opacity <= 0.0) {
                stop();
                GameBlock.this.timer = null;
                return;
            }
            var gc = GameBlock.this.getGraphicsContext2D();
            gc.setFill(Color.color(0.0, 1.0, 0.0, opacity));
            gc.fillRect(0.0, 0.0, GameBlock.this.width, GameBlock.this.height);
        }
    }

}
