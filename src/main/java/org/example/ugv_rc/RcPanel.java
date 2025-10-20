package org.example.ugv_rc;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class RcPanel extends Application {

  @FXML
  private TextArea console;

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(RcPanel.class.getResource("ugv_rc.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 800, 600);
    stage.setTitle("UGV RC");
    stage.setScene(scene);
    stage.show();
  }
}
