package cs.youtrade.cstradeapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindowController {
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

                insertAccController.setMainWindowController(this);
                insertAccController.setLoginFormOpen(true);
                insertAccController.setStage(stage);
                stage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}