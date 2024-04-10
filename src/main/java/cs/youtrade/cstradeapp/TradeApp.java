package cs.youtrade.cstradeapp;

import cs.youtrade.cstradeapp.storage.TmApiRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;

public class TradeApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        TmApiRepository.setKeys(TmApiRepository.loadApiKeys());
        FXMLLoader fxmlLoader = new FXMLLoader(TradeApp.class.getResource("main-window.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("TmTradeApp");
        stage.setResizable(false);

        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            List<Window> windowsList = Window.getWindows();
            int listSize = windowsList.size();
            for (int i = 0; i < listSize; i++)
                ((Stage) windowsList.get(0)).close();
            System.exit(0);
        });
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        TmApiRepository.saveDataToFile();
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}