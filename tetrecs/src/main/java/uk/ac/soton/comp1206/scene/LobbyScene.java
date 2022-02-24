package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ChannelBox;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Lobby scene. Holds the UI for the lobby window.
 */
public class LobbyScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);
    protected ScheduledExecutorService executor;
    protected Communicator communicator;
    protected VBox channelList;
    protected VBox topBox;
    protected HBox mainBox;
    protected List<String> list;
    protected ScheduledFuture<?> loop;
    protected boolean channel;
    protected ChannelBox channelBox;

    /**
     * Create a new Lobby scene
     * @param gameWindow
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Lobby Scene");
        communicator = gameWindow.getCommunicator();
        executor = Executors.newSingleThreadScheduledExecutor();
        list = new ArrayList<>();
    }


    /**
     * Initialise the scene and start the lobby scene(chat)
     */
    @Override
    public void initialise() {
        communicator.addListener(message -> Platform.runLater(() -> handleMessage(message)));
        requestChannels();
    }

    /**
     * Build the Lobby window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        mainBox = new HBox();
        var leftPane = new VBox();
        channelList = new VBox();

        var textField = new TextField();
        var currentGames = new Text("Current Games");
        currentGames.getStyleClass().add("title");
        var hostNew = new Text("Host New Game");
        hostNew.getStyleClass().add("heading");

        hostNew.setOnMousePressed(e -> textField.setOpacity(1));

        textField.setOpacity(0);

        textField.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                textField.setOpacity(0);
                String name = textField.getText();
                textField.clear();
                communicator.send("CREATE " + name);
            }
        });

        leftPane.getChildren().addAll(currentGames, hostNew, textField, channelList);
        mainBox.getChildren().add(leftPane);

        mainBox.setMaxWidth(gameWindow.getWidth());
        mainBox.setMaxHeight(gameWindow.getHeight());
        mainBox.getStyleClass().add("menu-background");

        root.getChildren().add(mainBox);

        Platform.runLater(() -> scene.setOnKeyPressed(e -> handleKeyPress(e)));
    }

    public void handleKeyPress(KeyEvent e){
        if (e.getCode().equals(KeyCode.ESCAPE)) {
            executor.shutdownNow();
            gameWindow.startMenu();
        }
    }

    /**
     * Requests the list of users in the channel and updates the channels list
     */
    private void requestChannels() {
        if (channel) {
            communicator.send("USERS");
        }
        communicator.send("LIST");
        loop = executor.schedule(this::requestChannels, 2000, TimeUnit.MILLISECONDS);
    }

    /**
     * Handles network messages
     * @param s
     */
    private void handleMessage(String s) {
        String[] part = s.split(" ", 2);
        String header = part[0];

        //Get a list of all current channels
        if (header.equals("CHANNELS")) {
            if (part.length == 1) {
                channelList.getChildren().clear();
                return;
            }
            String message = part[1];
            List<String> l = Arrays.asList(message.split("\\s+"));

            if (!l.equals(list)) {
                channelList.getChildren().clear();

                for (String i : l) {
                    var x = new Text(i);
                    x.getStyleClass().add("channelItem");
                    x.setOnMouseClicked(e -> communicator.send("JOIN " + i));
                    channelList.getChildren().add(x);

                }
                list.clear();
                list.addAll(l);
            }
        }
        //Request to join the given channel, if not already in a channel
        if (header.equals("JOIN")) {
            channel = true;
            String channelName = part[1];

            channelBox = new ChannelBox(gameWindow, channelName);

            mainBox.getChildren().add(channelBox);
        }

        //Received if an action was not possible
        if (header.equals("ERROR")) {
            String message = part[1];
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(message);
            logger.error(message);
            alert.showAndWait();
        }

        //You are the host of this channel and can start a game
        if (header.equals("HOST")) {
            channelBox.revealStartButton();
        }

        //Leave a channel
        if (header.equals("PARTED")) {
            mainBox.getChildren().remove(channelBox);
            channel = false;
        }

        //User list in channel
        if (header.equals("USERS")) {
            String message = part[1];
            List<String> list = Arrays.asList(message.split("\\s+"));
            channelBox.updateUsers(list);
        }
        //Change nickname
        if (header.equals("NICK")) {
            String message = part[1];
        }

        //Start game
        if (header.equals("START")) {
            executor.shutdownNow();
            communicator.clearListeners();
            gameWindow.startMultiplayer();
        }

        //Received a chat message from the player
        if (header.equals("MSG")) {
            String[] subParts = part[1].split(":");
            if (subParts.length == 2) {
                String sender = subParts[0];
                String message = subParts[1];
                channelBox.addMessage(sender, message);
            }
        }
    }
}
