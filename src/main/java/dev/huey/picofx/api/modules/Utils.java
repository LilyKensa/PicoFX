package dev.huey.picofx.api.modules;

import dev.huey.picofx.api.bases.TileMap;
import javafx.scene.image.Image;
import javafx.scene.media.Media;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Utils {
  static public String loadText(String path) {
    try (InputStream inputStream = TileMap.class.getResourceAsStream(path)) {
      if (inputStream == null) {
        throw new IllegalArgumentException("Resource not found: " + path);
      }

      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }

    return null;
  }

  static public Image loadImage(String path) {
    return new Image(
      Utils.class.getResource(path).toExternalForm()
    );
  }

  static public Media loadSound(String path) {
    return new Media(
      Utils.class.getResource(path).toExternalForm()
    );
  }
}
