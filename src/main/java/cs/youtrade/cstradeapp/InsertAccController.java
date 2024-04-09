package cs.youtrade.cstradeapp;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import cs.youtrade.cstradeapp.storage.TmApiRepository;
import cs.youtrade.cstradeapp.storage.UserData;
import cs.youtrade.cstradeapp.storage.steamauth.SteamGuardCodeService;
import cs.youtrade.cstradeapp.storage.steamauth.SteamSessionService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class InsertAccController {
    private final static Logger log = LoggerFactory.getLogger(InsertAccController.class);
    private static InsertAccController instance;
    private MainWindowController mainWindowController;
    private boolean isLoginFormOpen;
    @FXML
    private Label usernameLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private Label passwordLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label apiKeyLabel;
    @FXML
    private TextField apiKeyField;
    @FXML
    private Label filePathLabel;
    @FXML
    private Button browseButton;
    @FXML
    private PasswordField filePathField;
    @FXML
    private Label errField;
    private Stage stage;

    public static InsertAccController getInstance() {
        if (instance == null) {
            instance = new InsertAccController();
        }
        return instance;
    }

    public void initialize() {
        apiKeyLabel.setVisible(false);
        apiKeyField.setVisible(false);

        filePathLabel.setVisible(false);
        filePathField.setVisible(false);
        browseButton.setVisible(false);

        usernameField.textProperty().addListener((observable, oldValue, newValue) -> updateApiKeyVisibility());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> updateApiKeyVisibility());
        apiKeyField.textProperty().addListener((observable, oldValue, newValue) -> updateBrowseVisibility());
    }

    public void browseFile() throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            Gson gson = new Gson();
            SharedSecretWrapper sharedSecret = gson.fromJson(new JsonReader(new FileReader(selectedFile)), SharedSecretWrapper.class);
            filePathField.setText(sharedSecret.getSharedSecret());
        }
    }

    public void saveAndClose() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String apiKey = apiKeyField.getText();
        String sharedSecret = filePathField.getText();

        TmApiRepository.saveApiKey(new UserData(username, password, apiKey, sharedSecret));

        SteamGuardCodeService guardCodeService = new SteamGuardCodeService(sharedSecret);
        SteamSessionService steamSessionService = new SteamSessionService(username, password, guardCodeService);
        steamSessionService.run();
        Stage stage = (Stage) usernameField.getScene().getWindow();
        instance.setLoginFormOpen(false);
        stage.close();
    }

    private void updateApiKeyVisibility() {
        boolean hasLogin = !usernameField.getText().isEmpty();
        boolean hasPassword = !passwordField.getText().isEmpty();

        apiKeyLabel.setVisible(hasLogin && hasPassword);
        apiKeyField.setVisible(hasLogin && hasPassword);
    }

    private void updateBrowseVisibility() {
        boolean hasApiKey = !apiKeyField.getText().isEmpty();

        browseButton.setVisible(hasApiKey);
        filePathField.setVisible(hasApiKey);
        filePathLabel.setVisible(hasApiKey);
    }

    public MainWindowController getMainWindowController() {
        return mainWindowController;
    }

    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    public boolean isLoginFormOpen() {
        return isLoginFormOpen;
    }

    public void setLoginFormOpen(boolean loginFormOpen) {
        isLoginFormOpen = loginFormOpen;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(event -> instance.setLoginFormOpen(false));
    }

    public static class SharedSecretWrapper {
        @SerializedName("shared_secret")
        private String sharedSecret;

        public String getSharedSecret() {
            return sharedSecret;
        }

        public void setSharedSecret(String sharedSecret) {
            this.sharedSecret = sharedSecret;
        }
    }
}
