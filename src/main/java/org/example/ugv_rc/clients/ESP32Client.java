package org.example.ugv_rc.clients;

import static org.apache.hc.client5.http.impl.classic.HttpClients.createDefault;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.StatusLine;

/*
 * References:
 *  - https://www.waveshare.com/wiki/UGV02
 *  - https://www.waveshare.com/wiki/2-Axis_Pan-Tilt_Camera_Module
 */
@Slf4j
public class ESP32Client {

  private final String host;
  private int actPan;
  private int actTilt;
  private boolean panTiltLed;

  public ESP32Client(String host) {
    this.host = host;
    this.actPan = 0;
    this.actTilt = 0;
    this.panTiltLed = false;
  }

  /*
   * Retrieve IMU data
   * Output:
   *  3D orientation angles
   *    - r (roll, rotation about x-axis)
   *    - p (pitch, rotation about y-axis)
   *    - y (yaw, rotation about z-axis)
   *  accelerometer
   *    - ax, ay, az
   *  gyroscope
   *    - gx, gy, gz
   *  magnetometer
   *    - mx, my, mz
   *
   *  - temp
   */
  public JsonNode retrieve_IMU_data() {
    String cmd = "{\"T\":126}";
    return get(cmd);
  }

  /*
   * CMD_BASE_FEEDBACK
   * Output:
   *  - L, R
   *  3D orientation angles
   *    - r, p, y
   *  Voltage:
   *    - v
   */
  public JsonNode cmd_base_feedback() {
    String cmd = "{\"T\":130}";
    return get(cmd);
  }

  /*
   * Set servo middle position
   * Sets the actual servo position as new middle position
   * Input:
   *  - id: 1 - tilt servo, 2 - pan servo
   */
  public JsonNode set_middle_position(int servo) {
    String cmd = "{\"T\":502,\"id\":" + servo + "}";
    return get(cmd);
  }

  /*
   * CMD_SPEED_CTRL
   * Input:
   *  - L, R: speed of the wheel, value range 0.5 - -0.5
   */
  public JsonNode cmd_speed_control(double l, double r) {
    String cmd = "{\"T\":1,\"L\":" + l + ",\"R\":" + r + "}";
    return get(cmd);
  }

  /*
   * CMD_GIMBAL_CTRL_SIMPLE
   * Input:
   *  - X: PAN, value range -180 to 180
   *  - Y: Tilt, value range -30 to 90
   *  - SPD: Speed, 0 means fastest
   *  - ACC: Acceleration, 0 means fastest
   */
  public JsonNode cmd_gimbal_ctrl_simple(int pan, int tilt) {
    String cmd = "{\"T\":133,\"X\":" + pan + ",\"Y\":" + tilt + ",\"SPD\":0,\"ACC\":0} ";
    actPan = pan;
    actTilt = tilt;
    return get(cmd);
  }

  /*
   * CMD_GIMBAL_CTRL_STOPE
   * Stops the pan-tilt movement at any time
   */
  public void cmd_gimbal_ctrl_stop() {
    String cmd = "{\"T\":133} ";
    get(cmd);
  }


  public void pan_right() {
    if (actPan < 180) {
      actPan+=2;
    }
    cmd_gimbal_ctrl_simple(actPan, actTilt);
  }

  public void pan_left() {
    if (actPan > -180) {
      actPan-=2;
    }
    cmd_gimbal_ctrl_simple(actPan, actTilt);
  }

  public void tilt_up() {
    if (actTilt < 90) {
      actTilt+=2;
    }
    cmd_gimbal_ctrl_simple(actPan, actTilt);
  }

  public void tilt_down() {
    if (actTilt > -30) {
      actTilt-=2;
    }
    cmd_gimbal_ctrl_simple(actPan, actTilt);
  }

  /*
   *  CMD_LED_CTRL
   *  IO5 controls pan-tilt LED
   */
  public void turn_pan_tilt_led() {
    int brightness;
    if (!panTiltLed) {
      panTiltLed = true;
      brightness = 255;
    } else {
      panTiltLed = false;
      brightness = 0;
    }
    String cmd = "{\"T\":132, \"IO4\":0,\"IO5\":" + brightness + "}";
    get(cmd);
  }

  private JsonNode get(String cmd) throws RuntimeException {
    JsonFactory jsonFactory = new JsonFactory();
    ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    JsonNode responseData;
    try (CloseableHttpClient client = createDefault()) {
      ClassicHttpRequest httpGet = ClassicRequestBuilder.get()
          .setScheme("http")
          .setHttpHost(new HttpHost(host))
          .setPath("/js")
          .addParameter("json", cmd)
          .build();
      log.info("Request: {}", cmd);
      responseData = client.execute(httpGet, response -> {
        if (response.getCode() >= 300) {
          log.error(new StatusLine(response).toString());
          client.close();
          throw new RuntimeException("ESPClientError");
        }
        final HttpEntity responseEntity = response.getEntity();
        if (responseEntity == null) {
          return null;
        }
        try (InputStream inputStream = responseEntity.getContent()) {
          return objectMapper.readTree(inputStream);
        }
      });
      if (responseData != null) {
        if (responseData.get(0) != null) {
          log.info("Response: {}", responseData);
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new RuntimeException("ESPClientError");
    }
    return responseData;
  }
}

