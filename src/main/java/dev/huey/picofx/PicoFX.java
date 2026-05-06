package dev.huey.picofx;

import dev.huey.picofx.api.Entry;
import javafx.application.Application;
import javafx.stage.Stage;

public class PicoFX extends Application {
  
  public static void main(String[] args) {
    launch();
  }
  
  Entry entry = new Entry();
  
  @Override
  public void start(Stage stage) {
    entry.start(stage);
  }
}