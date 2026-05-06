package dev.huey.picofx.api.modules;

import dev.huey.picofx.api.items.Sound;
import javafx.scene.media.MediaPlayer;

public class Audios {
  
  static public void emit(Sound sound) {
    sound.getClip().play();
  }

  static MediaPlayer player = null;
  
  static public void music(Sound sound, int times) {
    stop();
    player = new MediaPlayer(sound.getMedia());
    player.setCycleCount(times);
    player.play();
  }

  static public void music(Sound sound) {
    music(sound, MediaPlayer.INDEFINITE);
  }

  static public void stop() {
    if (player == null) return;
    player.stop();
  }
}
