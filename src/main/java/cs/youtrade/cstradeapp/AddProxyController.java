package cs.youtrade.cstradeapp;

import cs.youtrade.cstradeapp.storage.TmApiRepository;
import cs.youtrade.cstradeapp.storage.UserData;
import cs.youtrade.cstradeapp.util.ProxyModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;

public class AddProxyController {
    private Stage stage;
    private UserData userData;
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

    public AddProxyController() {
    }

    public void initData(UserData userData) {
        this.userData = userData;
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
            showError("IP и порт должны быть заполнены");
            return;
        }

        if ((!username.isEmpty() && password.isEmpty()) ||
                (username.isEmpty() && !password.isEmpty())) {
            showError("Нужно указать и логин и пароль");
            return;
        }

        ProxyModel proxyModel;
        if (username.isEmpty()) {
            proxyModel = new ProxyModel(ipAddress, Integer.parseInt(port));
        } else {
            proxyModel = new ProxyModel(ipAddress, Integer.parseInt(port), username, password);
        }

        CloseableHttpClient httpClient;
        HttpHost proxy = new HttpHost(proxyModel.getHostname(), proxyModel.getPort());
        if (username.isEmpty()) {
            httpClient = HttpClients.custom()
                    .setProxy(proxy)
                    .build();
        } else {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(proxy),
                    new UsernamePasswordCredentials(proxyModel.getUserName(), proxyModel.getPassword()));

            httpClient = HttpClients.custom()
                    .setProxy(proxy)
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .build();
        }

        try {
            httpClient.execute(new HttpGet("https://example.com/"));

            userData.setProxyModel(proxyModel);
            errorLabel.setText("");
            TmApiRepository.saveDataToFile();
            stage.close();
        } catch (IOException e) {
            errorLabel.setText("Укажите действительный прокси");
        }
    }

    private void showError(String errorMessage) {
        errorLabel.setText(errorMessage);
    }
}
