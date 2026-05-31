package dev.huey.picofx.game;

import dev.huey.picofx.api.bases.Game;
import dev.huey.picofx.api.bases.Vec;
import dev.huey.picofx.api.items.Sound;
import dev.huey.picofx.api.modules.Audios;
import javafx.scene.input.KeyCode;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

public class Celeste implements Game {
  @Override
  public String getName() {
    return "Celeste";
  }

  Vec room = Vec.zero();
  // objects = {}
  // types = {}
  int freeze = 0;
  int shake = 0;
  boolean willRestart = false;
  int delayRestart = 0;
  List<Boolean> gotFruit = new ArrayList<>();
  boolean hasDashed = false;
  int sfxTimer = 0;
  boolean hasKey = false;
  boolean pausePlayer = false;
  boolean flashBg = false;
  int musicTimer = 0;

  KeyCode k_left = KeyCode.LEFT;
  KeyCode k_right = KeyCode.RIGHT;
  KeyCode k_up = KeyCode.UP;
  KeyCode k_down = KeyCode.DOWN;
  KeyCode k_jump = KeyCode.C;
  KeyCode k_dash = KeyCode.X;

  @Override
  public void start() {
    titleScreen();
  }

  int frames, deaths, maxDJump;
  boolean startGame;
  int startGameFlash;

  void titleScreen() {
    for (int i = 0; i < 30; ++i) {
      gotFruit.add(false);
    }
    frames = 0;
    deaths = 0;
    maxDJump = 1;
    startGame = false;
    startGameFlash = 0;

    Audios.music(Sound.load("title"));

    // loadRoom(7, 3);
  }

  int minutes, seconds;

  void beginGame() {
    frames = 0;
    seconds = 0;
    minutes = 0;
    musicTimer = 0;
    startGame = false;

    Audios.music(Sound.load("game"));

    // loadRoom(0, 0);
  }

  int levelIndex() {
    return (int) (room.x % 8 + room.y * 8);
  }

  boolean isTitle() {
    return levelIndex() == 31;
  }

  @Builder
  static class Cloud {
    double x, y, speed, w;
  }

  List<Cloud> clouds;

  @Override
  public void update() {

  }

  @Override
  public void render() {

  }
}
