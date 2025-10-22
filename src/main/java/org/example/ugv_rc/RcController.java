package org.example.ugv_rc;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.example.ugv_rc.clients.ESP32Client;

@Slf4j
public class RcController {

  @FXML
  private TextArea console;
  @FXML
  private TextField tf_L;
  @FXML
  private TextField tf_R;
  @FXML
  private TextField tf_r;
  @FXML
  private TextField tf_p;
  @FXML
  private TextField tf_y;
  @FXML
  private TextField tf_v;

  private KeyboardController kbctrl;

  ESP32Client ugv;

  @FXML
  private void initialize() {
    Properties properties = loadProperties();
    String host = properties.get("UGV02.host").toString();
    log.info("ugv host: {}", host);
    ugv = initUgv02Client(host);
    kbctrl = new KeyboardController(ugv);
    log.info("UGV RC initialized");
  }

  @FXML
  private void getBaseFeedback() {
    JsonNode result = ugv.cmd_base_feedback();
    console.appendText(result.toString() + "\n");
    tf_L.setText(result.get("L").toString());
    tf_R.setText(result.get("R").toString());
    tf_r.setText(result.get("r").toString());
    tf_p.setText(result.get("p").toString());
    tf_y.setText(result.get("y").toString());
    tf_v.setText(result.get("v").toString());
  }

  @FXML
  private void getImuData() {
    JsonNode result = ugv.retrieve_IMU_data();
    console.appendText(result.toString() + "\n");
  }

  @FXML
  protected void keyPressed(KeyEvent event) {
    kbctrl.keyPressed(event);
  }

  @FXML
  protected void keyReleased(KeyEvent event) {
    kbctrl.keyReleased(event);
  }

  @FXML
  protected void enterKeyboardControl() {
    // focus on button is sufficient
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
