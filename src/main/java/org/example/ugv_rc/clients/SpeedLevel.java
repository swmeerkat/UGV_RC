package org.example.ugv_rc.clients;

public enum SpeedLevel {
  LEVEL_ONE,
  LEVEL_TWO,
  LEVEL_THREE,
  LEVEL_FOUR;

  public double getSpeed() {
    switch (this) {
      case LEVEL_ONE -> {
        return 0.14;
      }
      case LEVEL_TWO -> {
        return 0.28;
      }
      case LEVEL_THREE -> {
        return 0.35;
      }
      case LEVEL_FOUR -> {
        return 0.5;
      }
    }
    return 0;
  }

}
