package dev.huey.picofx.api.modules;

import dev.huey.picofx.api.Entry;
import dev.huey.picofx.api.bases.Vec;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Setter;

import java.util.EnumMap;
import java.util.EnumSet;

public class Inputs {
  static public EnumMap<KeyCode, Integer> keyTimers = new EnumMap<>(KeyCode.class);
  
  static public void onKeyDown(KeyEvent ev) {
    keyTimers.put(ev.getCode(), 0);
  }
  
  static public void onKeyUp(KeyEvent ev) {
    keyTimers.remove(ev.getCode());
  }

  static public Vec mousePos = Vec.zero();

  static public Vec cursor() {
    return mousePos.clone();
  }

  static public EnumMap<MouseButton, Integer> buttonTimers = new EnumMap<>(MouseButton.class);

  static void updateMousePos(MouseEvent ev) {
    mousePos = Vec.of(ev.getX(), ev.getY())
      .minus(Entry.instance.getPanePos())
      .divide(Entry.instance.getUpscaleRatio());
  }

  static public void onMouseMove(MouseEvent ev) {
    updateMousePos(ev);
  }

  static public void onMouseDown(MouseEvent ev) {
    updateMousePos(ev);
    buttonTimers.put(ev.getButton(), 0);
  }

  static public void onMouseUp(MouseEvent ev) {
    updateMousePos(ev);
    buttonTimers.remove(ev.getButton());
  }
  
  static public void onPostUpdate() {
    keyTimers.replaceAll((_, value) -> value + 1);
    buttonTimers.replaceAll((_, value) -> value + 1);
  }
  
  static public boolean key(KeyCode key) {
    return keyTimers.containsKey(key);
  }
  
  static public boolean keyOnce(KeyCode key) {
    return key(key) && keyTimers.get(key) == 0;
  }

  static public boolean button(MouseButton btn) {
    return buttonTimers.containsKey(btn);
  }

  static public boolean buttonOnce(MouseButton btn) {
    return button(btn) && buttonTimers.get(btn) == 0;
  }
  
  @Setter
  static int repeatStart = 30, repeatRate = 10;
  
  static public boolean keyRepeat(KeyCode key) {
    if (!key(key)) return false;
    int time = keyTimers.get(key);
    return time == 0 || (time >= repeatStart && time % repeatRate == 0);
  }
}
