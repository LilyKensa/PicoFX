package dev.huey.picofx.api.modules;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Setter;

import java.util.EnumMap;

public class Inputs {
  static public EnumMap<KeyCode, Integer> timers = new EnumMap<>(KeyCode.class);
  
  static public void onKeyDown(KeyEvent ev) {
    timers.put(ev.getCode(), 0);
  }
  
  static public void onKeyUp(KeyEvent ev) {
    timers.remove(ev.getCode());
  }
  
  static public void onPostUpdate() {
    timers.replaceAll((_, value) -> value + 1);
  }
  
  static public boolean key(KeyCode key) {
    return timers.containsKey(key);
  }
  
  static public boolean keyOnce(KeyCode key) {
    return key(key) && timers.get(key) == 0;
  }
  
  @Setter
  static int repeatStart = 30, repeatRate = 10;
  
  static public boolean keyRepeat(KeyCode key) {
    if (!key(key)) return false;
    int time = timers.get(key);
    return time == 0 || (time >= repeatStart && time % repeatRate == 0);
  }
}
