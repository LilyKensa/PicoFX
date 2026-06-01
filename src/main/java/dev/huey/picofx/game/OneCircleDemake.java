package dev.huey.picofx.game;

import dev.huey.picofx.api.Entry;
import dev.huey.picofx.api.bases.Game;
import dev.huey.picofx.api.bases.Vec;
import dev.huey.picofx.api.items.Font;
import dev.huey.picofx.api.items.Sound;
import dev.huey.picofx.api.items.Sprite;
import dev.huey.picofx.api.items.TileMap;
import dev.huey.picofx.api.modules.Audios;
import dev.huey.picofx.api.modules.Graphics;
import dev.huey.picofx.api.modules.Inputs;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneCircleDemake implements Game {
  @Override
  public String getId() {
    return "one-circle-demake";
  }

  @Override
  public String getName() {
    return "One Circle Demake";
  }

  double time() {
    return Entry.instance.time() / 1000_000_000d;
  }

  Sound[] sounds = new Sound[30];

  void sfx(int index) {
    Audios.emit(sounds[index]);
  }

  void music(int index) {
    Audios.music(sounds[index]);
  }

  void loadSounds() {
    for (int i = 0; i < 4; ++i) {
      sounds[i] = Sound.load("music_" + i);
    }

    for (int i = 10; i < 30; ++i) {
      sounds[i] = Sound.load("sfx_" + i);
    }
  }
  
  Font font = Font.pico8();

  Sprite sheet = Sprite.load("sheet");

  Map<Character, Sprite> tileSprites = Map.ofEntries(
    Map.entry('o', sheet.slice(Vec.of(96, 0), 32, 32))
  );

  TileMap bgTiles = TileMap.builder()
    .source("background")
    .gridWidth(32).gridHeight(32)
    .map(tileSprites)
    .build();

  final Color[] palette = {
    Color.rgb(0, 0, 0),
    Color.rgb(29, 43, 83),
    Color.rgb(126, 37, 83),
    Color.rgb(0, 135, 81),
    Color.rgb(171, 82, 54),
    Color.rgb(73, 51, 59),
    Color.rgb(194, 195, 199),
    Color.rgb(255, 241, 232),
    Color.rgb(255, 0, 77),
    Color.rgb(255, 108, 36),
    Color.rgb(162, 136, 121),
    Color.rgb(0, 181, 67),
    Color.rgb(6, 90, 181),
    Color.rgb(131, 118, 156),
    Color.rgb(190, 18, 80),
    Color.rgb(255, 204, 170)
  };

  // Utils

  void shadowCircle(Vec pos, double size, Color col) {
    Graphics.circle(pos.add(Vec.of(1.5, 1.5)), size, palette[0]);
    Graphics.circle(pos, size, col);
  }

  void shadowFillCircle(Vec pos, double size, Color col) {
    Graphics.fillCircle(pos.add(Vec.of(1.5, 1.5)), size, palette[0]);
    Graphics.fillCircle(pos, size, col);
  }

  Vec getDir(Vec start, Vec end, int threshold) {
    Vec delta = end.minus(start);
    boolean smallX = Math.abs(delta.x) < threshold, smallY = Math.abs(delta.y) < threshold;

    if (smallX && smallY) {
      return Vec.zero();
    }
    else if (smallX) {
      return Vec.of(0, Math.signum(delta.y));
    }
    else if (smallY) {
      return Vec.of(Math.signum(delta.x), 0);
    }

    return null;
  }

  Vec getDir(Vec start, Vec end) {
    return getDir(start, end, 12);
  }

  boolean inRect(Vec pos, Vec lower, Vec size) {
    Vec upper = lower.add(size);

    return pos.x >= lower.x
      && pos.y >= lower.y
      && pos.x < upper.x
      && pos.y < upper.y;
  }

  boolean cursorStartAndEndIn(Vec lower, Vec size) {
    return inRect(cursor.start, lower, size)
      && inRect(cursor.end, lower, size);
  }

  void printNumber(int num, Vec pos, Color col, int length) {
    for (int i = 0; i < length; ++i) {
      int d = num % 10;
      num = (num - d) / 10;

      Graphics.map(palette[7], col);
      Graphics.sprite(
        sheet.slice(Vec.of(25 + d * 5, 25), 4, 6),
        Vec.of(pos.x - i * 5, pos.y)
      );
      Graphics.map(palette[7], palette[7]);
    }
  }

  void printNumber(int num, Vec pos, Color col) {
    printNumber(num, pos, col, 2);
  }

  void printNumber(int num, Vec pos) {
    printNumber(num, pos, palette[7]);
  }

  // Main

  enum GameScene {
    TITLE, GAME, MENU, CREDITS
  }

  GameScene scene = GameScene.TITLE;

  @Override
  public void start() {
    loadSounds();
    Graphics.font(font);

    bg.start();
    levels.start();
  }

  @Override
  public void update() {
    schedule.update();

    bg.update();
    particles.update();
    
    switch (scene) {
      case TITLE -> title.update();
      case GAME -> objects.update();
      case MENU -> menu.update();
      case CREDITS -> credits.update();
    }

    cursor.update();
  }

  @Override
  public void render() {
    bg.render();
    particles.render();

    switch (scene) {
      case TITLE -> title.render();
      case GAME -> objects.render();
      case MENU -> menu.render();
      case CREDITS -> credits.render();
    }

    cursor.render();
  }

  // Scheduled Tasks

  static class GameTask {
    Runnable task;
    int ticks;

    public GameTask(Runnable task, int ticks) {
      this.task = task;
      this.ticks = ticks;
    }
  }

  static class GameSchedule {
    List<GameTask> list = new ArrayList<>();

    void update() {
      list.removeIf(item -> {
        item.ticks--;
        if (item.ticks > 0) return false;

        item.task.run();
        return true;
      });
    }

    void add(Runnable task, int ticks) {
      list.add(new GameTask(task, ticks));
    }

    void add(Runnable task, double ticks) {
      add(task, (int) Math.ceil(ticks));
    }
  }

  GameSchedule schedule = new GameSchedule();

  // Background

  class GameBackground {
    static final double speed = 0.12;
    static final Vec size = Vec.of(32, 32);

    Vec pos = Vec.zero(), vel = Vec.zero();

    static class Shake {
      int duration = 0;
      int radius = 0;
      Vec pos = Vec.zero();
    }

    Shake shake = new Shake();

    void changeDir() {
      double dir = Math.random() * Math.PI * 2;
      vel = Vec.ofPolar(dir, speed);
    }

    void shakeScreen(int duration, int radius) {
      shake.duration += duration;
      shake.radius = Math.max(shake.radius, radius);
    }

    void shakeScreen() {
      shakeScreen(6, 3);
    }

    void start() {
      changeDir();
    }

    void update() {
      pos = pos.add(vel).mod(size);

      if (shake.duration > 0) {
        shake.duration--;
        shake.pos = Vec.of(
          Math.random() * shake.radius,
          Math.random() * shake.radius
        );
      }
      else {
        shake.radius = 0;
        shake.pos = Vec.zero();
      }
    }

    void render() {
      Graphics.camera(shake.pos);

      Graphics.clear(palette[1]);
      Graphics.tiles(bgTiles, pos.minus(size));
    }
  }

  GameBackground bg = new GameBackground();

  // Input

  class GameCursor {
    Vec pos = Vec.zero();
    Vec start = Vec.zero(), end = Vec.zero();
    Vec dir = Vec.zero();

    boolean isDown = false, wasDown = false;

    void down() {
      start = pos.clone();
    }

    void up() {
      end = pos.clone();

      dir = getDir(start, end);

      switch (scene) {
        case TITLE -> title.onMouseUp();
        case GAME -> objects.onMouseUp();
        case MENU -> menu.onMouseUp();
      }
    }

    void update() {
      pos = Inputs.cursor();

      wasDown = isDown;
      isDown = Inputs.button(MouseButton.PRIMARY);

      if (!wasDown && isDown) {
        down();
      }
      else if (wasDown && !isDown) {
        up();
      }
    }

    void render() {
      shadowFillCircle(pos, isDown ? 2 : 3, palette[7]);
      Graphics.circle(pos, isDown ? 2 : 3, palette[0]);
    }
  }

  GameCursor cursor = new GameCursor();

  // Title Screen

  class GameTitle {
    double size = -1, buttonSize = -1;
    boolean showButton = false, hovering = false;

    class Hack {
      static final List<KeyCode> allKeys = List.of(
        KeyCode.UP,
        KeyCode.DOWN,
        KeyCode.LEFT,
        KeyCode.RIGHT,
        KeyCode.C,
        KeyCode.X
      );
      static final List<KeyCode> matchKeys = List.of(
        KeyCode.UP,
        KeyCode.UP,
        KeyCode.DOWN,
        KeyCode.DOWN,
        KeyCode.LEFT,
        KeyCode.RIGHT,
        KeyCode.LEFT,
        KeyCode.RIGHT,
        KeyCode.X,
        KeyCode.C
      );

      List<KeyCode> lastKeys = new ArrayList<>();

      void check() {
        for (KeyCode code : allKeys) {
          if (Inputs.keyOnce(code)) {
            lastKeys.add(code);

            if (lastKeys.size() > matchKeys.size()) {
              lastKeys.removeFirst();
            }
          }
        }

        if (lastKeys.size() < matchKeys.size()) return;

        for (int i = 0; i < matchKeys.size(); ++i) {
          if (lastKeys.get(i) != matchKeys.get(i)) return;
        }

        levels.max = levels.list.length - 1;

        scene = GameScene.GAME;
        levels.restart();
        bg.changeDir();
      }
    }

    Hack hack = new Hack();

    void update() {
      if (size < 2) {
        size = Math.min(Math.max(0.6, size * 1.06), 2);
      }
      else if (!showButton) {
        showButton = true;
        bg.shakeScreen();
      }

      hovering = inRect(cursor.pos, Vec.of(50, 80), Vec.of(27, 13));

      if (showButton && hovering) {
        if (buttonSize < 1.3) buttonSize *= 1.04;
      }
      else {
        buttonSize = Math.max(buttonSize * 0.93, 1);
      }

      hack.check();
    }

    Sprite titleSpr = sheet.slice(Vec.of(1, 1), 30, 13);
    Sprite buttonSpr = sheet.slice(Vec.of(1, 25), 19, 5);

    void render() {
      if (size > 0) {
        drawBob(Vec.of(52 - 30 * size * 0.3, 40), 1, size * 6.1);
        Graphics.easeSprite(
          titleSpr,
          Vec.of(64 - 30 * size * 0.2, 43 - 13 * size / 2),
          30 * size, 13 * size
        );
      }

      if (showButton) {
        Graphics.easeSprite(
          buttonSpr,
          Vec.of(64 - 19 * buttonSize / 2, 88 - 5 * buttonSize / 2),
          19 * buttonSize, 5 * buttonSize
        );
      }
    }

    void onMouseUp() {
      if (showButton && cursorStartAndEndIn(Vec.of(40, 70), Vec.of(47, 33))) {
        scene = GameScene.GAME;
        levels.restart();

        bg.changeDir();
        particles.radiate(Vec.of(64, 86), palette[7], 4);

        sfx((int) (18 + Math.random() * 3));
      }
    }
  }

  GameTitle title = new GameTitle();

  // Objects

  class BaseObject {
    boolean active = true;

    int index = 0;
    Vec pos = Vec.zero(), drawPos = Vec.zero();
  }

  class Void extends BaseObject {

  }

  class SolidObject extends BaseObject {
    double size = 0;
  }

  class Bob extends SolidObject {
    Vec wiggle = Vec.zero(), wiggleVel = Vec.zero();
    boolean isStart = false, isHovering = false;
    double flashRed = 0;
  }

  class Splitter extends SolidObject {

  }

  class Brick extends SolidObject {

  }

  class Spacer extends SolidObject {

  }

  class MovingBob extends BaseObject {
    BaseObject origin, target;
    Vec vel = Vec.zero();
  }

  Sprite reloadSpr = sheet.slice(Vec.of(81, 25), 5, 6);
  Sprite menuSpr = sheet.slice(Vec.of(89, 25), 5, 6);

  class GameObjects {
    static class ObjectColor {
      Color primary, secondary;

      public ObjectColor(Color primary, Color secondary) {
        this.primary = primary;
        this.secondary = secondary;
      }
    }

    final List<ObjectColor> colors = List.of(
      new ObjectColor(null, null),
      new ObjectColor(palette[15], palette[10]),
      new ObjectColor(palette[11], palette[ 3]),
      new ObjectColor(palette[ 6], palette[13]),
      new ObjectColor(palette[ 9], palette[ 4]),
      new ObjectColor(palette[14], palette[ 2])
    );

    List<BaseObject> list = new ArrayList<>();
    int bobsCount = 0;

    <T extends SolidObject>
    void add(T obj, Vec pos, int index, double size, Vec wiggle) {
      Vec drawStart = Vec.of(
        73 - 20 * (double) levels.w / 2,
        73 - 20 * (double) levels.h / 2
      );

      obj.pos = pos.clone();
      obj.drawPos = drawStart.add(pos.multiply(20));
      obj.index = index;
      obj.size = size;

      if (obj instanceof Bob bob) {
        bob.wiggle = Vec.zero();
        bob.wiggleVel = wiggle;
      }

      list.add(obj);
    }

    <T extends SolidObject>
    void add(T obj, Vec pos, int index, double size) {
      add(obj, pos, index, size, Vec.zero());
    }

    <T extends SolidObject>
    void add(T obj, Vec pos, int index) {
      add(obj, pos, index, 0);
    }

    <T extends SolidObject>
    void add(T obj, Vec pos) {
      add(obj, pos, -1);
    }

    void update() {
      list.removeIf(obj -> !obj.active);

      if (Inputs.keyOnce(KeyCode.C)) {
        scene = GameScene.MENU;
        sfx(21);
        return;
      }

      if (Inputs.keyOnce(KeyCode.X) && !list.isEmpty()) {
        list.clear();
        levels.restart();
        return;
      }

      bobsCount = 0;

      boolean skipBobsUpdate = false;

      for (BaseObject obj : list) {
        if (!obj.active) continue;

        if (obj instanceof MovingBob movBob) {
          skipBobsUpdate = true;

          movBob.pos = movBob.pos.add(movBob.vel);

          if ((time() * 60) % 2 < 1) {
            particles.ball(
              movBob.pos.add(Math.random() * 4 - 2, Math.random() * 4 - 2),
              colors.get(movBob.index).primary,
              Math.random() * 3 + 1,
              Vec.zero()
            );
          }

          for (BaseObject obj2 : list) {
            if (!obj2.active) continue;

            if (obj2 instanceof Brick brick && movBob.pos.inRange(brick.drawPos, 8)) {
              brick.size = 10.4;
            }
          }

          if (movBob.pos.inRange(movBob.target.drawPos, 8)) {
            bg.shakeScreen();

            if (movBob.target instanceof Void) {
              add(new Bob(), movBob.target.pos, movBob.index, 8, movBob.vel.multiply(0.45));
            } else if (movBob.target instanceof Bob tarBob) {
              tarBob.index--;
              tarBob.wiggle = Vec.zero();
              tarBob.wiggleVel = movBob.vel.multiply(1.1);

              particles.radiate(tarBob.drawPos, colors.get(obj.index).primary, tarBob.size);
              for (int j = 0; j < 12; ++j) {
                particles.ball(
                  tarBob.drawPos.add(Math.random() * 4 - 2, Math.random() * 4 - 2),
                  colors.get(movBob.index).primary,
                  1 + Math.random() * 3,
                  movBob.vel.multiply(0.46)
                    .add(Math.random() * 1.2 - 0.6, Math.random() * 1.2 - 0.6),
                  0
                );
              }

              sfx(19 - tarBob.index * 2);
            } else if (movBob.target instanceof Splitter tarSpl) {
              for (int d : new int[]{1, -1}) {
                Vec delta = Vec.of(
                  (movBob.vel.x == 0 ? 1 : 0) * d,
                  (movBob.vel.y == 0 ? 1 : 0) * d
                );

                MovingBob newMovBob = new MovingBob();

                newMovBob.index = movBob.index + 1;
                newMovBob.origin = movBob.target;

                newMovBob.target = new Void();
                newMovBob.target.pos = movBob.target.pos.add(delta);
                newMovBob.target.drawPos = movBob.target.drawPos.add(delta.multiply(20));

                newMovBob.pos = movBob.target.drawPos.clone();
                newMovBob.vel.x = Math.abs(movBob.vel.y) * d;
                newMovBob.vel.y = Math.abs(movBob.vel.x) * d;

                list.add(newMovBob);
              }

              particles.radiate(tarSpl.drawPos, palette[8], tarSpl.size);

              sfx(23);
              sfx(24);

              tarSpl.active = false;
            } else if (movBob.target instanceof Spacer tarSpa) {
              for (int j = 0; j < 12; ++j) {
                particles.ball(
                  movBob.target.drawPos.add(Math.random() * 4 - 2, Math.random() * 4 - 2),
                  palette[7],
                  1 + Math.random() * 3,
                  movBob.vel.multiply(0.46)
                    .add(Math.random() * 1.2 - 0.6, Math.random() * 1.2 - 0.6),
                  0
                );
              }

              Bob newBob = new Bob();

              newBob.index = movBob.index;
              newBob.wiggleVel = movBob.vel.multiply(1.1);

              newBob.size = tarSpa.size;
              newBob.pos = tarSpa.pos;
              newBob.drawPos = tarSpa.drawPos;

              tarSpa.active = false;
              list.add(newBob);

              sfx(31 - obj.index);
            }

            movBob.active = false;
          }
        } else if (obj instanceof SolidObject solObj) {
          if (solObj.size < 8) {
            solObj.size += 0.18 * (10 - solObj.size);
          }

          double maxSize = solObj instanceof Bob bob && bob.isStart ? 10.4 : 8;

          if (solObj.size > maxSize) {
            solObj.size = Math.max(solObj.size * 0.96, maxSize);
          }
        }
      }

      if (skipBobsUpdate) return;

      boolean notYetHovering = true;

      for (BaseObject obj : list) {
        if (!obj.active) continue;

        if (obj instanceof Bob bob && (bob.isStart || bob.isHovering)) {
          notYetHovering = false;
          break;
        }
      }

      for (BaseObject obj : list) {
        if (!obj.active) continue;

        if (obj instanceof Bob bob) {
          bobsCount++;

          if (bob.flashRed > 0) {
            bob.flashRed -= 0.5;
          }

          if (!bob.isHovering && cursor.isDown && cursor.pos.inRange(bob.drawPos, bob.size)) {
            bob.isHovering = true;

            if (!bob.isStart || notYetHovering) {
              bob.size = 13;

              schedule.add(() -> {
                particles.radiate(bob.drawPos, palette[0], bob.size, bob.size * 2, 1.7, true);
              }, 2);
            }

            if (notYetHovering && obj.index > 1) {
              sfx(20 - 2 * bob.index);
            }

            bob.isStart = notYetHovering;
            notYetHovering = false;
          }

          if (!bob.wiggleVel.isZero()) {
            bob.wiggleVel = bob.wiggleVel.add(bob.wiggle.multiply(-0.13));
            bob.wiggle = bob.wiggle.add(bob.wiggleVel);
            bob.wiggleVel = bob.wiggleVel.multiply(0.82);

            if (bob.wiggle.inRange(0.1) || bob.wiggleVel.inRange(0.1)) {
              bob.wiggle = Vec.zero();
              bob.wiggleVel = Vec.zero();
            }
          }
        }
      }
    }

    Sprite brickSpr = sheet.slice(Vec.of(73, 1), 18, 18);
    Sprite splitterSpr = sheet.slice(Vec.of(49, 1), 18, 18);

    void render() {
      for (BaseObject obj : list) {
        if (!obj.active) continue;

        if (obj instanceof Bob bob) {
          drawBob(bob.drawPos.add(bob.wiggle), bob.index, bob.size, bob.flashRed);
        }
        else if (obj instanceof MovingBob movBob) {
          drawBob(movBob.pos, movBob.index, 8);
        }
        else if (obj instanceof Brick bri) {
          Graphics.map(palette[8], colors.get(bri.index).primary);
          Graphics.map(palette[14], colors.get(bri.index).secondary);
          Graphics.easeSprite(
            brickSpr,
            obj.drawPos.minus(bri.size, bri.size),
            bri.size * 2 + 2, bri.size * 2 + 2
          );
          Graphics.map(palette[8], palette[8]);
          Graphics.map(palette[14], palette[14]);
        }
        else if (obj instanceof Splitter spl) {
          Graphics.easeSprite(
            splitterSpr,
            obj.drawPos.minus(spl.size, spl.size),
            spl.size * 2 + 2, spl.size * 2 + 2
          );
        }
        else if (obj instanceof Spacer spa) {
          for (int dx = 0; dx <= 1; ++dx) {
            for (int dy = 0; dy <= 1; ++dy) {
              Graphics.circle(
                spa.drawPos.add(dx + 1, dy + 1),
                spa.size - 1,
                palette[0]
              );
            }
          }

          for (int dx = 0; dx <= 1; ++dx) {
            for (int dy = 0; dy <= 1; ++dy) {
              Graphics.circle(
                spa.drawPos.add(dx, dy),
                spa.size - 1,
                palette[7]
              );
            }
          }
        }
      }

      Graphics.sprite(
        reloadSpr,
        Vec.of(115, 1).add(bg.shake.pos)
      );
      Graphics.sprite(
        menuSpr,
        Vec.of(122, 1).add(bg.shake.pos)
      );
    }

    void onMouseUp() {
      if (cursorStartAndEndIn(Vec.of(114, 0), Vec.of(8, 8))) {
        objects.list.clear();
        levels.restart();
        sfx(25);
        return;
      }

      if (cursorStartAndEndIn(Vec.of(120, 0), Vec.of(8, 8))) {
        scene = GameScene.MENU;
        sfx(21);
        return;
      }

      Bob startBob = null;

      for (BaseObject obj : list) {
        if (!obj.active) continue;

        if (obj instanceof Bob bob) {
          if (bob.isStart) {
            startBob = bob;
          }

          bob.isHovering = false;
        }
      }

      for (BaseObject obj : list) {
        if (!obj.active) continue;

        if (obj instanceof MovingBob) {
          return;
        }
      }

      boolean isSwiping = true;

      Vec dir = cursor.dir == null ? null : cursor.dir.clone();

      if (dir == null) {
        for (BaseObject obj : list) {
          if (!obj.active) continue;

          if (obj instanceof Bob bob) {
            bob.isStart = false;
          }
        }
        return;
      }

      if (startBob != null && cursor.dir.isZero()) {
        if (bobsCount == 1) {
          Bob lastBob = null;

          for (BaseObject obj : list) {
            if (!obj.active) continue;

            if (obj instanceof Bob bob) {
              lastBob = bob;
              bob.active = false;

              particles.radiate(bob.drawPos, colors.get(bob.index).primary, bob.size);
              schedule.add(() -> {
                particles.radiate(bob.drawPos, palette[0], bob.size, true);
              }, 14);

              sfx((int) (18 + Math.random() * 3));

              schedule.add(() -> {
                levels.next();
              }, 32);
              break;
            }
          }

          if (lastBob == null) return;

          for (BaseObject obj : list) {
            if (!obj.active) continue;

            if (obj instanceof SolidObject solObj) {
              double vanishTime = 12 + solObj.drawPos.dist(lastBob.drawPos) / 3.4;

              schedule.add(() -> {
                solObj.size = 6;
              }, vanishTime - 2);
              schedule.add(() -> {
                solObj.size = 3;
              }, vanishTime - 1);
              schedule.add(() -> {
                solObj.active = false;
              }, vanishTime);
            }
          }

          return;
        }

        isSwiping = false;

        dir = getDir(startBob.drawPos, cursor.pos);

        if (dir == null) {
          for (BaseObject obj : list) {
            if (!obj.active) continue;

            if (obj instanceof Bob bob) {
              bob.isStart = bob.drawPos.inRange(cursor.pos, bob.size);
            }
          }

          return;
        }
        else if (dir.isZero()) {
          return;
        }
      }

      if (isSwiping) {
        if (dir.isZero()) return;

        for (BaseObject obj : list) {
          if (!obj.active) continue;

          if (obj instanceof Bob bob) {
            bob.isStart = bob.drawPos.inRange(cursor.start, bob.size);

            if (bob.isStart) {
              startBob = bob;
            }
          }
        }
      }

      if (startBob == null) return;

      Vec pos = startBob.pos.clone();

      boolean found = false, blocked = false;

      do {
        pos = pos.add(dir);

        for (BaseObject obj : list) {
          if (obj.pos.equals(pos)) {
            if (obj instanceof Bob bob && bob.index != startBob.index) {
              blocked = true;
            }

            if (obj instanceof Splitter spl) {
              if (startBob.index == 5) {
                blocked = true;
              }
              else {
                for (BaseObject obj2 : list) {
                  if (!obj2.active) continue;

                  if (obj2 instanceof SolidObject solObj2) {
                    if (
                      (solObj2.pos.x == pos.x + dir.y && solObj2.pos.y == pos.y + dir.x) ||
                      (solObj2.pos.x == pos.x + dir.y && solObj2.pos.y == pos.y - dir.x) ||
                      (solObj2.pos.x == pos.x - dir.y && solObj2.pos.y == pos.y + dir.x) ||
                      (solObj2.pos.x == pos.x - dir.y && solObj2.pos.y == pos.y - dir.x)
                    ) {
                      blocked = true;
                      break;
                    }
                  }
                }
              }
            }

            if (blocked) break;

            if (!(obj instanceof Brick)) {
              MovingBob movBob = new MovingBob();
              movBob.index = startBob.index;
              movBob.origin = startBob;
              movBob.target = obj;
              movBob.pos = startBob.drawPos;
              movBob.vel = dir.multiply(6.4);

              list.add(movBob);
              startBob.active = false;

              found = true;
              break;
            }
          }
        }

        if (found || blocked) break;
      } while (inRect(pos, Vec.zero(), Vec.of(levels.w + 2, levels.h + 2)));

      if (blocked) {
        boolean foundNewStart = false;

        for (BaseObject obj : list) {
          if (!obj.active) continue;

          if (obj instanceof Bob bob) {
            bob.isStart = bob.drawPos.inRange(cursor.pos, bob.size);
            if (bob.isStart) {
              foundNewStart = true;
            }
          }
        }

        if (isSwiping || !foundNewStart) {
          startBob.flashRed = 4;
        }
      }
      else if (isSwiping && !found) {
        for (BaseObject obj : list) {
          if (!obj.active) continue;

          if (obj instanceof Bob bob) {
            bob.isStart = false;
          }
        }
      }
    }
  }

  GameObjects objects = new GameObjects();

  void drawBob(Vec pos, int index, double size, double red) {
    size -= red;

    shadowFillCircle(pos, size, palette[0]);
    Graphics.fillCircle(pos, size - 1, red > 0 ? palette[8] : objects.colors.get(index).primary);
    Graphics.fillCircle(pos, size - 3, red > 0 ? palette[14] : objects.colors.get(index).secondary);

    double horiz = Math.ceil(size * 0.64 - 2);
    double up = -size * 0.07, down = size * 0.05;

    if (size > 4) {
      Graphics.fillRect(pos.add( horiz, up), pos.add( horiz - 1, down), palette[0]);
      Graphics.fillRect(pos.add(-horiz, up), pos.add(-horiz + 1, down), palette[0]);
    }
  }

  void drawBob(Vec pos, int index, double size) {
    drawBob(pos, index, size, 0);
  }

  void drawBob(Vec pos, int index) {
    drawBob(pos, index, 8);
  }

  // Levels

  class GameLevels {
    int musicId = 0;

    int[][][] list = {
      { //  0
        { 1}
      },
      {
        { 2},
        { 0},
        { 0},
        { 0},
        { 2}
      },
      {
        { 3,  0,  3},
        { 0,  0,  0},
        { 0,  0,  0},
        { 0,  0,  0},
        { 0,  0,  2}
      },
      {
        { 3,  0,  0,  0,  3},
        { 0,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0},
        { 3,  0,  0,  0,  3}
      },
      {
        { 2,  4,  4},
        { 0,  0,  0},
        { 0,  0,  3}
      },
      { //  5
        { 0,  3,  0,  3},
        { 0,  0,  0,  0},
        { 4,  4,  0,  0},
        { 0,  0,  0,  0},
        { 0,  3,  0,  0}
      },
      {
        { 0,  4,  0,  4},
        { 0,  0,  0,  4},
        { 0,  0,  0,  0},
        { 4,  4,  0,  4},
        { 0,  0,  0,  0},
        { 0,  3,  0,  0}
      },
      {
        { 0,  0,  5},
        { 2,  0,  0},
        { 0,  0,  0},
        { 4,  0,  5},
        { 0,  0,  0},
        { 3,  0,  0}
      },
      {
        { 4,  0,  3,  0,  0},
        { 0,  0,  0,  0,  0},
        { 4,  0,  4,  0,  5},
        { 0,  0,  0,  0,  0},
        { 0,  0,  4,  0,  4},
        { 0,  0,  0,  0,  5}
      },
      {
        { 4,  0,  0,  3,  0,  0},
        { 0,  0,  0,  0,  0,  0},
        { 5,  5,  5,  5,  0,  5},
        { 0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  4,  0,  5},
        { 0,  0,  0,  0,  0,  4}
      },
      { // 10
        { 3,  0,  0,  4,  0,  0},
        { 0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  4,  0,  0},
        { 0,  0,  0,  4,  0,  4},
        { 0,  0,  0,  4,  0,  0},
        { 0,  0,  0,  0,  0,  4}
      },
      {
        { 4,  4,  4},
        { 4,  0,  4},
        { 4,  4,  4}
      },
      {
        { 5,  0,  0,  0,  0},
        { 5,  0,  4,  0,  5},
        { 4,  0,  3,  0,  5},
        { 0,  0,  5,  0,  5},
        { 0,  0,  4,  0,  0}
      },
      {
        { 0,  5,  0,  5,  0},
        { 0,  5,  5,  5,  0},
        { 4,  5,  5,  5,  4},
        { 0,  5,  5,  5,  0},
        { 0,  0,  5,  0,  0}
      },
      {
        { 5,  0,  5,  5,  0,  5},
        { 0,  0,  0,  0,  0,  0},
        { 5,  0,  5,  5,  0,  5},
        { 5,  0,  5,  5,  0,  5},
        { 0,  0,  0,  0,  0,  0},
        { 5,  0,  5,  5,  0,  5}
      },
      { // 15
        { 4,  0,  5,  0,  4},
        { 0,  0,  5,  0,  0},
        { 5,  5,  0,  5,  5},
        { 0,  0,  5,  0,  0},
        { 4,  0,  5,  0,  4}
      },
      {
        { 5,  5,  5,  4},
        { 5,  3,  5,  0},
        { 5,  5,  5,  4}
      },
      {
        { 0,  5,  0,  5,  0},
        { 0,  5,  5,  5,  0},
        { 4,  5,  5,  5,  4},
        { 0,  5,  5,  5,  0},
        { 0,  0,  0,  5,  0}
      },
      {
        { 4,  0,  4,  0,  5},
        { 0,  0,  0,  0,  0},
        { 0,  0,  2,  0,  0},
        { 0,  0,  0,  0,  0},
        { 5,  0,  5,  0,  5}
      },
      {
        { 2,  0},
        { 0,  2},
        { 0,  0},
        { 0, 20}
      },
      { // 20
        {20,  0,  5,  5,  5},
        { 0,  0,  5,  2,  5},
        { 0,  0,  5,  5,  5},
      },
      {
        { 3,  0,  0,  3},
        { 0,  0,  0,  0},
        {20,  0,  0, 20},
        { 0,  0,  0,  0},
        { 3,  0,  0,  3}
      },
      {
        { 0, 20,  0,  0,  3,  0},
        { 0,  0,  0,  0,  0,  0},
        { 2,  0,  0, 20,  0,  0},
        { 0,  0,  0,  0,  0,  3}
      },
      {
        { 2, 20,  0, 20,  2}
      },
      {
        { 4, 20,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0},
        { 0,  3,  0,  0,  0,  0},
        { 4,  0,  3,  0, 20,  3}
      },
      { // 25
        { 0,  0,  0, 20,  0},
        { 0,  0,  0,  3,  0},
        { 4,  3,  5,  0,  5},
        { 0,  0,  0,  0,  4},
        { 0, 20,  0,  0,  4},
        { 0,  0,  0,  0,  0}
      },
      {
        { 0,  5,  0},
        { 0, 20,  0},
        { 4,  3,  0},
        {20,  0,  0},
        { 4,  5,  3},
        { 4,  0,  0}
      },
      {
        { 4,  0,  0,  0,  3,  4},
        { 0,  0,  0,  0,  0,  0},
        { 0, 20,  0,  0, 20,  0},
        { 0,  0,  0,  0,  0,  0},
        { 4,  3,  0,  0,  0,  4}
      },
      {
        { 0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  0, 20,  3},
        { 0, 20,  0,  0,  0,  0},
        { 3,  0,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0,  3},
        { 3,  0,  0,  0,  0,  0}
      },
      {
        { 0,  0,  0,  0,  0,  0},
        { 3,  3,  0,  0, 20,  0},
        { 0,  0,  0, 20,  0,  0},
        { 0, 20,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0},
        { 0,  3,  0,  0,  0,  3}
      },
      { // 30
        { 5,  4},
        { 0,  3},
        { 0,  0},
        { 0, 20},
        { 5,  0},
        { 3,  3}
      },
      {
        { 0,  0,  0,  4,  0,  0},
        { 0,  0,  0,  0,  0,  4},
        { 0, 20,  0,  0,  3,  0},
        { 0,  0,  0,  4,  0,  4},
        {20,  0,  0,  0,  4,  0},
        { 0,  0,  0,  0,  0,  4}
      },
      {
        { 0,  4,  0,  4,  0,  3},
        { 0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  5,  0,  5},
        { 0, 20,  0,  0,  3,  0},
        { 0,  0,  0,  5,  0,  5}
      },
      {
        { 0,  0,  5,  0,  5,  0},
        { 0,  0,  0,  0,  0,  0},
        { 4,  0, 20,  0,  0,  3},
        { 0,  0,  0,  0,  0,  0},
        { 0,  4,  4,  0,  4,  4}
      },
      {
        { 0,  0,  0, 20,  0,  0},
        { 0,  0,  0,  0,  0,  0},
        { 0,  0,  0,  3,  0,  3},
        { 0,  0,  3,  0,  0,  0},
        { 5,  0,  5,  0,  4,  0}
      },
      { // 35
        { 0,  0,  0,  0,  5,  3},
        { 4,  0,  0,  0,  5,  0},
        { 0,  0, 20,  0,  5,  5},
        { 0,  0,  0,  0,  0,  4},
        { 4,  0,  4,  0,  0,  0}
      },
      {
        { 0,  0,  0,  0,  4},
        { 0,  0,  0,  0,  0},
        { 0,  3,  0,  0,  0},
        {20,  0,  0,  0,  5},
        { 0,  2,  0,  0,  0},
        { 0,  0,  0,  0,  5}
      },
      {
        { 3,  0,  3,  0,  0},
        { 0,  0,  0,  0,  5},
        { 4, 20,  0,  0,  0},
        { 3,  0,  0,  0,  0},
        { 0,  5,  0,  0,  0}
      },
      // {
      //   { 2,  0, 20,  0,  0,  0},
      //   { 5,  0,  0,  0,  0,  0},
      //   { 0,  3,  0,  0, 20,  0},
      //   { 0,  0,  0,  0,  0,  0},
      //   { 5,  0, 20,  0,  0,  4} // ! slightly out of screen
      // },
      // {
      //   {20,  0,  0,  0, 20},
      //   { 0,  0,  0,  0,  0},
      //   { 0,  0,  0,  0,  3},
      //   { 0,  0,  2,  0,  0},
      //   { 3,  0,  0,  0,  0},
      //   { 0,  0, 20,  0,  0} // ! slightly out of screen
      // },
      { // 40
        { 2,  0, 12,  0,  2}
      },
      {
        { 4,  0,  3,  0,  0},
        { 0,  0,  0,  0,  0},
        { 4,  0, 12,  0,  4},
        { 0,  0,  0,  0,  0},
        { 0,  0,  3,  0,  4}
      },
      {
        { 4,  4,  4,  4},
        { 0,  0,  0,  0},
        {12, 12, 12, 12},
        { 0,  0,  0,  0},
        { 4,  4,  4,  4},
      },
      {
        { 5, 15,  5,  5,  0},
        { 0,  0,  0,  0,  0},
        { 5,  0,  0, 20,  0},
        { 0,  0,  0,  0,  0},
        { 3,  0, 12,  0,  3},
        { 0,  0,  0,  0,  3},
      },
      {
        { 0,  5,  0, 14,  0,  4},
        { 0, 15,  0,  0,  0, 13},
        { 0,  5,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0,  0},
        { 0,  4,  4, 12,  0,  3},
        { 5,  5,  0,  5,  0,  5}
      },
      { // 45
        {20,  2, 15,  2, 20}
      },
      {
        { 0,  0,  0,  0},
        {20,  0,  0, 20},
        { 0,  0, 13,  0},
        { 0,  2, 13,  2},
        { 0,  0, 13,  0},
        { 0, 20,  0,  0}
      },
      {
        { 0,  0,  4, 14,  4},
        { 0,  0,  0,  0, 14},
        { 4,  0,  4,  0,  3},
        { 0,  0,  5,  0,  0},
        { 0,  0,  5,  0,  4}
      },
      {
        { 5,  5, 15,  5, 14,  4},
        { 0,  0,  0,  0,  0, 13},
        {15,  0,  3, 12,  0,  3},
        { 0,  0,  0,  0,  0,  0},
        { 5, 14,  4,  0,  0,  0}
      },
      {
        { 4,  0,  4,  0,  3},
        { 0,  0,  0,  0,  0},
        { 0,  0, 14,  0,  0},
        { 0,  0,  0,  0,  0},
        { 3,  0,  4,  0,  4}
      },
      // { // 50
      //   { 4,  0,  0, 20,  0,  0},
      //   { 0,  0, 13,  0,  0,  4},
      //   { 0,  0,  3,  0,  0,  5},
      //   { 0,  0,  0,  3,  0,  0},
      //   { 0,  0,  0, 20,  0,  0},
      //   { 4,  0,  0,  0,  0,  5} // ! out of screen
      // },
      // {
      //   { 0,  0, 20,  0,  4},
      //   { 3,  0,  0,  0,  0},
      //   { 3,  0,  4,  0,  0},
      //   { 0,  0, 15,  0,  0},
      //   {20,  0,  4,  0,  4} // ! slightly out of screen
      // },
      {
        { 4,  0,  0, 13,  0,  3},
        { 0,  0,  0,  0,  0,  0},
        { 5,  0,  5, 15,  5,  5},
        { 0,  0,  0,  0,  0,  0},
        { 3,  0, 20,  0,  0,  4},
        { 0,  0,  0,  0,  0,  0}
      },
      {
        { 5,  0,  4,  0,  0},
        {15,  0,  0, 20,  0},
        { 5,  0, 13,  0,  0},
        { 5,  0,  4,  0,  4},
        { 5,  0,  0,  3,  0},
        { 5,  0,  0,  0,  5}
      },
      // {
      //   { 0,  0, 20,  0,  0,  4},
      //   { 0, 14,  0,  0,  0,  0},
      //   { 0,  0, 13, 14,  0,  0},
      //   { 0, 20,  3,  3, 14,  4},
      //   {20,  0,  0, 14,  0,  0},
      //   { 0,  0, 13,  4, 14,  4} // ! out of screen
      // },
      { // 55
        { 0,  0,  0,  4,  0,  3},
        { 0, 20,  0, 14,  4,  0},
        { 0,  0,  0,  0,  0, 15},
        { 0,  0,  0,  0,  0, 15},
        { 0, 20,  0, 14,  4,  0},
        { 0,  0,  0,  4,  0,  3}
      },
      {
        { 3,  0,  4,  0,  4},
        { 0,  0,  0,  0,  0},
        { 5, 15,  5,  5,  5},
        {15,  0,  0,  0,  0},
        { 0,  0,  0,  0,  0},
        { 5,  0,  5,  4,  0}
      },
      {
        { 4,  0,  5,  3},
        {12,  0,  0,  0},
        { 0,  0,  0, 15},
        {20,  0,  0,  3},
        { 0,  0,  0,  0},
        { 0,  0,  5,  3}
      },
      {
        { 5,  5,  0,  4,  0,  0},
        { 5,  0,  0,  4,  4,  0},
        { 0,  0,  0,  0,  0,  0},
        {15,  0,  0,  4, 14,  4},
        { 0, 20,  0,  0,  4,  0},
        { 5,  0,  0,  0,  0,  0}
      },
      {
        { 3, 13,  0,  0,  0},
        {13,  0,  0,  0, 13},
        { 0,  3,  0,  3,  0},
        {13,  0,  0,  0, 13},
        { 0, 20,  0, 13,  3},
        { 0,  0,  0,  0,  0}
      },
      { // 60
        {21,  0,  2},
        { 0,  0,  0},
        { 0,  0,  0},
        { 0,  0,  0},
        { 2,  0,  0}
      },
      {
        {21, 21,  2},
        { 0, 13, 13},
        {21,  0, 21},
        {13, 13,  0},
        { 2, 21, 21}
      },
      {
        { 4,  0,  3,  0,  5},
        { 0,  0,  0,  0,  0},
        { 5,  0, 21,  0,  5},
        { 0,  0,  0,  0,  0},
        { 5,  0,  3,  0,  4}
      },
      {
        { 3,  0,  0,  0},
        { 0,  0,  2,  0},
        {21, 12, 21,  3},
        { 0,  0,  0,  0},
        {20,  0,  0, 21},
        { 0,  0,  0,  0}
      },
      { // originally 38
        { 0,  0,  0,  0,  0,  0},
        { 2,  0, 20,  0,  0,  0},
        { 5,  0,  0,  0,  0,  0},
        { 0,  3,  0,  0, 20,  0},
        { 0,  0, 21,  0,  0,  0},
        { 5,  0, 21,  0,  0,  4}
      },
      { // originally 51
        { 0,  0,  0,  0,  0},
        { 0,  0, 20,  0,  4},
        { 3,  0,  0,  0,  0},
        { 3,  0,  4,  0,  0},
        { 0,  0, 15,  0,  0},
        {21,  0,  4,  0,  4}
      },
      { // originally 50
        { 4,  0,  0, 21,  0,  0},
        { 0,  0, 13, 21,  0,  4},
        { 0,  0,  3,  0,  0,  5},
        { 0,  0,  0,  3,  0,  0},
        { 0,  0,  0, 20,  0,  0},
        { 4,  0,  0,  0,  0,  5}
      },
      { // originally 54
        { 0,  0, 21,  0,  0,  4},
        { 0, 14,  0,  0,  0,  0},
        { 0,  0, 13, 14,  0,  0},
        { 0, 20,  3,  3, 14,  4},
        {20,  0,  0, 14,  0,  0},
        { 0,  0, 13,  4, 14,  4}
      },
      {}
    };

    // int max = loadStats(0);
    int max = 0;

    int current = max == list.length - 1 ? 0 : max;

    int w = 0, h = 0;

    void resetMax() {
      max = 1;
      current = max;
    }

    void load(int index) {
      int[][] lv = list[index];

      h = lv.length;
      w = h > 0 ? lv[0].length : 0;

      for (int y = 0; y < h; ++ y) {
        for (int x = 0; x < w; ++ x) {
          int type = lv[y][x];

          if (type >= 1 && type <= 5) {
            objects.add(new Bob(), Vec.of(x, y), type);
          }
          else if (type >= 11 && type <= 15) {
            objects.add(new Brick(), Vec.of(x, y), type - 10);
          }
          else if (type == 20) {
            objects.add(new Splitter(), Vec.of(x, y));
          }
          else if (type == 21) {
            objects.add(new Spacer(), Vec.of(x, y));
          }
        }
      }
    }

    void restart() {
      max = Math.max(current, max);
      // setStats(0, max);
      load(current);

      if (current == list.length - 1) {
        schedule.add(() -> {
          scene = GameScene.CREDITS;
        }, 24);
      }

      int targetMusicId =
        current >= 56 ? 3 :
        current >= 39 ? 2 :
        current >= 20 ? 1 :
        0;

      if (musicId != targetMusicId) {
        musicId = targetMusicId;
        music(musicId);
      }
    }

    void next() {
      bg.changeDir();

      current++;
      restart();
    }

    void start() {
      music(musicId);
    }
  }

  GameLevels levels = new GameLevels();

  // Menu

  class GameMenu {
    static final int lineLength = 7;

    void update() {
      if (Inputs.keyOnce(KeyCode.C)) {
        scene = GameScene.GAME;
        sfx(22);
        return;
      }
    }

    Sprite arrowSpr = sheet.slice(Vec.of(33, 1), 6, 10);

    void render() {
      for (int i = 1; i <= 5; ++i) {
        drawBob(Vec.of(3 + 20 * i, 12), 6 - i, 4);
        if (i < 5) {
          Graphics.sprite(arrowSpr, Vec.of(11 + 20 * i, 8));
        }
      }

      for (int i = 0; i < levels.list.length; ++i) {
        int x = i % lineLength, y = i / lineLength;

        if (inRect(cursor.pos, Vec.of(9 + x * 16, 24 + y * 10), Vec.of(12, 9))) {
          Graphics.rect(
            Vec.of(10 + x * 16, 25 + y * 10),
            Vec.of(22 + x * 16, 34 + y * 10),
            palette[0]
          );
          Graphics.rect(
            Vec.of(9 + x * 16, 24 + y * 10),
            Vec.of(21 + x * 16, 33 + y * 10),
            palette[i <= levels.max ? 6 : 14]
          );
        }
        else {
          Graphics.fillRect(
            Vec.of(11 + x * 16, 26 + y * 10),
            Vec.of(21 + x * 16, 33 + y * 10),
            palette[0]
          );
        }

        Graphics.fillRect(
          Vec.of(10 + x * 16, 25 + y * 10),
          Vec.of(20 + x * 16, 32 + y * 10),
          palette[i == levels.current ? 3 : i <= levels.max ? 13 : 5]
        );

        printNumber(
          i + 1,
          Vec.of(16 + x * 16, 26 + y * 10),
          palette[i <= levels.max ? 7 : 10]
        );
      }

      Graphics.sprite(menuSpr, Vec.of(122, 1));
    }

    void onMouseUp() {
      if (cursorStartAndEndIn(Vec.of(120, 0), Vec.of(8, 8))) {
        scene = GameScene.GAME;
        sfx(22);
        return;
      }

      for (int i = 0; i < levels.list.length; ++i) {
        int x = i % lineLength, y = i / lineLength;

        if (cursorStartAndEndIn(Vec.of(9 + x * 16, 24 + y * 10), Vec.of(12, 9))) {
          if (i <= levels.max) {
            levels.current = i;
            objects.list.clear();
            bg.changeDir();
            levels.restart();
            scene = GameScene.GAME;
            sfx(25);
          }
          else {
            bg.shakeScreen();
          }
          break;
        }
      }
    }
  }

  GameMenu menu = new GameMenu();

  // Credits

  class GameCredits {
    static final int[] bobsIndexes = { 2, 4, 1, 5 };

    double size = -1;
    double bobsCount = 0;

    static class FakeBob {
      double y = 0;
      double yv = 0;
      boolean stopped = false;
    }

    List<FakeBob> bobPhysics = new ArrayList<>();

    void update() {
      if (size < 2) {
        size = Math.min(Math.max(0.6, size * 1.06), 2);
      }
      else {
        if (bobsCount == 0) {
          bg.shakeScreen();
        }

        if (bobsCount < 4) {
          bobsCount += 0.1;
        }
      }

      while (bobPhysics.size() < bobsCount) {
        bobPhysics.add(new FakeBob());
      }

      for (FakeBob fakBob : bobPhysics) {
        if (!fakBob.stopped) {
          fakBob.y += fakBob.yv;

          if (fakBob.y > 0) {
            fakBob.y = 0;
            fakBob.stopped = true;

            schedule.add(() -> {
              fakBob.yv = -3;
              fakBob.stopped = false;
            }, 24);
          }

          fakBob.yv += 0.2;
        }
      }
    }

    Sprite youWinSpr = sheet.slice(Vec.of(1, 17), 31, 6);

    void render() {
      if (size > 0) {
        Graphics.easeSprite(
          youWinSpr,
          Vec.of(64 - 31 * size / 2, 36 - 6 * size / 2),
          31 * size, 6 * size
        );
      }

      if (bobsCount > 0) {
        Color textCol = palette[(time() * 60) % 40 < 18 ? 10 : 7];

        Graphics.print("Buy the original game", Vec.of(23, 51), palette[0]);
        Graphics.print("Buy the original game", Vec.of(23, 50), textCol);
        Graphics.print("For more levels!", Vec.of(34, 59), palette[0]);
        Graphics.print("For more levels!", Vec.of(34, 58), textCol);
      }

      for (int i = 0; i < bobsCount; ++i) {
        drawBob(Vec.of(34 + 20 * i, 102 + bobPhysics.get(i).y), bobsIndexes[i]);
      }
    }
  }

  GameCredits credits = new GameCredits();

  // Particles

  class BaseParticle {
    boolean active = true;
    Vec pos = Vec.zero();
    Color col = palette[7];
    double r = 0, dr = 0;
    boolean noShadow = false;
  }

  class RadianceParticle extends BaseParticle {
    double maxRSq = 0;
  }

  class BallParticle extends BaseParticle {
    Vec vel = Vec.zero();
  }

  class GameParticles {
    List<BaseParticle> list = new ArrayList<>();

    void _radiate(Vec pos, Color col, double r, double maxRSq, double dr, boolean noShadow) {
      RadianceParticle rp = new RadianceParticle();
      rp.pos = pos.clone();
      rp.col = col;
      rp.r = r;
      rp.dr = dr;
      rp.maxRSq = maxRSq;
      rp.noShadow = noShadow;

      list.add(rp);
    }

    void radiate(Vec pos, Color col, double r, double maxR, double dr, boolean noShadow) {
      _radiate(pos, col, r, maxR * maxR, dr, noShadow);
    }

    void radiate(Vec pos, Color col, double r, double dr, boolean noShadow) {
      double
        r1 = pos.distSq(-1, -1),
        r2 = pos.distSq(128, -1),
        r3 = pos.distSq(-1, 128),
        r4 = pos.distSq(128, 128);

      double maxRSq = Math.max(Math.max(Math.max(r1, r2), r3), r4);
      _radiate(pos, col, r, maxRSq, dr, noShadow);
    }

    void radiate(Vec pos, Color col, double r, boolean noShadow) {
      radiate(pos, col, r, 3.4, noShadow);
    }

    void radiate(Vec pos, Color col, double r) {
      radiate(pos, col, r, false);
    }

    void ball(Vec pos, Color col, double r, Vec vel, double dr, boolean noShadow) {
      BallParticle bp = new BallParticle();
      bp.pos = pos.clone();
      bp.col = col;
      bp.r = r;
      bp.dr = dr;
      bp.vel = vel.clone();
      bp.noShadow = noShadow;

      list.add(bp);
    }

    void ball(Vec pos, Color col, double r, Vec vel, double dr) {
      ball(pos, col, r, vel, dr, false);
    }
    void ball(Vec pos, Color col, double r, Vec vel) {
      ball(pos, col, r, vel, -0.06);
    }

    void update() {
      list.removeIf(p -> !p.active);

      for (BaseParticle p : list) {
        if (!p.active) continue;

        p.r += p.dr;

        if (p instanceof RadianceParticle rp) {
          if (rp.r * rp.r > rp.maxRSq) {
            rp.active = false;
          }
        }
        else if (p instanceof BallParticle bp) {
          bp.pos = bp.pos.add(bp.vel);
          if (bp.r < 0 || !inRect(bp.pos, Vec.of(-bp.r, -bp.r), Vec.of(128 + bp.r * 2, 128 + bp.r * 2))) {
            bp.active = false;
          }
        }
      }
    }

    void render() {
      for (BaseParticle p : list) {
        if (!p.active) continue;

        if (p instanceof RadianceParticle rp) {
          if (rp.noShadow) {
            Graphics.circle(rp.pos, rp.r, rp.col);
          }
          else {
            shadowCircle(rp.pos, rp.r, rp.col);
          }
        }
        else if (p instanceof BallParticle bp) {
          if (bp.noShadow) {
            Graphics.fillCircle(bp.pos, bp.r, bp.col);
          }
          else {
            shadowFillCircle(bp.pos, bp.r, bp.col);
          }
        }
      }
    }
  }

  GameParticles particles = new GameParticles();
}
