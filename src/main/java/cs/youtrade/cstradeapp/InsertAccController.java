package cs.youtrade.cstradeapp;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import cs.youtrade.cstradeapp.storage.TmApiRepository;
import cs.youtrade.cstradeapp.storage.UserData;
import cs.youtrade.cstradeapp.storage.steamauth.SteamGuardCodeService;
import cs.youtrade.cstradeapp.util.TmCommunicationService;
import cs.youtrade.cstradeapp.util.TmPingException;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.authentication.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamuser.LogOnDetails;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOffCallback;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import in.dragonbra.javasteam.steam.steamclient.SteamClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackManager;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    private SteamClient steamClient;
    private SteamUnifiedMessages unifiedMessages;
    private CallbackManager manager;
    private SteamUser steamUser;
    private boolean isRunning;
    private SteamGuardCodeService guardCodeService;
    private String username;
    private String password;
    private String apiKey;
    private String sharedSecret;
    private String accessToken;
    private String refreshToken;

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
            try {
                SharedSecretWrapper sharedSecret = gson.fromJson(new JsonReader(new FileReader(selectedFile)), SharedSecretWrapper.class);
                filePathField.setText(sharedSecret.getSharedSecret());
            } catch (JsonSyntaxException e) {
                errField.setText("Choose .mafile");
            }
        }
    }

    public void saveAndClose() {
        this.username = usernameField.getText();
        this.password = passwordField.getText();
        this.apiKey = apiKeyField.getText();
        this.sharedSecret = filePathField.getText();
        if (!this.username.isEmpty() &&
                !this.password.isEmpty() &&
                !this.apiKey.isEmpty() &&
                !this.sharedSecret.isEmpty()
        ) {
            try {
                this.steamClient = new SteamClient();
                this.manager = new CallbackManager(this.steamClient);
                this.unifiedMessages = this.steamClient.getHandler(SteamUnifiedMessages.class);
                this.steamUser = this.steamClient.getHandler(SteamUser.class);

                this.manager.subscribe(ConnectedCallback.class, callback -> {
                    try {
                        onConnected(callback);
                    } catch (AuthenticationException e) {
                        throw new RuntimeException(e);
                    }
                });
                this.manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
                this.manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);
                this.manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

                this.isRunning = true;

                log.info("Connecting to steam...");
                this.guardCodeService = new SteamGuardCodeService(sharedSecret);
                this.steamClient.connect();
                while (this.isRunning) {
                    this.manager.runWaitCallbacks(1000L);
                }

                UserData userData = new UserData(username, password, apiKey, sharedSecret)
                        .setAccessToken(accessToken)
                        .setRefreshToken(refreshToken);
                try {
                    TmCommunicationService.pingTM(userData);
                } catch (TmPingException e) {
                    log.error("Error occurred while pinging TM: " + e.getMessage());
                    e.printStackTrace();
                }

                TmApiRepository.saveApiKey(username, userData);
                errField.setText("");
                Stage stage = (Stage) usernameField.getScene().getWindow();
                instance.setLoginFormOpen(false);

                stage.close();
            } catch (RuntimeException e) {
                e.printStackTrace();
                String errorText = "Cannot authenticate by given credentials.";
                log.error(errorText);
                errField.setText(errorText);
            } catch (ExecutionException e) {
                log.error("Error occurred while Pinging TM: " + e.getMessage());
            }
        } else {
            errField.setText("""
                    UserName, Password,
                    Tm Api Key and Shared Secret Fields
                    must not be empty""");
        }
    }

    private void onConnected(ConnectedCallback callback) throws AuthenticationException {
        System.out.println("Connected to Steam! Logging in " + username + "...");

        AuthSessionDetails authDetails = new AuthSessionDetails();
        authDetails.username = username;
        authDetails.password = password;
        authDetails.persistentSession = false;
        authDetails.authenticator = guardCodeService;

        SteamAuthentication auth = new SteamAuthentication(steamClient, unifiedMessages);
        CredentialsAuthSession authSession = auth.beginAuthSessionViaCredentials(authDetails);
        AuthPollResult pollResponse = authSession.pollingWaitForResultCompat();

        LogOnDetails details = new LogOnDetails();
        details.setLoginID(3);
        details.setUsername(pollResponse.getAccountName());
        details.setAccessToken(pollResponse.getRefreshToken());

        steamUser.logOn(details);
        accessToken = pollResponse.getAccessToken();
        refreshToken = pollResponse.getRefreshToken();
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        if (callback.getResult() != EResult.OK) {
            System.out.println("Unable to logon to Steam: " + callback.getResult() + " / " + callback.getExtendedResult());

            isRunning = false;
            return;
        }

        System.out.println("Successfully logged on!");
        isRunning = false;
        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());
        isRunning = false;
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam");
        isRunning = false;
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
