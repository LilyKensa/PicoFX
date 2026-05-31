package dev.huey.picofx.api.items;

import dev.huey.picofx.api.Entry;
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

  static public Sound load(String namespace, String path) {
    return new Sound(Utils.loadSound("/assets/%s/audios/%s.wav".formatted(namespace, path)));
  }

  static public Sound load(String path) {
    return load(Entry.instance.getId(), path);
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
