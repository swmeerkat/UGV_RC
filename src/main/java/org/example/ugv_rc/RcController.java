package org.example.ugv_rc;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Properties;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.MediaPlayer;
import lombok.extern.slf4j.Slf4j;
import org.example.ugv_rc.clients.ESP32Client;

@Slf4j
public class RcController {

  @FXML
  private TextField bf_roll;
  @FXML
  private TextField bf_pitch;
  @FXML
  private TextField bf_yaw;
  @FXML
  private TextField bf_voltage;
  @FXML
  private Button cameraButton;
  @FXML
  private MediaPlayer mediaPlayer;
  @FXML
  private TextArea console;

  private KeyboardController kbctrl;
  private ESP32Client ugv;
  private boolean cameraOn = false;

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
    bf_roll.setText(roundParamValue("r", result));
    bf_pitch.setText(roundParamValue("p", result));
    bf_yaw.setText(roundParamValue("y", result));
    bf_voltage.setText(roundParamValue("v", result));
  }

  @FXML
  private void getImuData() {
    JsonNode result = ugv.get_IMU_data();
    console.appendText(result.toString() + "\n");
  }

  @FXML
  protected void switchCamera() {
    if (cameraOn) {
      // switch camera off
      cameraOn = false;
      cameraButton.setText("Camera start");
      mediaPlayer.pause();
    } else {
      // switch camera on
      cameraOn = true;
      cameraButton.setText("Camera pause");
      mediaPlayer.play();
    }
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

  private String roundParamValue(String parameter, JsonNode result) {
    Double num = Double.parseDouble(result.get(parameter).toString());
    DecimalFormat df = new DecimalFormat("###.##");
    return df.format(num);
  }
}
