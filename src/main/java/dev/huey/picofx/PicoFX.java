package dev.huey.picofx;

import dev.huey.picofx.api.Entry;
import dev.huey.picofx.game.OneCircleDemake;
import dev.huey.picofx.game.SuperDiscBox;
import dev.huey.picofx.game.TestGame;
import javafx.application.Application;
import javafx.stage.Stage;

public class PicoFX extends Application {
  static void main(String[] args) {
    launch();
  }

  Entry entry = new Entry()
    .addGame(1, "super-disc-box", SuperDiscBox.class)
    .addGame(2, "one-circle-demake", OneCircleDemake.class)
    .addGame(3, "test", TestGame.class);
  
  @Override
  public void start(Stage stage) {
    entry.start(stage);
  }
}