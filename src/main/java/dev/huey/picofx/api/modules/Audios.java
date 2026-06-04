package dev.huey.picofx.api.modules;

import dev.huey.picofx.api.items.Sound;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Audios {

  static Set<AudioClip> activeClips = new HashSet<>();

  static MediaPlayer player = null;
  static boolean shouldResume = false;

  static public void emit(Sound sound) {
    AudioClip clip = sound.getClip();
    activeClips.add(clip);
    clip.play();
  }

  static public void onPauseStateChange(boolean paused) {
    if (paused) {
      pause();
      shouldResume = true;
    }
    else if (shouldResume) {
      resume();
      shouldResume = false;
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
    player.dispose();
    player = null;
  }

  static public void stopAll() {
    stop();

    for (AudioClip clip : new ArrayList<>(activeClips)) {
      clip.stop();
    }
    activeClips.clear();

    shouldResume = false;
  }
}
