package org.example.ugv_rc;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class RcController {

  @FXML
  private Label welcomeText;
  @FXML
  private TextArea console;

  @FXML
  private void initialize() {
    console.appendText("UGV RC started");
  }

  @FXML
  protected void onHelloButtonClick() {
    welcomeText.setText("Welcome to JavaFX Application!");
  }
}
