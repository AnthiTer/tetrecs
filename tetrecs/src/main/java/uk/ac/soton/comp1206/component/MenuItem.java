package uk.ac.soton.comp1206.component;

import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.utility.Multimedia;

public class MenuItem extends Group {
    private Text text;

    private Rectangle selection;

    private Runnable action;

    public MenuItem(String name) {
        text = new Text(name);
        text.getStyleClass().add("menuItem");
        getChildren().add(text);
    }

    public void select() {
        text.getStyleClass().add("selected");
    }

    public void deselect() {
        text.getStyleClass().remove("selected");
    }

    public void setOnAction(Runnable action) {
        this.action = action;
        setOnMouseClicked(e -> {
            Multimedia.playAudio("rotate.wav");
            action.run();
        });
    }

    public void fire() {
        action.run();
    }
}
