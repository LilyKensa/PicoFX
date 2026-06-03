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
import javafx.scene.paint.Color;
import dev.huey.picofx.api.items.Font;
import dev.huey.picofx.api.modules.Graphics;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.nio.IntBuffer;

public class Entry {
  static public Entry instance;

  static final Color LAUNCHER_BG = Color.BLACK;
  static final Color LAUNCHER_TITLE = Color.web("#fff1e8");
  static final Color LAUNCHER_TEXT = Color.web("#c2c3c7");
  static final Color LAUNCHER_HINT = Color.web("#29adff");

  static public String fetchGameId() {
    GameSlot slot = instance.slots[instance.slot];
    return slot == null ? "_null" : slot.id;
  }

  @Getter
  Game game;

  record GameSlot(String id, Class<? extends Game> game) {

  }

  GameSlot[] slots = new GameSlot[10];
  int slot = 1;

  public Entry() {
    instance = this;
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

  boolean launcherMode = true;
  Font launcherFont;

  // Separate timer for launcher blinking before any game is loaded
  AnimationTimer launcherTimer;
  int launcherFrame = 0;
  boolean launcherHintVisible = true;

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
    upscaleRatio =  Math.max(1, (int) Math.min(
      windowWidth / Config.size.width(),
      windowHeight / Config.size.height()
    ));
    
    pane.resize(
      Config.size.width() * upscaleRatio,
      Config.size.height() * upscaleRatio
    );
  }
  
  void onKeyDown(KeyEvent ev) {
    if (ev.isControlDown()) return;
    Inputs.onKeyDown(ev);

    switch (ev.getCode()) {
      case ESCAPE -> {
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
          if (launcherMode) return;

          killClock();
          Audios.stopAll();
          paused = false;
          initClock();
          startClock();
        }
        case C -> {
          if (launcherMode) return;

          killClock();
          Audios.stopAll();
          ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

          paused = false;
          game = null;
          tickTimer = null;
          startLauncher();
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
    if (slots[slot] == null) return;

    try {
      game = slots[slot].game.getDeclaredConstructor().newInstance();
    }
    catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
      throw new RuntimeException(ex);
    }
    tickTimer = new TickAnimationTimer();
  }

  void startClock() {
    if (game == null || tickTimer == null) return;

    stage.setTitle(game.getName() + " - PicoFX");

    game.start();
    tickTimer.start();
  }

  void killClock() {
    if (tickTimer == null) return;

    tickTimer.stop();
    origin = -1;
  }

  void loadGame(int index) {
    if (index < 0 || index >= slots.length || slots[index] == null) return;

    stopLauncher();
    killClock();
    Audios.stopAll();
    ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

    slot = index;
    launcherMode = false;
    paused = false;

    initClock();
    startClock();
  }

  int launcherTextWidth(String text) {
    return text.length() * launcherFont.getSpaceWidth();
  }

  void launcherPrintCentered(String text, int y, Color color) {
    int x = (Config.size.width() - launcherTextWidth(text)) / 2;
    Graphics.print(text, Vec.of(x, y), color, launcherFont);
  }

  void launcherPrint(String text, int x, int y, Color color) {
    Graphics.print(text, Vec.of(x, y), color, launcherFont);
  }

  void startLauncher() {
    launcherMode = true;
    stage.setTitle("PicoFX");

    launcherFrame = 0;
    launcherHintVisible = true;
    renderLauncher();

    if (launcherTimer == null) {
      launcherTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
          launcherFrame++;

          if (launcherFrame >= 30) {
            launcherFrame = 0;
            launcherHintVisible = !launcherHintVisible;
            renderLauncher();
          }
        }
      };
    }

    launcherTimer.start();
  }

  void stopLauncher() {
    if (launcherTimer != null) {
      launcherTimer.stop();
    }
  }

  void renderLauncher() {
    // Draw launcher through the same 128x128 pixel buffer as games
    useScreenB = false;

    Graphics.camera();
    Graphics.clear(LAUNCHER_BG);

    launcherPrintCentered("PICOFX", 4, LAUNCHER_TITLE);
    launcherPrintCentered("GAME LAUNCHER", 16, LAUNCHER_TEXT);

    launcherPrint("AVAILABLE GAMES", 16, 32, LAUNCHER_TITLE);
    launcherPrint("CTRL+1  SUPER DISC BOX", 16, 42, LAUNCHER_TEXT);
    launcherPrint("CTRL+2  ONE CIRCLE DEMAKE", 16, 50, LAUNCHER_TEXT);

    launcherPrint("CONTROLS", 16, 66, LAUNCHER_TITLE);
    launcherPrint("ESC      PAUSE", 16, 76, LAUNCHER_TEXT);
    launcherPrint("F11      FULLSCREEN", 16, 84, LAUNCHER_TEXT);
    launcherPrint("CTRL+R   RELOAD", 16, 92, LAUNCHER_TEXT);
    launcherPrint("CTRL+C   STOP", 16, 100, LAUNCHER_TEXT);

    if (launcherHintVisible) {
      launcherPrintCentered("PRESS CTRL+NUMBER", 116, LAUNCHER_HINT);
    }

    currentScreenBuffer().updateBuffer(_ -> null);
    ctx.drawImage(currentScreenImage(), 0, 0, canvas.getWidth(), canvas.getHeight());
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

    launcherFont = Font.pico8();

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
    ) -> {
      onResize(val.doubleValue(), scene.getHeight());
      if (launcherMode) renderLauncher();
    });
    scene.heightProperty().addListener((
      _,
      _,
      val
    ) -> {
      onResize(scene.getWidth(), val.doubleValue());
      if (launcherMode) renderLauncher();
    });
    
    scene.setOnKeyPressed(this::onKeyDown);
    scene.setOnKeyReleased(this::onKeyUp);

    scene.setOnMouseMoved(this::onMouseMove);
    scene.setOnMouseDragged(this::onMouseMove);
    scene.setOnMousePressed(this::onMouseDown);
    scene.setOnMouseReleased(this::onMouseUp);
    
    stage.setScene(scene);
    stage.show();

    startLauncher();
  }
  
  void update() {
    if (!launcherMode && !paused && game != null) {
      game.update();
    }
    
    Inputs.onPostUpdate();
  }
  
  void render() {
    if (launcherMode) {
      renderLauncher();
      return;
    }

    if (paused) {
      ctx.drawImage(currentScreenImage(), 0, 0, canvas.getWidth(), canvas.getHeight());
      return;
    }

    useScreenB = !useScreenB;

    if (game != null) {
      game.render();
    }
    
    currentScreenBuffer().updateBuffer(_ -> null);

    ctx.drawImage(currentScreenImage(), 0, 0, canvas.getWidth(), canvas.getHeight());
  }
}
