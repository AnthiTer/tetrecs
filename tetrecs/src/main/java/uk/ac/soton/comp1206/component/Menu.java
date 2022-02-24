package uk.ac.soton.comp1206.component;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import java.util.ArrayList;

public class Menu extends Group {

    private VBox box;

    private ArrayList<MenuItem> items = new ArrayList<>();

    private int selected = -1;

    public Menu(int width, int height) {
        box = new VBox(5.0);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("menu");
        setOnMouseMoved(e -> {
            for (MenuItem item : items)
                item.deselect();
        });
        getChildren().add(box);
    }

    private void paint() {
        for (MenuItem item : items)
            item.deselect();
        (items.get(selected)).select();
    }

    public void add(String label, Runnable action) {
        MenuItem item = new MenuItem(label);
        items.add(item);
        box.getChildren().add(item);
        item.setOnAction(action);
    }

    public void up() {
        if (selected > 0) {
            selected--;
        } else if (selected < 0) {
            selected = 0;
        }
        paint();
    }

    public void down() {
        if (selected < items.size() - 1)
            selected++;
        paint();
    }

    public void select() {
        (items.get(selected)).fire();
    }
}