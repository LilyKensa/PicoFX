package dev.huey.picofx.api.modules;

import dev.huey.picofx.api.items.Sound;
import javafx.scene.media.MediaPlayer;

public class Audios {
  
  static public void emit(Sound sound) {
    sound.getClip().play();
  }

  static MediaPlayer player = null;
  static boolean isPausedByGame = false;

  static public void onPauseStateChange(boolean paused) {
    if (paused) {
      pause();
      isPausedByGame = true;
    }
    else if (isPausedByGame) {
      resume();
      isPausedByGame = false;
    }
  }
  
  static public void music(Sound sound, int times) {
    stop();
    player = new MediaPlayer(sound.getMedia());
    player.setCycleCount(times);
    player.play();
  }

  static public void music(Sound sound) {
    music(sound, MediaPlayer.INDEFINITE);
  }

  static public void pause() {
    if (player == null) return;
    player.pause();
  }

  static public void resume() {
    if (player == null) return;
    player.play();
  }

  static public void stop() {
    if (player == null) return;
    player.stop();
  }
}
