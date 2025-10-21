package org.example.ugv_rc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import org.example.ugv_rc.clients.ESP32Client;

@Slf4j
public class RcController {

  @FXML
  private Label welcomeText;
  @FXML
  private TextArea console;

  private ESP32Client ugv02;


  @FXML
  private void initialize() {
    Properties properties = loadProperties();
    String host = properties.get("UGV02.host").toString();
    log.info("ugv host: {}", host);
    this.ugv02 = initUgv02Client(host);
    log.info("UGV RC initialized");
  }

  @FXML
  protected void onHelloButtonClick() {
    welcomeText.setText("Welcome to JavaFX Application!");
  }

  private ESP32Client initUgv02Client(String host) {
    ESP32Client ugv02 = new ESP32Client(host);
    log.info("Init gimbal: cmd_gimbal_ctrl_simple(0, 0)");
    ugv02.cmd_gimbal_ctrl_simple(0, 0);
    return ugv02;
  }

  private Properties loadProperties() {
    Properties properties = new Properties();
    InputStream stream =
        Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("application.properties");
    try {
      properties.load(stream);
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return properties;
  }
}
