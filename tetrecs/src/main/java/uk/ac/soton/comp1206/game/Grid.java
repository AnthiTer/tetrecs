package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    private final int cols;

    private final int rows;

    private final SimpleIntegerProperty[][] grid;

    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        this.grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Clear the completed lines
     */
    public void clear() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++)
                grid[x][y].set(0);
        }
    }

    /**
     * Takes a GamePiece with a given x and y of the grid will return true or false if that piece can be played
     * @param piece
     * @param placeX
     * @param placeY
     * @return if GamePiece can be played or not
     */
    public boolean canPlayPiece(GamePiece piece, int placeX, int placeY) {
        int[][] blocks = piece.getBlocks();
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < (blocks[x]).length; y++) {
                int value = blocks[x][y];
                if (value != 0) {
                    int gridValue = get(x + placeX, y + placeY);
                    if (gridValue != 0)
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Takes a GamePiece with a given x and y of the grid will place that piece in the grid
     * @param piece
     * @param placeX
     * @param placeY
     * @return place of the GamePiece
     */
    public boolean playPiece(GamePiece piece, int placeX, int placeY) {
        int[][] blocks = piece.getBlocks();
        if (!canPlayPiece(piece, placeX, placeY))
            return false;
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < (blocks[x]).length; y++) {
                int value = blocks[x][y];
                if (value != 0)
                    set(x + placeX, y + placeY, value);
            }
        }
        return true;
    }

    /**
     * Ensures that the GamePiece will be placed by its centre
     * @param piece
     * @param placeX
     * @param placeY
     * @return centre of the GamePiece
     */
    public boolean addPieceCentered(GamePiece piece, int placeX, int placeY) {
        placeX--;
        placeY--;
        return playPiece(piece, placeX, placeY);
    }

}
