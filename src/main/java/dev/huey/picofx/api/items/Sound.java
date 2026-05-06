package dev.huey.picofx.api.items;

import dev.huey.picofx.api.modules.Utils;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import lombok.Getter;

public class Sound {
  @Getter
  Media media;
  @Getter
  AudioClip clip;

  static public Sound load(String path) {
    return new Sound(Utils.loadSound("/assets/audios/%s.wav".formatted(path)));
  }

  public Sound(Media media) {
    this.media = media;
    this.clip = new AudioClip(media.getSource());
  }

  public void playOnce() {
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    mediaPlayer.play();
  }
}
