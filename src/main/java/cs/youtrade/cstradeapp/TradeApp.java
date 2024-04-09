package cs.youtrade.cstradeapp;

import cs.youtrade.cstradeapp.storage.TmApiRepository;
import cs.youtrade.cstradeapp.storage.UserData;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TradeApp extends Application {
    private static List<UserData> keys;

    @Override
    public void start(Stage stage) throws IOException {
        keys = TmApiRepository.loadApiKeys();
        FXMLLoader fxmlLoader = new FXMLLoader(TradeApp.class.getResource("main-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("TmTradeApp");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            Window.getWindows().forEach(window -> ((Stage) window).close());
            System.exit(0);
        });
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        TmApiRepository.saveDataToFile(keys);
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}