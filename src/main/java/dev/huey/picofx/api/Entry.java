package dev.huey.picofx.api;

import dev.huey.picofx.api.bases.Game;
import dev.huey.picofx.api.modules.Config;
import dev.huey.picofx.api.modules.Inputs;
import dev.huey.picofx.api.modules.Utils;
import dev.huey.picofx.game.SuperDiscBox;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.nio.IntBuffer;

public class Entry {
  
  static public Entry instance;

  public Entry() {
    instance = this;
  }
  
  Stage stage;
  
  Game game;
  
  int upscaleRatio;
  
  PixelBuffer<IntBuffer> screenBufferA, screenBufferB;
  WritableImage screenImageA, screenImageB;
  boolean useScreenB = false;
  
  StackPane pane;
  Canvas canvas;
  GraphicsContext ctx;
  
  public PixelBuffer<IntBuffer> currentScreenBuffer() {
    return useScreenB ? screenBufferB : screenBufferA;
  }
  
  public WritableImage currentScreenImage() {
    return useScreenB ? screenImageB : screenImageA;
  }
  
  void onResize(double windowWidth, double windowHeight) {
    
    upscaleRatio = (int) Math.min(
      windowWidth / Config.size.width(),
      windowHeight / Config.size.height()
    );
    
    pane.resize(
      Config.size.width() * upscaleRatio,
      Config.size.height() * upscaleRatio
    );
  }
  
  void onKeyDown(KeyEvent ev) {
    Inputs.onKeyDown(ev);

    switch (ev.getCode()) {
      case ESCAPE -> {
        paused = !paused;
      }
      case F11 -> {
        stage.setFullScreen(!stage.isFullScreen());
      }
    }
  }
  
  void onKeyUp(KeyEvent ev) {
    Inputs.onKeyUp(ev);
  }

  boolean paused;
  
  long count = 0, origin = -1, skipped = 0, last = 0, delta = 0;
  
  public long time() {
    return last - origin - skipped;
  }
  
  class TickAnimationTimer extends AnimationTimer {
    @Override
    public void handle(long now) {
      count++;
      delta = now - last;
      last = now;

      if (paused) {
        skipped += delta;
      }
      
      if (origin < 0) {
        origin = now;
      }
      else {
        update();

        if (!stage.isIconified()) {
          render();
        }
      }
    }
  }
  
  TickAnimationTimer tickTimer = new TickAnimationTimer();
  
  public void start(Stage stage) {
    this.stage = stage;

    game = new SuperDiscBox();

    stage.getIcons().add(Utils.loadImage("/assets/icon.png"));
    stage.setTitle(game.getName() + " - PicoFX");

    Screen screen = Screen.getPrimary();
    stage.setWidth(screen.getBounds().getWidth() * 0.5);
    stage.setHeight(screen.getBounds().getHeight() * 0.6);

    stage.setFullScreenExitHint("");
    stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

    screenBufferA = new PixelBuffer<>(
      Config.size.width(),
      Config.size.height(),
      IntBuffer.allocate(Config.size.width() * Config.size.height()),
      PixelFormat.getIntArgbPreInstance()
    );
    screenImageA = new WritableImage(screenBufferA);
    screenBufferB = new PixelBuffer<>(
      Config.size.width(),
      Config.size.height(),
      IntBuffer.allocate(Config.size.width() * Config.size.height()),
      PixelFormat.getIntArgbPreInstance()
    );
    screenImageB = new WritableImage(screenBufferA);
    
    canvas = new Canvas();
    ctx = canvas.getGraphicsContext2D();
    ctx.setImageSmoothing(false);
    
    pane = new StackPane(canvas);
    pane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    
    canvas.widthProperty().bind(pane.widthProperty());
    canvas.heightProperty().bind(pane.heightProperty());
    
    onResize(stage.getWidth(), stage.getHeight());
    
    StackPane root = new StackPane(pane);
    root.setStyle("-fx-background-color: black;");
    
    Scene scene = new Scene(root, canvas.getWidth(), canvas.getHeight());
    
    scene.widthProperty().addListener((
      _,
      _,
      val
    ) -> onResize(val.doubleValue(), scene.getHeight()));
    scene.heightProperty().addListener((
      _,
      _,
      val
    ) -> onResize(scene.getWidth(), val.doubleValue()));
    
    scene.addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyDown);
    scene.addEventHandler(KeyEvent.KEY_RELEASED, this::onKeyUp);
    
    stage.setScene(scene);
    stage.show();
    
    game.start();
    
    tickTimer.start();
  }
  
  void update() {
    if (!paused) {
      game.update();
    }
    
    Inputs.onPostUpdate();
  }
  
  void render() {
    useScreenB = !useScreenB;

    if (!paused) {
      game.render();
    }
    
    currentScreenBuffer().updateBuffer(_ -> null);

    ctx.drawImage(currentScreenImage(), 0, 0, canvas.getWidth(), canvas.getHeight());
  }
}
