package dev.huey.picofx.api;

import dev.huey.picofx.api.bases.Game;
import dev.huey.picofx.api.bases.Vec;
import dev.huey.picofx.api.modules.Audios;
import dev.huey.picofx.api.modules.Config;
import dev.huey.picofx.api.modules.Inputs;
import dev.huey.picofx.api.modules.Utils;
import javafx.animation.AnimationTimer;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.nio.IntBuffer;

public class Entry {
  static public Entry instance;

  static public String fetchGameId() {
    GameSlot slot = instance.slots[instance.slot];
    return slot == null ? "_null" : slot.id;
  }

  @Getter
  Fantasy fantasy;

  @Getter
  Game game;

  record GameSlot(String id, Class<? extends Game> game) { }

  GameSlot[] slots = new GameSlot[10];
  int slot = 1;

  public Entry() {
    instance = this;
    fantasy = new Fantasy();
  }

  public Entry addGame(int slot, String id, Class<? extends Game> gameClass) {
    slots[slot] = new GameSlot(id, gameClass);
    return this;
  }
  
  Stage stage;

  @Getter
  int upscaleRatio;
  
  PixelBuffer<IntBuffer> screenBufferA, screenBufferB;
  WritableImage screenImageA, screenImageB;
  boolean useScreenB = false;

  Scene scene;
  StackPane pane;

  Canvas canvas;
  GraphicsContext ctx;

  boolean inFantasy = true;

  public Vec getPanePos() {
    return Vec.of(
      scene.getWidth() - Config.size.width() * upscaleRatio,
      scene.getHeight() - Config.size.height() * upscaleRatio
    ).divide(2);
  }
  
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
      case P -> {
        paused = !paused;

        Audios.onPauseStateChange(paused);
      }
      case F11 -> {
        stage.setFullScreen(!stage.isFullScreen());
      }
    }
  }
  
  void onKeyUp(KeyEvent ev) {
    Inputs.onKeyUp(ev);

    if (ev.isControlDown()) {
      switch (ev.getCode()) {
        case R -> {
          if (inFantasy) return;

          loadGame(slot);
        }
        case Q -> {
          if (inFantasy) return;

          loadFantasy();
        }
      }

      if (
        ev.getCode().getCode() >= KeyCode.DIGIT1.getCode() &&
        ev.getCode().getCode() <= KeyCode.DIGIT9.getCode()
      ) {
        int index = ev.getCode().getCode() - KeyCode.DIGIT0.getCode();

        loadGame(index);
      }
    }
  }

  void onMouseMove(MouseEvent ev) {
    Inputs.onMouseMove(ev);
  }

  void onMouseDown(MouseEvent ev) {
    Inputs.onMouseDown(ev);
  }

  void onMouseUp(MouseEvent ev) {
    Inputs.onMouseUp(ev);
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

      // double fps = 1000000000d / delta;
      // if (fps < 50 || fps > 700) {
      //   System.out.printf("Weird Frame Rate: %.2f\n", fps);
      // }

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
  
  TickAnimationTimer tickTimer;

  void initClock() {
    if (!inFantasy) {
      if (slots[slot] == null) return;

      try {
        game = slots[slot].game.getDeclaredConstructor().newInstance();
      }
      catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
        throw new RuntimeException(ex);
      }
    }

    tickTimer = new TickAnimationTimer();
  }

  void startClock() {
    if (tickTimer == null) return;

    if (inFantasy) {
      fantasy.start();
    }
    else {
      game.start();
    }

    tickTimer.start();
  }

  void killClock() {
    if (tickTimer == null) return;

    tickTimer.stop();
    origin = -1;
  }

  void loadFantasy() {
    killClock();
    Audios.stopAll();

    inFantasy = true;
    paused = false;

    initClock();
    startClock();

    stage.setTitle("PicoFX");
  }

  void loadGame(int index) {
    if (index < 0 || index >= slots.length || slots[index] == null) return;

    killClock();
    Audios.stopAll();

    slot = index;
    inFantasy = false;
    paused = false;

    initClock();
    startClock();

    stage.setTitle(game.getName() + " - PicoFX");
  }

  public void start(Stage stage) {
    this.stage = stage;

    stage.getIcons().add(Utils.loadImage("/assets/icon.png"));
    stage.setTitle("PicoFX");

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
    screenImageB = new WritableImage(screenBufferB);

    canvas = new Canvas();
    ctx = canvas.getGraphicsContext2D();
    ctx.setImageSmoothing(false);
    
    pane = new StackPane(canvas);
    pane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    pane.setCursor(Cursor.NONE);
    
    canvas.widthProperty().bind(pane.widthProperty());
    canvas.heightProperty().bind(pane.heightProperty());
    
    onResize(stage.getWidth(), stage.getHeight());
    
    StackPane root = new StackPane(pane);
    root.setStyle("-fx-background-color: black;");
    
    scene = new Scene(root, canvas.getWidth(), canvas.getHeight());
    
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
    
    scene.setOnKeyPressed(this::onKeyDown);
    scene.setOnKeyReleased(this::onKeyUp);

    scene.setOnMouseMoved(this::onMouseMove);
    scene.setOnMouseDragged(this::onMouseMove);
    scene.setOnMousePressed(this::onMouseDown);
    scene.setOnMouseReleased(this::onMouseUp);
    
    stage.setScene(scene);
    stage.show();

    loadFantasy();
  }
  
  void update() {
    if (inFantasy) {
      fantasy.update();
    }
    else if (!paused && game != null) {
      game.update();
    }
    
    Inputs.onPostUpdate();
  }
  
  void render() {
    if (inFantasy) {
      fantasy.render();
    }
    else if (!paused) {
      useScreenB = !useScreenB;

      if (game != null) {
        game.render();
      }
    }

    currentScreenBuffer().updateBuffer(_ -> null);
    ctx.drawImage(currentScreenImage(), 0, 0, canvas.getWidth(), canvas.getHeight());
  }
}
