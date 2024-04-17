package cs.youtrade.cstradeapp;

import cs.youtrade.cstradeapp.storage.TmApiRepository;
import cs.youtrade.cstradeapp.storage.UserData;
import cs.youtrade.cstradeapp.util.PingTask;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainWindowController {
    private final static Logger log = LoggerFactory.getLogger(MainWindowController.class);
    private PingTask pingTask;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox buttonContainer;

    public void initialize() {
        ObservableMap<String, UserData> keys = TmApiRepository.getKeys();
        for (String key : keys.keySet()) {
            addButton(key);
        }

        keys.addListener((MapChangeListener.Change<? extends String, ? extends UserData> change) -> {
            if (change.wasAdded()) {
                addButton(change.getKey());
            }
        });

        keys.addListener((MapChangeListener.Change<? extends String, ? extends UserData> change) -> {
            if (change.wasRemoved()) {
                removeButton(change.getKey());
            }
        });

        pingTask = new PingTask(this);
        pingTask.start();
    }

    @FXML
    protected void onOpenInsertAccForm() {
        try {
            InsertAccController insertAccController = InsertAccController.getInstance();
            if (!insertAccController.isLoginFormOpen()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("insert-acc.fxml"));
                Parent root = loader.load();
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Add account");
                stage.setResizable(false);

                insertAccController.setMainWindowController(this);
                insertAccController.setLoginFormOpen(true);
                insertAccController.setStage(stage);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addButton(String key) {
        UserData userData = TmApiRepository.getKeys().get(key);

        Button userButton = new Button(key);
        userButton.getStyleClass().removeAll("button");
        userButton.getStyleClass().add(userData.isWorks() ? "button-green" : "button-red");
        userButton.setMinWidth(120);
        userButton.setOnAction(event -> {
            userData.setWorks(!userData.isWorks());
            log.info(userData.getuName() + " - works: " + userData.isWorks());
            userButton.getStyleClass().removeAll(userData.isWorks() ? "button-red" : "button-green");
            userButton.getStyleClass().add(userData.isWorks() ? "button-green" : "button-red");
            TmApiRepository.saveDataToFile();
        });

        HBox.setHgrow(userButton, Priority.ALWAYS);
        Button addProxyButton = createAddProxyButton(userData);
        addProxyButton.setMaxWidth(60);
        HBox.setHgrow(addProxyButton, Priority.ALWAYS);
        Button deleteButton = createDeleteButton(key);

        HBox hbox = new HBox(userButton, addProxyButton, deleteButton);
        hbox.setSpacing(10.0);
        hbox.getStyleClass().add("hbox-background");
        buttonContainer.getChildren().add(hbox);
    }

    private void removeButton(String key) {
        buttonContainer.getChildren().removeIf(node -> {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                for (Node child : hbox.getChildren()) {
                    if (child instanceof Button) {
                        Button button = (Button) child;
                        if (button.getText().equals(key)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    private Button createDeleteButton(String key) {
        SVGPath svg = new SVGPath();
        svg.setContent("M 10 2 L 9 3 L 3 3 L 3 5 L 4.109375 5 L 5.8925781 20.255859 L 5.8925781 20.263672 C 6.023602 21.250335 6.8803207 22 7.875 22 L 16.123047 22 C 17.117726 22 17.974445 21.250322 18.105469 20.263672 L 18.107422 20.255859 L 19.890625 5 L 21 5 L 21 3 L 15 3 L 14 2 L 10 2 z M 6.125 5 L 17.875 5 L 16.123047 20 L 7.875 20 L 6.125 5 z");
        svg.getStyleClass().add("delete-icon");

        Button deleteButton = new Button();
        deleteButton.setGraphic(svg);
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setTooltip(new Tooltip("Delete"));

        deleteButton.setOnAction(event -> TmApiRepository.removeUser(key));

        return deleteButton;
    }

    private Button createAddProxyButton(UserData userData) {
        Button addProxyButton = new Button("Add Proxy");
        addProxyButton.setOnAction(event -> openAddProxyWindow(userData));
        return addProxyButton;
    }

    private void openAddProxyWindow(UserData userData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("add-proxy.fxml"));
            Parent root = loader.load();

            AddProxyController controller = loader.getController();
            controller.initData(userData);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Proxy");
            stage.setResizable(false);

            controller.setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}