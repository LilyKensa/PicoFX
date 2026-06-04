package dev.huey.picofx.api;

import dev.huey.picofx.api.bases.Vec;
import dev.huey.picofx.api.items.Font;
import dev.huey.picofx.api.modules.Config;
import dev.huey.picofx.api.modules.Graphics;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;

public class Fantasy {
  static final Color LAUNCHER_BG = Color.BLACK;
  static final Color LAUNCHER_TITLE = Color.web("#fff1e8");
  static final Color LAUNCHER_TEXT = Color.web("#c2c3c7");
  static final Color LAUNCHER_HINT = Color.web("#29adff");

  Font font;
  boolean launcherHintVisible = true;

  int frame = 0;

  public Fantasy() {
    font = Font.pico8();
  }

  int getTextWidth(String text) {
    return text.length() * font.getSpaceWidth();
  }

  void printCentered(String text, int y, Color color) {
    int x = (Config.size.width() - getTextWidth(text)) / 2;
    Graphics.print(text, Vec.of(x, y), color, font);
  }

  void print(String text, int x, int y, Color color) {
    Graphics.print(text, Vec.of(x, y), color, font);
  }

  void start() {
    frame = 0;
  }

  void update() {
    frame++;

    if (frame >= 30) {
      frame = 0;
      launcherHintVisible = !launcherHintVisible;
    }
  }

  void render() {
    Graphics.camera();
    Graphics.clear(LAUNCHER_BG);

    printCentered("PICOFX", 4, LAUNCHER_TITLE);
    printCentered("GAME LAUNCHER", 16, LAUNCHER_TEXT);

    print("AVAILABLE GAMES", 16, 32, LAUNCHER_TITLE);
    print("CTRL+1  SUPER DISC BOX", 16, 42, LAUNCHER_TEXT);
    print("CTRL+2  ONE CIRCLE DEMAKE", 16, 50, LAUNCHER_TEXT);

    print("CONTROLS", 16, 66, LAUNCHER_TITLE);
    print("ESC      PAUSE", 16, 76, LAUNCHER_TEXT);
    print("F11      FULLSCREEN", 16, 84, LAUNCHER_TEXT);
    print("CTRL+R   RELOAD", 16, 92, LAUNCHER_TEXT);
    print("CTRL+C   STOP", 16, 100, LAUNCHER_TEXT);

    if (launcherHintVisible) {
      printCentered("PRESS CTRL+NUMBER", 116, LAUNCHER_HINT);
    }
  }
}
