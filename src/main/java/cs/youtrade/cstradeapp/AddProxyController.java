package cs.youtrade.cstradeapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddProxyController {
    private static AddProxyController instance;
    private Stage stage;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField ipAddressTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;

    private AddProxyController() {
    }

    public static AddProxyController getInstance() {
        if (instance == null) {
            instance = new AddProxyController();
        }
        return instance;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void saveButtonClicked() {
        String ipAddress = ipAddressTextField.getText();
        String port = portTextField.getText();
        String username = usernameTextField.getText();
        String password = passwordField.getText();

        if (ipAddress.isEmpty() || port.isEmpty()) {
            showError("IP-адрес и порт должны быть заполнены");
            return;
        }

        if ((!username.isEmpty() && password.isEmpty()) ||
                (username.isEmpty() && !password.isEmpty())) {
            showError("Необходимо указать и логин и пароль");
            return;
        }

        errorLabel.setText("");
        stage.close();
    }

    private void showError(String errorMessage) {
        errorLabel.setText(errorMessage);
    }
}
