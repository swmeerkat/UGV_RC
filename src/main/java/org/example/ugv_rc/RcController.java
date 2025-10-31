package org.example.ugv_rc;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.ugv_rc.clients.ESP32Client;
import org.example.ugv_rc.clients.JetsonOrinNanoClient;
import org.example.ugv_rc.clients.MovingDirection;

@Slf4j
public class RcController {

  @FXML
  private TextField bf_roll;
  @FXML
  private TextField bf_pitch;
  @FXML
  private TextField bf_yaw;
  @FXML
  protected TextField bf_voltage;
  @FXML
  private TextArea console;

  @Setter
  private Stage stage;
  private ESP32Client ugv;
  private JetsonOrinNanoClient jetson;
  private KeyboardController kbctrl;
  private Timer gimbalTimer;
  private Timer chassisTimer;

  @FXML
  private void initialize() {
    Properties properties = loadProperties();
    String ugv_host = properties.get("UGV02.host").toString();
    log.info("ugv host: {}", ugv_host);
    ugv = initUgv02Client(ugv_host);
    String jetson_host = properties.get("Jetson.host").toString();
    log.info("jetson host: {}", jetson_host);
    jetson = new JetsonOrinNanoClient(jetson_host);
    kbctrl = new KeyboardController(ugv);
    Timer feedbackTimer = new Timer();
    feedbackTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        getBaseFeedback();
      }
    }, 0, 3000);
    Platform.runLater(() -> stage.setOnCloseRequest(_ -> feedbackTimer.cancel()));
    log.info("UGV RC initialized");
  }

  @FXML
  private void getBaseFeedback() {
    JsonNode result = ugv.cmd_base_feedback();
    //console.appendText(result.toString() + "\n");
    bf_roll.setText(roundParamValue("r", result));
    bf_pitch.setText(roundParamValue("p", result));
    bf_yaw.setText(roundParamValue("y", result));
    bf_voltage.setText(roundParamValue("v", result));
  }

  @FXML
  private void getImuData() {
    JsonNode result = ugv.get_IMU_data();
    console.appendText(result.toPrettyString() + "\n");
  }

  @FXML
  private void getInaData() {
    JsonNode result = jetson.get("/ugv_power_status");
    console.appendText(result.toPrettyString() + "\n");
  }

  // gimbal upper left button
  @FXML
  private void gul_pressed() {
    repeat_gimbal_cmd(-1, 1);
  }

  // gimbal upper middle button
  @FXML
  private void gum_pressed() {
    repeat_gimbal_cmd(0, 1);
  }

  // gimbal upper right button
  @FXML
  private void gur_pressed() {
    repeat_gimbal_cmd(1, 1);
  }

  // gimbal middle left button
  @FXML
  private void gml_pressed() {
    repeat_gimbal_cmd(-1, 0);
  }

  // gimbal middle middle button
  @FXML
  private void gmm_pressed() {
    ugv.cmd_gimbal_ctrl_simple(0, 0);
  }

  // gimbal middle right button
  @FXML
  private void gmr_pressed() {
    repeat_gimbal_cmd(1, 0);
  }

  // gimbal bottom left button
  @FXML
  private void gbl_pressed() {
    repeat_gimbal_cmd(-1, -1);
  }

  // gimbal bottom middle button
  @FXML
  private void gbm_pressed() {
    repeat_gimbal_cmd(0, -1);
  }

  // gimbal bottom right button
  @FXML
  private void gbr_pressed() {
    repeat_gimbal_cmd(1, -1);
  }

  // chassis upper left button
  @FXML
  private void cul_pressed() {
    repeat_chassis_cmd(MovingDirection.NORTHWEST);
  }

  // chassis upper middle button
  @FXML
  private void cum_pressed() {
    repeat_chassis_cmd(MovingDirection.NORTH);
  }

  // chassis upper right button
  @FXML
  private void cur_pressed() {
    repeat_chassis_cmd(MovingDirection.NORTHEAST);
  }

  // chassis middle left button
  @FXML
  private void cml_pressed() {
    repeat_chassis_cmd(MovingDirection.WEST);
  }

  // chassis middle middle button
  @FXML
  private void cmm_pressed() {
    ugv.cmd_speed_control(MovingDirection.STOP);
  }

  // chassis middle right button
  @FXML
  private void cmr_pressed() {
    repeat_chassis_cmd(MovingDirection.EAST);
  }

  // chassis bottom left button
  @FXML
  private void cbl_pressed() {
    repeat_chassis_cmd(MovingDirection.SOUTHWEST);
  }

  // chassis bottom middle button
  @FXML
  private void cbm_pressed() {
    repeat_chassis_cmd(MovingDirection.SOUTH);
  }

  // chassis bottom right button
  @FXML
  private void cbr_pressed() {
    repeat_chassis_cmd(MovingDirection.SOUTHEAST);
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
    if (!result.isEmpty()) {
      Double num = Double.parseDouble(result.get(parameter).toString());
      DecimalFormat df = new DecimalFormat("###.##");
      return df.format(num);
    }
    return "";
  }

  private void repeat_gimbal_cmd(int delta_pan, int delta_tilt) {
    gimbalTimer = new Timer();
    gimbalTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        ugv.gimbal_step(delta_pan, delta_tilt);
      }
    }, 0, 50);
  }

  @FXML
  private void gimbal_released() {
    gimbalTimer.cancel();
  }

  private void repeat_chassis_cmd(MovingDirection direction) {
    chassisTimer = new Timer();
    chassisTimer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        ugv.cmd_speed_control(direction);
      }
    }, 0, 3000);
  }

  @FXML
  private void chassis_released() {
    ugv.cmd_speed_control(MovingDirection.STOP);
    chassisTimer.cancel();
  }
}
