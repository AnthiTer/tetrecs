package uk.ac.soton.comp1206.component;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import java.util.ArrayList;
import java.util.List;

/**
 * Essentially the chat box of the channel selected
 */
public class ChannelBox extends VBox {
   protected String channelName;
    private VBox messages;
    protected HBox users;
    protected List<String> list;
    protected ScrollPane scrollpane;
    private Node start;
    protected GameWindow gameWindow;
    protected Communicator communicator;

    public ChannelBox(GameWindow gameWindow, String channelName) {
        this.gameWindow = gameWindow;
        this.channelName = channelName;
        this.communicator = gameWindow.getCommunicator();
        list = new ArrayList<>();
        build();
    }

    public void build() {
        scrollpane = new ScrollPane();
        scrollpane.setFitToHeight(true);
        scrollpane.setFitToWidth(true);


        scrollpane.getStyleClass().add("gameBox");

        var entry = new TextField();
        messages = new VBox(2);

        entry.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                String text = entry.getText();
                entry.clear();
                String[] part = text.split(" ", 2);
                if (part[0].equals("/nick")) {
                    if (part.length > 1) {
                        communicator.send("NICK " + part[1]);
                    }
                    return;
                }
                communicator.send("MSG " + text);
            }
        });

        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(messages, Priority.ALWAYS);
        VBox.setVgrow(scrollpane, Priority.ALWAYS);

        users = new HBox(4);

        var buttons = new HBox(4);

        var leave = new Button("Leave Game");

        leave.setOnMouseClicked(e -> communicator.send("PART"));

        start = new Button("Start Game");

        start.setOnMouseClicked(e -> communicator.send("START"));

        start.setOpacity(0);

        buttons.getChildren().addAll(leave, start);
        scrollpane.setContent(messages);

        getChildren().addAll(users, scrollpane, buttons, entry);
    }

    public void addMessage(String nickname, String message) {
        var messageObject = new Text("<" + nickname+ "> : " + message);
        messageObject.getStyleClass().add("messages");
        messages.getChildren().add(messageObject);
        Multimedia.playAudio("message.wav");
        scrollpane.layout();
        scrollpane.setVvalue(1);
    }

    public void updateUsers(List<String> l) {

        if (!l.equals(list)) {
            users.getChildren().clear();
            for (String i : l) {
                var x = new Text(i);
                x.getStyleClass().add("channelItem");
                users.getChildren().add(x);
            }
            list.clear();
            list.addAll(l);
        }

    }

    public void revealStartButton() {
        start.setOpacity(1);
    }
}
