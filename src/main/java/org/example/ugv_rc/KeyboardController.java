package org.example.ugv_rc;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.example.ugv_rc.clients.ESP32Client;

/*
 Apple key codes:
  - command: 157
  - option: 18
  - cursor up: 38
  - cursor down: 40
  - cursor right: 39
  - cursor left: 37
  - numpad '0': 96
 */
@Slf4j
public class KeyboardController {

  private final ESP32Client esp32Client;
  private boolean optionKeyPressed = false;

  public KeyboardController(ESP32Client esp32Client) {
    this.esp32Client = esp32Client;
  }

  public void keyPressed(KeyEvent e) {
    switch (e.getCode()) {
      case KeyCode.SHIFT:
        optionKeyPressed = true;
        break;
      case KeyCode.H:
        if (optionKeyPressed) {
          esp32Client.pan_left();
        } else {
          esp32Client.cmd_speed_control(-0.15, 0.15);
        }
        break;
      case KeyCode.L:
        if (optionKeyPressed) {
          esp32Client.pan_right();
        } else {
          esp32Client.cmd_speed_control(0.15, -0.15);
        }
        break;
      case KeyCode.J:
        if (optionKeyPressed) {
          esp32Client.tilt_up();
        } else {
          esp32Client.cmd_speed_control(0.1, 0.1);
        }
        break;
      case KeyCode.K:
        if (optionKeyPressed) {
          esp32Client.tilt_down();
        } else {
          esp32Client.cmd_speed_control(-0.1, -0.1);
        }
        break;
      case KeyCode.A:
        esp32Client.turn_pan_tilt_led();
        break;
      case KeyCode.SPACE:
        esp32Client.cmd_speed_control(-0, -0);
        esp32Client.cmd_gimbal_ctrl_stop();
        break;
      default:
        log.info("unexpected key pressed: char={} code={}, ignored",
            e.getText(), e.getCode());
        break;
    }
  }

  public void keyReleased(KeyEvent e) {
    if (e.getCode() == KeyCode.SHIFT) {
      optionKeyPressed = false;
    }
  }
}
