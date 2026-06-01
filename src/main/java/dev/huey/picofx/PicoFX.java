package dev.huey.picofx;

import dev.huey.picofx.api.Entry;
import dev.huey.picofx.game.OneCircleDemake;
import dev.huey.picofx.game.SuperDiscBox;
import javafx.application.Application;
import javafx.stage.Stage;

public class PicoFX extends Application {
  static void main(String[] args) {
    launch();
  }

  // Entry entry = new Entry("super-disc-box")
  //   .load(new SuperDiscBox());
  // Entry entry = new Entry("celeste")
  //   .load(new Celeste());
  Entry entry = new Entry("one-circle-demake")
    .load(new OneCircleDemake());
  
  @Override
  public void start(Stage stage) {
    entry.start(stage);
  }
}