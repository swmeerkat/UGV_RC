package org.example.ugv_rc;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RcPanel extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(RcPanel.class.getResource("ugv_rc.fxml"));
    Parent parent = fxmlLoader.load();
    RcController rcController = fxmlLoader.getController();
    rcController.setStage(stage);
    Scene scene = new Scene(parent);
    stage.setTitle("UGV RC");
    stage.setScene(scene);
    stage.show();
  }
}
