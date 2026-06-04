package dev.huey.picofx.game;

import dev.huey.picofx.api.Entry;
import dev.huey.picofx.api.bases.Game;
import dev.huey.picofx.api.bases.Lifecycle;
import dev.huey.picofx.api.bases.Vec;
import dev.huey.picofx.api.items.Font;
import dev.huey.picofx.api.items.Sound;
import dev.huey.picofx.api.items.Sprite;
import dev.huey.picofx.api.items.TileMap;
import dev.huey.picofx.api.modules.Audios;
import dev.huey.picofx.api.modules.Graphics;
import dev.huey.picofx.api.modules.Inputs;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import lombok.Builder;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SuperDiscBox implements Game {
  @Override
  public String getId() {
    return "super-disc-box";
  }

  @Override
  public String getName() {
    return "Super Disc Box";
  }

  Font font = Font.pico8();

  Sprite sheet = Sprite.load("sheet");

  Map<Character, Sprite> tileSprites = Map.ofEntries(
    Map.entry('=', sheet.slice(Vec.ofInt(8, 0), 8, 8).flag(1)),
    Map.entry('I', sheet.slice(Vec.ofInt(16, 0), 8, 8).flag(1)),
    Map.entry('#', sheet.slice(Vec.ofInt(24, 0), 8, 8).flag(1)),
    Map.entry('-', sheet.slice(Vec.ofInt(32, 0), 8, 8).flag(0)),
    Map.entry('|', sheet.slice(Vec.ofInt(40, 0), 8, 8).flag(0)),
    Map.entry('U', sheet.slice(Vec.ofInt(0, 8), 8, 8).flag(1)),
    Map.entry('V', sheet.slice(Vec.ofInt(8, 8), 8, 8).flag(1)),
    Map.entry('Q', sheet.slice(Vec.ofInt(16, 8), 8, 8).flag(1)),
    Map.entry('O', sheet.slice(Vec.ofInt(24, 8), 8, 8).flag(1)),
    Map.entry('_', sheet.slice(Vec.ofInt(32, 8), 8, 8).flag(1))
  );

  TileMap gameTiles = TileMap.builder()
    .source("game")
    .gridWidth(8).gridHeight(8)
    .map(tileSprites)
    .build();
  TileMap titleTiles = TileMap.builder()
    .source("title")
    .gridWidth(8).gridHeight(8)
    .map(tileSprites)
    .build();

  enum SpriteID {
    LOGO,
    PL_RUN_0,
    PL_RUN_1,
    PL_RUN_2,
    PL_RUN_3,
    PL_RUN_4,
    PL_IDLE_0,
    PL_IDLE_1,
    PL_IDLE_2,
    PL_IDLE_3,
    PL_IDLE_4,
    DISC_BIG_0,
    DISC_BIG_1,
    DISC_BIG_2,
    DISC_BIG_3,
    DISC_SEEK_0,
    DISC_SEEK_1,
    DISC_SEEK_2,
    DISC_SEEK_3,
    DISC_0,
    DISC_1,
    DISC_2,
    DISC_3,
    WEAPON_PISTOL,
    WEAPON_REVOLVER,
    WEAPON_MACHINE_GUN,
    WEAPON_MINI_GUN,
    WEAPON_SHOT_GUN,
    WEAPON_DISC_GUN,
    WEAPON_KATANA,
    WEAPON_KATANA_ATTACKING,
    WEAPON_BAZOOKA,
    WEAPON_GRENADE_LAUNCHER,
    WEAPON_MINES,
    WEAPON_FLAME_THROWER,
    WEAPON_LASER_RIFLE,
    WEAPON_LASER_RIFLE_MUZZLE,
    BULLET_YELLOW,
    BULLET_YELLOW_BIG,
    BULLET_SHELL,
    BULLET_GRENADE
  }

  static EnumMap<SpriteID, Sprite> spriteCache = new EnumMap<>(SpriteID.class);

  List<SpriteID> runAnimation = List.of(
    SpriteID.PL_RUN_0,
    SpriteID.PL_RUN_1,
    SpriteID.PL_RUN_2,
    SpriteID.PL_RUN_3,
    SpriteID.PL_RUN_4
  );
  List<SpriteID> idleAnimation = List.of(
    SpriteID.PL_IDLE_0,
    SpriteID.PL_IDLE_1,
    SpriteID.PL_IDLE_2,
    SpriteID.PL_IDLE_3,
    SpriteID.PL_IDLE_4
  );
  List<SpriteID> discBigAnimation = List.of(
    SpriteID.DISC_BIG_0,
    SpriteID.DISC_BIG_1,
    SpriteID.DISC_BIG_2,
    SpriteID.DISC_BIG_3
  );
  List<SpriteID> discSeekAnimation = List.of(
    SpriteID.DISC_SEEK_0,
    SpriteID.DISC_SEEK_1,
    SpriteID.DISC_SEEK_2,
    SpriteID.DISC_SEEK_3
  );
  List<SpriteID> discAnimation = List.of(
    SpriteID.DISC_0,
    SpriteID.DISC_1,
    SpriteID.DISC_2,
    SpriteID.DISC_3
  );

  enum SoundID {
    MUSIC_TITLE,
    MUSIC_GAME,
    RESTART,
    JETPACK,
    EXPLOSION,
    BOX,
    BOX_DISC,
    DISC_BOUNCE,
    DEATH,
    WEAPON_FIRE,
    WEAPON_FIRE_REVOLVER,
    WEAPON_FIRE_SHOTGUN,
    WEAPON_FIRE_DISC,
    WEAPON_FIRE_KATANA,
    WEAPON_FIRE_BAZOOKA,
    WEAPON_FIRE_GRENADE,
    WEAPON_FIRE_MINES,
    WEAPON_FIRE_LASER,
    WEAPON_FIRE_FLAMETHROWER,
    DISC_DAMAGE,
    DISC_DEATH,
    DISC_DEATH_SEEKER,
    DISC_DEATH_BIG,
    TYPING
  }

  static EnumMap<SoundID, Sound> sounds = new EnumMap<>(SoundID.class);

  @Override
  public void start() {
    for (int i = 0; i < 5; ++i) {
      spriteCache.put(
        SpriteID.valueOf("PL_RUN_" + i),
        sheet.slice(Vec.ofInt(48 + i * 16, 0), 16, 16)
      );
    }
    for (int i = 0; i < 5; ++i) {
      spriteCache.put(
        SpriteID.valueOf("PL_IDLE_" + i),
        sheet.slice(Vec.ofInt(i * 16, 16), 16, 16)
      );
    }

    for (int i = 0; i < 4; ++i) {
      spriteCache.put(
        SpriteID.valueOf("DISC_BIG_" + i),
        sheet.slice(Vec.ofInt(i * 32, 32), 32, 32)
      );
    }
    for (int i = 0; i < 4; ++i) {
      spriteCache.put(
        SpriteID.valueOf("DISC_" + i),
        sheet.slice(Vec.ofInt(64 + i * 16, 80), 16, 16)
      );
    }
    for (int i = 0; i < 4; ++i) {
      spriteCache.put(
        SpriteID.valueOf("DISC_SEEK_" + i),
        sheet.slice(Vec.ofInt(i * 16, 80), 16, 16)
      );
    }

    spriteCache.put(
      SpriteID.WEAPON_PISTOL,
      sheet.slice(Vec.ofInt(0, 64), 8, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_REVOLVER,
      sheet.slice(Vec.ofInt(48, 72), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_MACHINE_GUN,
      sheet.slice(Vec.ofInt(0, 72), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_MINI_GUN,
      sheet.slice(Vec.ofInt(96, 64), 16, 16)
    );
    spriteCache.put(
      SpriteID.WEAPON_SHOT_GUN,
      sheet.slice(Vec.ofInt(32, 72), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_DISC_GUN,
      sheet.slice(Vec.ofInt(80, 72), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_KATANA,
      sheet.slice(Vec.ofInt(48, 96), 8, 16)
    );
    spriteCache.put(
      SpriteID.WEAPON_KATANA_ATTACKING,
      sheet.slice(Vec.ofInt(56, 96), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_BAZOOKA,
      sheet.slice(Vec.ofInt(16, 72), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_GRENADE_LAUNCHER,
      sheet.slice(Vec.ofInt(112, 72), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_MINES,
      sheet.slice(Vec.ofInt(72, 64), 8, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_FLAME_THROWER,
      sheet.slice(Vec.ofInt(64, 72), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_LASER_RIFLE,
      sheet.slice(Vec.ofInt(72, 96), 16, 8)
    );
    spriteCache.put(
      SpriteID.WEAPON_LASER_RIFLE_MUZZLE,
      sheet.slice(Vec.ofInt(80, 64), 8, 8)
    );

    spriteCache.put(
      SpriteID.BULLET_YELLOW,
      sheet.slice(Vec.ofInt(8, 64), 8, 8)
    );
    spriteCache.put(
      SpriteID.BULLET_YELLOW_BIG,
      sheet.slice(Vec.ofInt(16, 64), 8, 8)
    );
    spriteCache.put(
      SpriteID.BULLET_SHELL,
      sheet.slice(Vec.ofInt(24, 64), 8, 8)
    );
    spriteCache.put(
      SpriteID.BULLET_GRENADE,
      sheet.slice(Vec.ofInt(32, 64), 8, 8)
    );

    for (SoundID sound : SoundID.values()) {
      sounds.put(
        sound,
        Sound.load(sound.name().toLowerCase().replaceAll("_", "-"))
      );
    }

    Graphics.font(font);

    initSplashScreen();
  }

  static final Color[] palette = {
    Color.web("#000000ff"), Color.web("#422136ff"),
    Color.web("#7e2553ff"), Color.web("#742f29ff"),
    Color.web("#ff6e59ff"), Color.web("#5f574fff"),
    Color.web("#c2c3c7ff"), Color.web("#fff1e8ff"),
    Color.web("#be1250ff"), Color.web("#ff9d81ff"),
    Color.web("#ffec27ff"), Color.web("#ab5236ff"),
    Color.web("#29adffff"), Color.web("#83768cff"),
    Color.web("#ff77a8ff"), Color.web("#ffccaaff")
  };

  int lastHighScore = 0;

  enum GameState {
    SPLASH,
    TITLE,
    GAME;
  }

  GameState state = GameState.SPLASH;

  double time() {
    return Entry.instance.time() / 1000_000_000d;
  }

  static class ButtonsManager {

    public boolean up() {
      return Inputs.key(KeyCode.UP) || Inputs.key(KeyCode.I);
    }

    public boolean upOnce()  {
      return Inputs.keyOnce(KeyCode.UP) || Inputs.keyOnce(KeyCode.I);
    }

    public boolean down()  {
      return Inputs.key(KeyCode.DOWN) || Inputs.key(KeyCode.K);
    }

    public boolean downOnce()  {
      return Inputs.keyOnce(KeyCode.DOWN) || Inputs.keyOnce(KeyCode.K);
    }

    public boolean left()  {
      return Inputs.key(KeyCode.LEFT) || Inputs.key(KeyCode.J);
    }

    public boolean leftOnce()  {
      return Inputs.keyOnce(KeyCode.LEFT) || Inputs.keyOnce(KeyCode.J);
    }

    public boolean right()  {
      return Inputs.key(KeyCode.RIGHT) || Inputs.key(KeyCode.L);
    }

    public boolean rightOnce()  {
      return Inputs.keyOnce(KeyCode.RIGHT) || Inputs.keyOnce(KeyCode.L);
    }

    public boolean o()  {
      return Inputs.key(KeyCode.C) || Inputs.key(KeyCode.S);
    }

    public boolean oOnce()  {
      return Inputs.keyOnce(KeyCode.C) || Inputs.keyOnce(KeyCode.S);
    }

    public boolean x()  {
      return Inputs.key(KeyCode.X) || Inputs.key(KeyCode.D);
    }

    public boolean xOnce()  {
      return Inputs.keyOnce(KeyCode.X) || Inputs.keyOnce(KeyCode.D);
    }
  }

  ButtonsManager buttons = new ButtonsManager();

  static boolean alwaysPlusWeapon = false;
  static boolean alwaysPlusDisc = false;

  String version = "0.8";

  Vec camera = Vec.zero();

  static class Player {
    Vec pos = Vec.of(64, 64);
    Vec vel = Vec.zero();

    Sprite spr;
  }

  static Player pl = new Player();

  Vec input = Vec.zero();

  @Builder
  static class Disc {
    @Builder.Default
    final boolean plus = false;
    final int radius;
    final boolean seek;

    Vec pos;
    @Builder.Default
    Vec vel = Vec.zero();

    double health;
    @Builder.Default
    boolean blood = false;

    @Builder.Default
    int hitCooldown = 0;
    int cooldown;

    @Builder.Default
    int frame = (int) Math.floor(Math.random() * 8);

    @Builder.Default
    boolean removed = false;
  }

  static Set<Disc> discs = new HashSet<>();

  @Builder(buildMethodName = "internalBuild")
  static class Bullet implements Lifecycle {

    public static class BulletBuilder {
      public Bullet build() {
        Bullet bullet = this.internalBuild();
        bullet.lastPos = bullet.pos;
        return bullet;
      }
    }


    Vec pos, vel;
    @Builder.Default
    Vec lastPos = null;

    double damage;
    double drag;

    boolean track;

    @Builder.Default
    Sprite sprite = spriteCache.get(SpriteID.BULLET_YELLOW);

    @Builder.Default
    double smokeCooldown = 0;
    @Builder.Default
    double fuse = 0;
    @Builder.Default
    double radius = 0;
    @Builder.Default
    double p = 0, a = 0;

    @Builder.Default
    boolean removed = false;

    void baseTick() {
      // move
      lastPos = pos.clone();
      pos = pos.add(vel);
      vel = vel.multiply(drag);

      // collide with discs
      Disc hit = firstRayHit(lastPos, pos, 2);
      if (hit != null) {
        hitDisc(hit, damage);
        removed = true;
        return;
      }

      // track
      Disc nearest = null;
      double minSqDist = Double.MAX_VALUE;
      if (track) {
        for (Disc d : discs) {
          if (d.cooldown > 0) continue;

          double distSq = d.pos.distSq(pos);
          if (distSq < minSqDist) {
            nearest = d;
            minSqDist = distSq;
          }
        }
      }
      if (nearest != null) {
        double ba = Math.atan2(vel.y, vel.x);
        Vec off = nearest.pos.minus(pos);
        double da = off.angle();
        double angle = Math.min(Math.max(-0.3, da - ba > Math.PI ? da - ba - Math.PI : da - ba), 0.3);
        vel.x += Math.cos(ba + angle) * 0.6;
        vel.y += Math.sin(ba + angle) * 0.6;
        vel.x *= 0.87;
        vel.y *= 0.87;
      }

      // collide with edges
      boolean alive = pos.x >= -2 && pos.x <= 129
        && pos.y >= -2 && pos.y <= 129;
      if (drag < 1) {
        if (Math.abs(vel.x) < 0.1 && Math.abs(vel.y) < 0.1) {
          alive = false;
        }
      }
      if (!alive) removed = true;
    }

    void baseDraw() {
      Graphics.sprite(sprite, pos.minus(4, 4), vel.x < 0);
    }

    Consumer<Bullet> tick, draw;

    @Override
    public void start() {

    }

    @Override
    public void update() {
      tick.accept(this);
    }

    @Override
    public void render() {
      draw.accept(this);
    }
  }

  Set<Bullet> bullets = new HashSet<>();
  Set<Bullet> bulletsAdding = new HashSet<>();

  double boxCooldown = 1;
  Vec weaponGrab = Vec.zero();

  double frame = 0;

  Vec box = Vec.of(
    Math.random() * 92 + 16,
    Math.random() * 32 + 16
  );

  Sprite boxSprite = sheet.slice(Vec.ofInt(40, 8), 8, 8);

  double spawnCooldown = 0;
  double laserCooldown = 0;

  boolean dead = false;
  int deadCooldown = 0;
  int restartCooldown = 0;

  boolean newHighScore = false;
  boolean showingBest = false;
  static int score = 0;

  @Builder(buildMethodName = "internalBuild")
  static class Particle implements Lifecycle {

    public static class ParticleBuilder {
      public Particle build() {
        Particle particle = this.internalBuild();
        particle.lastPos = particle.pos;
        return particle;
      }
    }

    Vec pos, vel;
    Vec lastPos;

    double cooldown;
    double a;
    Color c;

    @Builder.Default
    boolean removed = false;

    Consumer<Particle> tick, draw;

    @Override
    public void start() {

    }

    @Override
    public void update() {
      tick.accept(this);
    }

    @Override
    public void render() {
      draw.accept(this);
    }
  }

  static Set<Particle> particles = new HashSet<>();

  double shake = 0;

  static boolean flipX = false;

  enum WeaponID {
    PISTOL,
    REVOLVER,
    MACHINE_GUN,
    MINI_GUN,
    DUAL_PISTOL,
    SHOT_GUN,
    DISC_GUN,
    KATANA,
    BAZOOKA,
    GRENADE_LAUNCHER,
    MINES,
    FLAME_THROWER,
    LASER_RIFLE
  }

  static boolean weaponPlusByChance() {
    double plusRand = Math.random();
    return alwaysPlusWeapon || (
      score >= 10 && ((
        score < 50 &&
          plusRand < 0.05 + (score - 10) * 0.005
      ) || plusRand < 0.25)
    );
  }

  @Builder
  static class Weapon implements Lifecycle {
    final WeaponID id;
    final String name;
    Sprite sprite;
    @Builder.Default
    Vec.Int spriteOffset = Vec.zero().toInt();
    @Builder.Default
    final boolean plus = weaponPlusByChance();
    @Builder.Default
    final boolean autoFire = false;
    final int firePeriod;

    @Builder.Default
    int cooldown = 0;

    void baseDraw() {
      double drawX = pl.pos.x;
      double drawY = pl.pos.y - 4 + spriteOffset.y;
      if (flipX) {
        drawX -= 4 + 8 + spriteOffset.x + 8 * (sprite.getWidth() / 8d - 1);
      }
      else {
        drawX += 4 + spriteOffset.x;
      }
      Graphics.sprite(
        sprite,
        Vec.of(drawX, drawY),
        flipX
      );
    }

    @Builder.Default
    final Consumer<Weapon> tick = _ -> {}, draw = _ -> {}, fire = _ -> {};

    @Override
    public void start() {}

    @Override
    public void update() {
      tick.accept(this);
    }

    @Override
    public void render() {
      draw.accept(this);
    }
  }

  List<Supplier<Weapon>> weapons = List.of(
    this::getRevolver,
    this::getMachineGun,
    this::getMiniGun,
    this::getDualPistol,
    this::getShotGun,
    this::getDiscGun,
    this::getKatana,
    this::getBazooka,
    this::getGrenadeLauncher,
    this::getMines,
    this::getFlameThrower,
    this::getLaserRifle
  );
  Weapon currentWeapon = getPistol();
  double weaponCooldown = 0;

  void restart() {
    Audios.emit(sounds.get(SoundID.RESTART));

    pl.pos = Vec.of(64, 64);
    pl.vel = Vec.zero();
    pl.spr = spriteCache.get(SpriteID.PL_RUN_0);

    input = Vec.zero();

    discs.clear();
    bullets.clear();

    // weapons = List.of(...);
    currentWeapon = getPistol();

    weaponCooldown = 0;
    boxCooldown = 1;
    weaponGrab = Vec.zero();

    frame = 0;

    box = Vec.of(
      Math.random() * 92 + 16,
      Math.random() * 32 + 16
    );

    spawnCooldown = 60;
    laserCooldown = 0;

    boxParticles();

    dead = false;
    deadCooldown = 0;
    restartCooldown = 0;

    newHighScore = false;
    showingBest = false;
    score = 0;

    particles.clear();

    shake = 0;

    flipX = false;
  }

  @Override
  public void update() {
    switch (state) {
      case SPLASH -> {
        updateSplashScreen();
      }
      case TITLE -> {
        updateTitleScreen();
      }
      case GAME -> {
        updateGame();
      }
    }
  }

  void initGame() {
    Audios.music(sounds.get(SoundID.MUSIC_GAME));
    restart();
  }

  void addScore() {
    score++;
    if (score > lastHighScore) {
      newHighScore = true;
      lastHighScore = score;
    }
  }

  void shake(double value) {
    shake = Math.max(shake, value);
  }

  void updateGame() {
    if (Inputs.keyOnce(KeyCode.M)) alwaysPlusWeapon = !alwaysPlusWeapon;
    if (Inputs.keyOnce(KeyCode.N)) alwaysPlusDisc = !alwaysPlusDisc;
    if (Inputs.keyRepeat(KeyCode.B)) {
      addScore();
    }

    if (dead) {
      deadCooldown--;
      if (deadCooldown < -120) {
        deadCooldown = 30;
        showingBest = !showingBest;
      }
      restartCooldown = Math.max(restartCooldown - 1, 0);

      // restart input
      if (deadCooldown <= 0 && (buttons.oOnce() || buttons.xOnce())) {
        restart();
        return;
      }
    }
    else {
      input = Vec.zero();

      if (buttons.leftOnce()) {
        flipX = true;
      }
      if (buttons.rightOnce()) {
        flipX = false;
      }

      if (buttons.left()) {
        input.x--;
      }
      if (buttons.right()) {
        input.x++;
      }
      if (buttons.up()) {
        input.y--;
      }
      if (buttons.down()) {
        input.y++;
      }

      if (buttons.upOnce()) {
        Audios.emit(sounds.get(SoundID.JETPACK));
      }

      if (input.lenSq() > 0) {
        // chording
        input.x *= 0.7071;
        input.y *= 0.7071;
      }

      // accelerate
      pl.vel = pl.vel.add(input);
      pl.vel.y += 0.25;

      // cap speed
      if (input.len() == 0) {
        if (pl.vel.lenSq() <= 0.1 * 0.1) {
          pl.vel = Vec.zero();
        }
        else {
          double speed = pl.vel.len();
          pl.vel = pl.vel.multiply((speed - 0.1) / speed);
        }
      }
      if (pl.vel.lenSq() > 2 * 2) {
        pl.vel = pl.vel.resize(2);
      }

      // step
      pl.pos = pl.pos.add(pl.vel);

      // walls bounce
      if (pl.pos.x < 0) {
        pl.pos.x = 0;
        pl.vel.x = Math.abs(pl.vel.x);
      }
      if (pl.pos.x > 128) {
        pl.pos.x = 128;
        pl.vel.x = -Math.abs(pl.vel.x);
      }
      if (pl.pos.y < 0) {
        pl.pos.y = 0;
        pl.vel.y = Math.abs(pl.vel.y);
      }
      if (pl.pos.y > 128) {
        pl.pos.y = 128;
        pl.vel.y = -Math.abs(pl.vel.y);
      }

      if (input.lenSq() > 1e-6) {
        // animate
        pl.spr = spriteCache.get(
          runAnimation.get((int) Math.floor(frame / 3d) % runAnimation.size())
        );
      } else {
        pl.spr = spriteCache.get(
          idleAnimation.get((int) Math.floor(frame / 4d) % idleAnimation.size())
        );
      }

      // smoke
      if (input.y < 0) {
        Particle spark = oldSpark(
          Vec.of(pl.pos.x - 1, pl.pos.y + 2),
          5 + Math.floor(Math.random() * 5),
          palette[7]
        );
        if (flipX) {
          spark.pos.x += 5;
        } else {
          spark.pos.x -= 5;
        }

        particles.add(spark);
      }

      // laser autoFire
      if (laserCooldown > 0) {
        laserCooldown--;

        if (laserCooldown <= 0) {
          // fire!
          double xStart = pl.pos.x, xEnd = 0;
          if (flipX) {
            xStart -= 4;
            xEnd = 0;
          } else {
            xStart += 4;
            xEnd = 128;
          }
          bullets.add(makeLaser(Vec.of(xStart, pl.pos.y), Vec.of(xEnd, pl.pos.y), currentWeapon.plus));
          shake(3);
        }
      }
    }

    // particle update && finish
    particles.forEach(Particle::update);
    particles.removeIf(p -> p.removed);

    // check for box hit
    weaponCooldown = Math.max(weaponCooldown - 0.015, 0);
    boxCooldown = Math.max(boxCooldown - 0.05, 0);
    if (Math.abs(pl.pos.x - box.x) < 8 && Math.abs(pl.pos.y - box.y) < 8) {
      boxParticles(); // exit particles
      double boxA = Math.random();
      box = Vec.of(
        (Math.sin(boxA) * 60 + box.x - 4) % 120 + 4,
        (Math.cos(boxA) * 60 + box.y - 4) % 120 + 4
      );
      boxParticles(); // entry particles
      addScore();

      // new weapon!
      List<Weapon> options = new ArrayList<>();
      for (Supplier<Weapon> supplier : weapons) {
        Weapon weapon = supplier.get();
        if (weapon.id != currentWeapon.id && (score > 10 || weapon.id != WeaponID.DISC_GUN)) {
          options.add(weapon);
        }
      }
      currentWeapon = options.get((int) (Math.random() * options.size()));
      if (currentWeapon.id == WeaponID.DISC_GUN) {
        Audios.emit(sounds.get(SoundID.BOX_DISC));
      } else {
        Audios.emit(sounds.get(SoundID.BOX));
      }
      weaponGrab = pl.pos.clone();
      weaponCooldown = 1;
      boxCooldown = 1;
    }

    discs.removeIf(d -> d.removed);

    // move discs
    for (Disc d : discs) {
      // cool down || move
      if (d.hitCooldown > 0) {
        d.hitCooldown--;
      }
      if (d.cooldown > 0) {
        d.cooldown -= d.plus ? 2 : 1;
      } else {
        // seeking!
        if (d.seek) {
          d.vel = d.vel.multiply(d.plus ? 0.92 : 0.99);
          if (!d.pos.inRange(pl.pos, 5)) {
            Vec sd = pl.pos.minus(d.pos);
            double sdlen = sd.len();
            d.vel = d.vel.add(sd.multiply((d.plus ? 0.08 : 0.01) / sdlen));
          }
        }
        d.pos = d.pos.add(d.vel);
        boolean hit = false;
        if (d.pos.x < 0) {
          d.pos.x = 0;
          d.vel.x = Math.abs(d.vel.x);
          hit = true;
        }
        if (d.pos.x > 128) {
          d.pos.x = 128;
          d.vel.x = -Math.abs(d.vel.x);
          hit = true;
        }
        if (d.pos.y < 0) {
          d.pos.y = 0;
          d.vel.y = Math.abs(d.vel.y);
          hit = true;
        }
        if (d.pos.y > 128) {
          d.pos.y = 128;
          d.vel.y = -Math.abs(d.vel.y);
          hit = true;
        }
        if (hit && d.radius > 10) {
          shake(4);
          Audios.emit(sounds.get(SoundID.DISC_BOUNCE));
        }
        // check for player collision
        if (!dead) {
          if (pl.pos.inRange(d.pos, d.radius)) {
            dead = true;
            deadCooldown = 60;
            restartCooldown = 60;
            Audios.emit(sounds.get(SoundID.DEATH));
            d.blood = true;
            for (int i = 0; i < 5; ++i) {
              addSparks(d, palette[2], 10, 0, 1);
              addSparks(d, palette[2], 20, 0, 1);
              addSparks(d, palette[2], 30, 0, 1);
              addSparks(d, palette[8], 40, 0, 1);
              addSparks(d, palette[8], 50, 0, 1);
            }
            shake = 15;
          }
        }
      }
      // spin regardless of movement
      d.frame = (d.frame + 1) % 8;
    }

    // spawn discs
    if (!dead) {
      spawnCooldown--;
      if (spawnCooldown <= 0) {
        if (score >= 10 || discs.size() < Math.max(score, 1)) {
          Vec spawn = Vec.of(
            Math.random() * 128,
            Math.random() * 128
          );

          double plusRand = Math.random();
          boolean plus = alwaysPlusDisc || (
            score >= 10 && (
              (score < 50 && plusRand < 0.05 + (score - 10) * 0.005)
                || plusRand < 0.25
            )
          );

          if (Math.random() < 0.5) {
            // three discs
            double angle = Math.random() * 2 * Math.PI, speed = 1.5;
            makeDisc(7, 20, false, 60, spawn, Vec.ofPolar(angle, speed), plus);
            makeDisc(7, 20, false, 60 + 15 * (plus ? 2 : 1), spawn, Vec.ofPolar(angle, speed), plus);
            makeDisc(7, 20, false, 60 + 15 * (plus ? 4 : 2), spawn, Vec.ofPolar(angle, speed), plus);
          }
          else {
            if (Math.random() < 0.5) {
              // big disc
              makeDisc(13, 100, false, 60, spawn, Vec.ofPolar(Math.random() * 2 * Math.PI, 0.75), plus);
            }
            else {
              // seeker
              makeDisc(7, 20, true, 60, spawn, Vec.zero(), plus);
            }
          }
        }

        spawnCooldown = 180 / (1 + Math.sqrt(score) * 0.1);
      }

      frame++;
    }

    // fire weapon
    if (currentWeapon.cooldown > 0) {
      // cooldown, don't fire
      currentWeapon.cooldown--;
    }
    else {
      // fire?
      if (!dead) {
        boolean fire = false;
        if (currentWeapon.autoFire) {
          fire = buttons.x() || buttons.o();
        }
        else {
          fire = buttons.xOnce() || buttons.oOnce();
        }

        if (fire) {
          currentWeapon.fire.accept(currentWeapon);
          currentWeapon.cooldown = currentWeapon.firePeriod;
        }
      }
    }

    if (!dead) {
      currentWeapon.update();
    }

    // move bullets
    for (Bullet b : bullets) {
      b.update();
    }
    bullets.removeIf(b -> b.removed);

    bullets.addAll(bulletsAdding);
    bulletsAdding.clear();

    // camera motion
    camera = camera.multiply(0.9)
      .add(pl.pos.minus(Vec.of(64, 64)).multiply(0.0375));

    if (shake > 0) {
      double angle = Math.random() * 2 * Math.PI;
      camera = camera.add(Vec.ofPolar(angle, shake / 2));
      shake--;
    }
  }

  void makeDisc(int radius, int health, boolean seek, int cooldown, Vec pos, Vec vel, boolean plus) {
    Disc disc = Disc.builder()
      .pos(pos)
      .vel(vel)
      .radius(radius)
      .seek(seek)
      .health(health)
      .cooldown(cooldown)
      .plus(plus)
      .build();
    discs.add(disc);
  }

  @Override
  public void render() {
    switch (state) {
      case SPLASH -> {
        drawSplashScreen();
      }
      case TITLE -> {
        drawTitleScreen();
      }
      case GAME -> {
        drawGame();
      }
    }
  }

  void drawGame() {
    Graphics.clear(palette[12]);
    Graphics.camera(camera);

    // background tiles
    Graphics.tiles(
      gameTiles,
      Vec.of(-64, -64),
      Set.of(0)
    );

    // box strings
    Graphics.line(box.add(-3, 0), Vec.of(box.x - 3, 0), palette[12]);
    Graphics.line(box.add(-2, 0), Vec.of(box.x - 2, 0), palette[13]);
    Graphics.line(box.add(-1, 0), Vec.of(box.x - 1, 0), palette[12]);

    Graphics.line(box.add(0, 0), Vec.of(box.x + 0, 0), palette[12]);
    Graphics.line(box.add(1, 0), Vec.of(box.x + 1, 0), palette[13]);
    Graphics.line(box.add(2, 0), Vec.of(box.x + 2, 0), palette[12]);

    // background discs
    for (Disc d : discs) {
      if (d.cooldown > 0) {
        Vec r = Vec.of (
          Math.floor(d.pos.x - 0.5),
          Math.floor(d.pos.y - 0.5)
        );
        Color col = palette[d.plus && frame % 32 < 16 ? 11 : 13];
        Graphics.circle(r.add(0, 0), d.cooldown * 0.5 + d.radius, col);
        Graphics.circle(r.add(1, 0), d.cooldown * 0.5 + d.radius, col);
        Graphics.circle(r.add(0, 1), d.cooldown * 0.5 + d.radius, col);
        Graphics.circle(r.add(1, 1), d.cooldown * 0.5 + d.radius, col);
      }
    }
    drawDiscs(true);

    // foreground tiles
    Graphics.tiles(
      gameTiles,
      Vec.of(-64, -64),
      Set.of(1)
    );

    // player
    if (!dead) {
      // body
      Graphics.sprite(pl.spr, pl.pos.minus(8, 8), flipX);

      // weapon
      if (currentWeapon.plus && frame % 12 < 5) {
        Graphics.map(palette[7], palette[15]);
        Graphics.map(palette[6], palette[4]);
        Graphics.map(palette[5], palette[11]);
        Graphics.map(palette[0], palette[3]);
      }

      currentWeapon.draw.accept(currentWeapon);

      Graphics.map(palette[7], palette[7]);
      Graphics.map(palette[6], palette[6]);
      Graphics.map(palette[5], palette[5]);
      Graphics.map(palette[0], palette[0]);
    } else {
      Graphics.fillCircle(pl.pos, 10, palette[2]);
    }

    // foreground discs
    drawDiscs(false);

    // box
    Graphics.sprite(
      boxSprite,
      Vec.of(
        box.x - 4,
        box.y - 4 + boxCooldown * Math.sin(boxCooldown * -2 * Math.PI) * -8
      )
    );

    // bullets
    for (Bullet b : bullets) {
      b.render();
    }

    // particles
    for (Particle p : particles) {
      p.render();
    }

    // hud
    if (weaponCooldown > 0) {
      double lift = ((1 - weaponCooldown) * 0.0 + 1.0 * (1 - weaponCooldown) * (1 - weaponCooldown)) * -128;
      shadowPrint(
        currentWeapon.name + (currentWeapon.plus ? "++" : ""),
        Vec.of(
          weaponGrab.x - currentWeapon.name.length() * 2,
          weaponGrab.y - 16 + lift
        ),
        palette[(currentWeapon.plus && frame % 12 < 4) ? 4 : 7]
      );
    }

    Graphics.camera();

    if (!dead) {
      scorePrint(score, Vec.of(64, 2), 2, newHighScore, true);
    }
    else {
      // background
      Graphics.fillRect(Vec.of(0, 32), Vec.of(128, 98), palette[1]);

      // game over
      double write = Math.min(60 - deadCooldown, 30) * 2;
      Graphics.map(palette[7], palette[0]);
      Graphics.easeSprite(
        sheet.slice(Vec.ofInt(1, 122), (int) write, 5),
        Vec.of(64 - write, 32 + 8 + 2),
        write * 2, 10
      );
      Graphics.map(palette[7], palette[7]);
      Graphics.easeSprite(
        sheet.slice(Vec.ofInt(1, 122), (int) write, 5),
        Vec.of(64 - write, 32 + 8),
        write * 2, 10
      );

      // boxEnds:/best:
      int[] box = new int[]{33, 116, 37, 5};
      int num = score;
      boolean exclaim = newHighScore;
      if (showingBest) {
        box = new int[]{1, 116, 30, 5};
        num = lastHighScore;
        exclaim = false;
      }
      String numStr = String.valueOf(num);
      int width = box[2] * 2 + 4 + numStr.length() * 7 * 2 + 2;
      if (exclaim) {
        width += 7 * 2;
      }
      write = Math.min(Math.max(30 - deadCooldown, 0), 30) * width / 30d;
      int y = 32 + 8 + 10 + 6;
      Graphics.map(palette[7], palette[0]);
      Graphics.easeSprite(
        sheet.slice(Vec.ofInt(box[0], box[1]), box[2], box[3]),
        Vec.of(
          64 - write * 0.5,
          y + 2
        ),
        box[2] * 2,
        10
      );
      Graphics.map(palette[7], palette[7]);
      Graphics.easeSprite(
        sheet.slice(Vec.ofInt(box[0], box[1]), box[2], box[3]),
        Vec.of(64 - write * 0.5, y),
        box[2] * 2, 10
      );
      scorePrint(num, Vec.of(64 - write * 0.5 + box[2] * 2 + 22, y), 2, exclaim, false);
      Graphics.fillRect(
        Vec.of(64 + write * 0.5, y),
        Vec.of(y + 128, y + 12),
        palette[1]
      ); // hiding rect
      String message = "DON'T TOUCH THE DISCS!";
      if (newHighScore) {
        message = "NEW HIGH SCORE!";
      }
      else if (score >= 10) {
        message = "ONLY " + (lastHighScore - score + 1) + " TO NEW HIGHSCORE!";
      }
      shadowPrintCentered(message, Vec.of(64, 32 + 12 + 20 + 12), palette[7]);
      shadowPrintCentered("PRESS C OR X TO RESTART", Vec.of(64, 32 + 12 + 20 + 12 + 8), palette[7]);
    }
  }

  void drawDiscs(boolean background) {
    for (Disc d : discs) {
      if ((d.cooldown > 0 && background) || (d.cooldown <= 0 && !background)) {
        if (d.cooldown > 0) {
          Graphics.map(palette[0], d.plus && frame % 24 < 18 ? palette[11] : palette[13]);
          Graphics.map(palette[1], palette[12]);
          Graphics.map(palette[7], palette[12]);
          Graphics.map(palette[6], palette[12]);
          Graphics.map(palette[5], palette[12]);
        }
        else if (d.blood || d.hitCooldown > 0) {
          Graphics.map(palette[7], palette[14]);
          Graphics.map(palette[6], palette[8]);
          Graphics.map(palette[5], palette[2]);
        }
        else if (d.plus && frame % 24 < 18) {
          Graphics.map(palette[7], palette[15]);
          Graphics.map(palette[6], palette[4]);
          Graphics.map(palette[5], palette[11]);
        }
        if (!d.seek) {
          if (d.radius > 10) {
            Graphics.sprite(
              spriteCache.get(discBigAnimation.get(d.frame / 2)),
              Vec.of(d.pos.x - 16, d.pos.y - 16),
              d.vel.x < 0
            );
          }
          else {
            Graphics.sprite(
              spriteCache.get(discSeekAnimation.get(d.frame / 2)),
              Vec.of(d.pos.x - 8, d.pos.y - 8),
              d.vel.x < 0
            );
          }
        }
        else {
          Graphics.sprite(
            spriteCache.get(discAnimation.get(d.frame / 2)),
            Vec.of(d.pos.x - 8, d.pos.y - 8),
            d.vel.x < 0
          );
        }
        Graphics.map(palette[0], palette[0]);
        Graphics.map(palette[1], palette[1]);
        Graphics.map(palette[7], palette[7]);
        Graphics.map(palette[6], palette[6]);
        Graphics.map(palette[5], palette[5]);
      }
    }
  }

  void prettyPrint(String str, Vec v, Color color) {
    Graphics.print(str, v.add(-1, -1), palette[0]);
    Graphics.print(str, v.add(+0, -1), palette[0]);
    Graphics.print(str, v.add(+1, -1), palette[0]);
    Graphics.print(str, v.add(-1, +0), palette[0]);
    Graphics.print(str, v.add(+1, +0), palette[0]);
    Graphics.print(str, v.add(-1, +1), palette[0]);
    Graphics.print(str, v.add(+0, +1), palette[0]);
    Graphics.print(str, v.add(+1, +1), palette[0]);
    Graphics.print(str, v, color);
  }

  void shadowPrint(String str, Vec v, Color color) {
    Graphics.print(str, v.add(0, 1), palette[0]);
    Graphics.print(str, v, color);
  }

  void shadowPrintCentered(String str, Vec v, Color color) {
    shadowPrint(str, v.add(str.length() * -2, 0), color);
  }

  void scorePrint(int num, Vec v, double scale, boolean exclaim, boolean center) {
    String chars = String.valueOf(num);
    if (exclaim) {
      chars += "!";
    }

    for (int i = 0; i < chars.length(); ++i) {
      char ch = chars.charAt(i);
      int sx = (
        ch == '!'
          ? 10
          : ch - '0'
      ) * 4 + 1;
      double startX = v.x + 7 * (i - 1) * scale;
      if (center) {
        startX = v.x - chars.length() * 3.5 * scale + 7 * i * scale;
      }
      Graphics.map(palette[7], palette[0]);
      Graphics.easeSprite(
        sheet.slice(Vec.ofInt(sx, 97), 3, 5),
        Vec.of(startX, v.y + 2),
        6 * scale, 5 * scale
      );
      Graphics.map(palette[7], palette[7]);
      Graphics.easeSprite(
        sheet.slice(Vec.ofInt(sx, 97), 3, 5),
        Vec.of(startX, v.y),
        6 * scale, 5 * scale
      );
    }
  }

  void knockback(double dx) {
    if (flipX) {
      pl.vel.x += dx;
    }
    else {
      pl.vel.x -= dx;
    }
  }

  Weapon getPistol() {
    return Weapon.builder()
      .plus(false)
      .id(WeaponID.PISTOL)
      .name("PISTOL")
      .sprite(spriteCache.get(SpriteID.WEAPON_PISTOL))
      .spriteOffset(Vec.ofInt(-1, 0))
      .firePeriod(6)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        Vec b = Vec.of(7, 0);
        Vec v = Vec.of(4, 0);
        if (flipX) {
          b.x *= -1;
          v.x *= -1;
        }
        bullets.add(makeBullet(pl.pos.add(b), v, 10));
        shake(2);
        knockback(1);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE));
      })
      .build();
  }

  Weapon getRevolver() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.REVOLVER)
      .name("REVOLVER")
      .sprite(spriteCache.get(SpriteID.WEAPON_REVOLVER))
      .autoFire(plus)
      .firePeriod(plus ? 12 : 6)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        Vec b = Vec.of(7, 0);
        Vec v = Vec.of(4, 0);
        if (flipX) {
          b.x *= -1;
          v.x *= -1;
        }
        Bullet bullet = makeBullet(pl.pos.add(b), v, 50, plus);
        bullet.sprite = spriteCache.get(SpriteID.BULLET_YELLOW_BIG);
        bullets.add(bullet);
        shake(5);
        knockback(1);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE_REVOLVER));
      })
      .build();
  }

  Weapon getMachineGun() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.MACHINE_GUN)
      .name("MACHINEGUN")
      .sprite(spriteCache.get(SpriteID.WEAPON_MACHINE_GUN))
      .spriteOffset(Vec.ofInt(-4, 0))
      .autoFire(true)
      .firePeriod(plus ? 3 : 4)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        Vec b = Vec.of(7, 0);
        Vec v = Vec.of(4, 0);
        if (flipX) {
          b.x *= -1;
          v.x *= -1;
        }
        for (int py = (plus ? -5 : 0); py <= (plus ? 5 : 0); py += 5) {
          bullets.add(makeBullet(pl.pos.add(b).add(0, py), v, plus ? 20 : 10));
        }
        shake(2);
        knockback(1);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE));
      })
      .build();
  }

  Weapon getMiniGun() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.MINI_GUN)
      .name("MINIGUN")
      .sprite(spriteCache.get(SpriteID.WEAPON_MINI_GUN))
      .spriteOffset(Vec.ofInt(-3, -8))
      .autoFire(true)
      .firePeriod(plus ? 0 : 1)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        for (int i = 0; i < (plus ? 6 : 3); ++i) {
          Vec b = Vec.of(11, 0);
          Vec v = Vec.of(
            8,
            (Math.random() * 2 - 1) * (plus ? 8 : 4)
          );
          if (flipX) {
            b.x *= -1;
            v.x *= -1;
          }
          bullets.add(makeBullet(pl.pos.add(b), v, plus ? 50 : 5));
        }
        shake(5);
        knockback(2);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE));
      })
      .build();
  }

  Weapon getDualPistol() {
    boolean plus = weaponPlusByChance();
    Sprite sprite = spriteCache.get(SpriteID.WEAPON_PISTOL);

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.DUAL_PISTOL)
      .name("DUAL PISTOL")
      .sprite(sprite)
      .spriteOffset(Vec.ofInt(-1, 0))
      .autoFire(plus)
      .firePeriod(6)
      .draw(_ -> {
        double y = pl.pos.y - 4;
        Graphics.sprite(sprite, Vec.of(pl.pos.x + 3, y), false);
        Graphics.sprite(sprite, Vec.of(pl.pos.x - 11, y), true);
      })
      .fire(_ -> {
        Vec b = Vec.of(7, 0);
        Vec v = Vec.of(4, 0);
        bullets.add(makeBullet(pl.pos.add(b), v, 10));
        bullets.add(makeBullet(pl.pos.add(b.mirror()), v.mirror(), 10));
        if (plus) {
          bullets.add(makeBullet(pl.pos.add(b), Vec.of(v.y, v.x), 10));
          bullets.add(makeBullet(pl.pos.add(b), Vec.of(v.y, -v.x), 10));
          bullets.add(makeBullet(pl.pos.add(b.mirror()), Vec.of(-v.y, v.x), 10));
          bullets.add(makeBullet(pl.pos.add(b.mirror()), Vec.of(-v.y, -v.x), 10));
        }
        shake(2);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE));
      })
      .build();
  }

  Weapon getShotGun() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.SHOT_GUN)
      .name("SHOTGUN")
      .sprite(spriteCache.get(SpriteID.WEAPON_SHOT_GUN))
      .spriteOffset(Vec.ofInt(-5, 1))
      .autoFire(plus)
      .firePeriod(plus ? 10 : 30)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        for (int i = 0; i < (plus ? 15 : 9); ++i) {
          Vec b = Vec.of(7, 0);
          Vec v = Vec.of(
            2 + Math.random() * (plus ? 48 : 12),
            (Math.random() * 2 - 1) * (plus ? 4 : 2)
          );
          if (flipX) {
            b.x *= -1;
            v.x *= -1;
          }
          Bullet bullet = makeBullet(pl.pos.add(b), v, 10);
          bullet.drag = 0.7;
          bullets.add(bullet);
        }
        shake(3);
        knockback(2);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE_SHOTGUN));
      })
      .build();
  }

  Weapon getDiscGun() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.DISC_GUN)
      .name("DISC GUN?!")
      .sprite(spriteCache.get(SpriteID.WEAPON_DISC_GUN))
      .spriteOffset(Vec.ofInt(-2, 0))
      .firePeriod(6)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        Vec b = Vec.of(7, 0);
        Vec v = Vec.of(
          3,
          plus ? Math.random() * 2 - 1 : 0
        );
        if (flipX) {
          b.x *= -1;
          v.x *= -1;
        }
        makeDisc(
          7,
          20,
          false,
          0,
          pl.pos.add(b),
          v,
          plus
        );
        shake(2);
        knockback(5);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE_DISC));
      })
      .build();
  }

  Weapon getKatana() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.KATANA)
      .name("KATANA")
      .sprite(spriteCache.get(SpriteID.WEAPON_KATANA))
      .spriteOffset(Vec.ofInt(-4, -1))
      .firePeriod(plus ? 3 : 12)
      .draw(Weapon::baseDraw)
      .tick(w -> {
        if (w.cooldown == 0) {
          w.sprite = spriteCache.get(SpriteID.WEAPON_KATANA);
          w.spriteOffset = Vec.ofInt(-4, -1);
        } else {
          w.sprite = spriteCache.get(SpriteID.WEAPON_KATANA_ATTACKING);
          w.spriteOffset = Vec.ofInt(3, 0);
        }
      })
      .fire(_ -> {
        double bx = 16;
        if (flipX) {
          bx = -bx;
        }
        for (Disc d : discs) {
          if (d.cooldown <= 0) {
            Vec delta = Vec.of(
              Math.max(Math.abs(pl.pos.x + bx - d.pos.x) - 16, 0),
              pl.pos.y - d.pos.y
            );
            if (delta.inRange((d.radius + 8) * (plus ? 2 : 1))) {
              // hit compressed space
              hitDisc(d, 50);

              if (flipX) {
                d.vel.x = -Math.abs(d.vel.x);
              } else {
                d.vel.x = Math.abs(d.vel.x);
              }
            }
          }
        }
        shake(2);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE_KATANA));
      })
      .build();
  }

  Weapon getBazooka() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.BAZOOKA)
      .name("BAZOOKA")
      .sprite(spriteCache.get(SpriteID.WEAPON_BAZOOKA))
      .spriteOffset(Vec.ofInt(-6, 0))
      .autoFire(plus)
      .firePeriod(plus ? 20 : 60)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        double bx = 9, vx = 1;
        if (flipX) {
          bx *= -1;
          vx *= -1;
        }
        bullets.add(makeShell(pl.pos.add(bx, 0), Vec.of(vx, 0), plus));
        shake(3);
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE_BAZOOKA));
      })
      .build();
  }

  Weapon getGrenadeLauncher() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.GRENADE_LAUNCHER)
      .name("GRENADE LAUNCHER")
      .sprite(spriteCache.get(SpriteID.WEAPON_GRENADE_LAUNCHER))
      .spriteOffset(Vec.ofInt(-4, 0))
      .autoFire(plus)
      .firePeriod(plus ? 20 : 60)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        double bx = 7, vx = 3;
        if (flipX) {
          bx *= -1;
          vx *= -1;
        }
        bullets.add(makeGrenade(pl.pos.add(bx, 0), Vec.of(vx, -2), plus));
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE_GRENADE));
      })
      .build();
  }

  Weapon getMines() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.MINES)
      .name("MINES")
      .sprite(spriteCache.get(SpriteID.WEAPON_MINES))
      .firePeriod(plus ? 20 : 60)
      .draw(w -> {
        Graphics.map(palette[8], palette[0]);
        Graphics.sprite(w.sprite, pl.pos.add(-4, 0));
        Graphics.map(palette[8], palette[8]);
      })
      .fire(_ -> {
        bullets.add(makeMine(pl.pos.add(0, 4), plus));
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE_MINES));
      })
      .build();
  }

  Weapon getFlameThrower() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.FLAME_THROWER)
      .name("FLAMETHROWER")
      .sprite(spriteCache.get(SpriteID.WEAPON_FLAME_THROWER))
      .spriteOffset(Vec.ofInt(-2, 0))
      .autoFire(true)
      .firePeriod(0)
      .draw(Weapon::baseDraw)
      .fire(_ -> {
        Vec b = Vec.of(7, 0);
        Vec v = Vec.of(
          6 + Math.random() * 3 - 1.5,
          Math.random() * -2
        );
        if (flipX) {
          b.x *= -1;
          v.x *= -1;
        }
        bullets.add(makeFlame(pl.pos.add(b), v, plus));
        if (frame % 2 == 0) {
          Audios.emit(sounds.get(SoundID.WEAPON_FIRE_FLAMETHROWER));
        }
      })
      .build();
  }

  Weapon getLaserRifle() {
    boolean plus = weaponPlusByChance();

    return Weapon.builder()
      .plus(plus)
      .id(WeaponID.LASER_RIFLE)
      .name("LASER RIFLE")
      .sprite(spriteCache.get(SpriteID.WEAPON_LASER_RIFLE))
      .spriteOffset(Vec.ofInt(-3, 0))
      .autoFire(plus)
      .firePeriod(plus ? 12 : 60)
      .draw(Weapon::baseDraw)
      .tick(w -> {
        if (w.cooldown > (plus ? 4 : 30)) {
          // collide with discs
          double hitX = pl.pos.x;
          if (flipX) {
            hitX -= 8;
          }
          else {
            hitX += 8;
          }

          for (Disc d : discs) {
            if (d.cooldown <= 0) {
              Vec v = Vec.of(
                Math.max(Math.abs(d.pos.x - hitX) - 4, 0),
                pl.pos.y - d.pos.y
              );
              if (v.inRange(d.radius + 3)) {
                // hit compressed space
                hitDisc(d, 100);
              }
            }
          }
        }
      })
      .draw(w -> {
        if (w.cooldown > (w.plus ? 8 : 30)) {
          w.sprite = spriteCache.get(SpriteID.WEAPON_LASER_RIFLE_MUZZLE);
          w.spriteOffset.x = 5;
          w.baseDraw();
        }
        w.sprite = spriteCache.get(SpriteID.WEAPON_LASER_RIFLE);
        w.spriteOffset.x = -3;
        w.baseDraw();
      })
      .fire(w -> {
        Audios.emit(sounds.get(SoundID.WEAPON_FIRE_LASER));
        laserCooldown = w.plus ? 6 : 30;
      })
      .build();
  }

  Bullet makeBullet(Vec pos, Vec vel, int damage, boolean track) {
    return Bullet.builder()
      .pos(pos)
      .vel(vel)
      .drag(1)
      .damage(damage)
      .track(track)
      .tick(Bullet::baseTick)
      .draw(Bullet::baseDraw)
      .build();
  }

  Bullet makeBullet(Vec pos, Vec vel, int damage) {
    return makeBullet(pos, vel, damage, false);
  }

  Bullet makeShell(Vec pos, Vec vel, boolean plus) {
    Bullet shell = makeBullet(pos, vel, 0);

    shell.sprite = spriteCache.get(SpriteID.BULLET_SHELL);

    shell.tick = s -> {
      // regular bulupdate
      s.baseTick();

      // now speed up
      s.vel.x += (s.vel.x > 0 ? 1 : -1) * 0.1;

      // if done, explode
      if (s.removed) {
        bulletsAdding.add(makeExplosion(s.pos, 20, plus ? 60 : 30, 30));
      }
      else {
        // still going, smoke
        s.smokeCooldown--;
        if (s.smokeCooldown <= 0) {
          particles.add(smokePuff(s.pos, 2, 0.025, 0));
          s.smokeCooldown = 4 + Math.random() * 2;
        }
      }
    };

    return shell;
  }

  Bullet makeGrenade(Vec pos, Vec vel, boolean plus) {
    Bullet grenade = makeBullet(pos, vel, 0);

    grenade.sprite = spriteCache.get(SpriteID.BULLET_GRENADE);
    grenade.fuse = plus ? 180 : 60;

    grenade.draw = g -> {
      Graphics.sprite(g.sprite, g.pos.minus(3.5, 3.5));
    };

    grenade.tick = g -> {
      // move
      g.lastPos = g.pos.clone();
      g.pos = g.pos.add(g.vel);
      g.vel.y += 0.2;

      // collide with discs
      Disc hit = firstRayHit(g.lastPos, g.pos, 2);
      if (hit != null) {
        g.fuse = 0;
      }

      double elasticity = plus ? 1.05 : 0.75;

      // bounce off edges
      if (g.pos.x < 0) {
        g.vel.x = Math.abs(g.vel.x) * elasticity;
      }
      if (g.pos.x > 128) {
        g.vel.x = -Math.abs(g.vel.x) * elasticity;
      }
      if (g.pos.y < 0) {
        g.vel.y = Math.abs(g.vel.y) * elasticity;
      }
      if (g.pos.y > 128) {
        g.vel.y = -Math.abs(g.vel.y) * elasticity;
      }

      // fuse!
      g.fuse--;
      if (g.fuse <= 0) {
        bulletsAdding.add(makeExplosion(g.pos, 20, plus ? 40 : 30, 30));
        g.removed = true;
      }
    };

    return grenade;
  }

  Bullet makeMine(Vec pos, boolean plus) {
    Bullet mine = makeBullet(pos, Vec.zero(), 0);

    mine.sprite = spriteCache.get(SpriteID.WEAPON_MINES);

    mine.fuse = plus ? 180 : 60;

    mine.draw = m -> {
      if (m.fuse > 0 || frame % 16 < 8) {
        Graphics.map(palette[8], palette[0]);
      }
      Graphics.sprite(m.sprite, m.pos.minus(4, 4));
      Graphics.map(palette[8], palette[8]);
    };

    mine.tick = m -> {
      // move
      m.lastPos = m.pos.clone();
      m.pos.y += m.vel.y;
      m.vel.y += 0.2;
      if (m.pos.y > 128) {
        m.pos.y = 128;
        m.vel.y = 0;
      }

      // fuse
      if (m.fuse > 0) {
        m.fuse--;
      }

      // collide with discs
      if (m.fuse <= 0) {
        Disc hit = firstRayHit(m.lastPos, m.pos, 3);
        if (hit != null) {
          bulletsAdding.add(makeExplosion(m.pos, 20, plus ? 100 : 30, 30));
          m.removed = true;
        }
      }
    };

    return mine;
  }

  Bullet makeFlame(Vec pos, Vec vel, boolean plus) {
    Bullet flame = makeBullet(pos, vel, 0);

    flame.radius = ((plus ? 60 : 30) - Math.abs(flame.fuse - 30)) * 0.375 + 2;
    flame.fuse = plus ? 90 : 60;

    flame.draw = f -> {
      if (Math.random() < 0.5) {
        if (Math.random() < 0.5) {
          Graphics.map(palette[7], palette[10]);
        }
        else {
          Graphics.map(palette[7], palette[8]);
        }
      } else if (Math.random() < 0.5) {
        Graphics.map(palette[7], palette[4]);
      }
      Graphics.easeSprite(
        sheet.slice(
          Vec.ofInt(40 + Math.floor(Math.random() * 4) * 8, 64),
          8, 8
        ),
        f.pos.minus(f.radius, f.radius),
        f.radius * 2, f.radius * 2,
        Math.random() < 0.5
      );
      Graphics.map(palette[7], palette[7]);
    };

    flame.tick = f -> {
      f.radius = ((plus ? 60 : 30) - Math.abs(f.fuse - 30)) * 0.375 + 2;

      // move
      f.lastPos = f.pos.clone();
      f.pos = f.pos.add(f.vel);
      f.vel.y += 0.2;
      f.vel = f.vel.multiply(0.9);

      // collide with discs
      if (Math.random() < 0.5) {
        Disc hit = firstRayHit(f.lastPos, f.pos, f.radius);
        if (hit != null) {
          hitDisc(hit, 1);
        }
      }

      // bounce off edges
      if (f.pos.x < 0) {
        f.vel.x = Math.abs(f.vel.x);
      }
      if (f.pos.x > 128) {
        f.vel.x = -Math.abs(f.vel.x);
      }
      if (f.pos.y < 0) {
        f.vel.y = Math.abs(f.vel.y);
      }
      if (f.pos.y > 128) {
        f.vel.y = -Math.abs(f.vel.y);
      }

      // fuse!
      f.fuse--;
      if (f.fuse <= 0) {
        f.removed = true;
      }
    };

    return flame;
  }

  Bullet makeLaser(Vec start, Vec end, boolean plus) {
    Bullet laser = makeBullet(start, Vec.zero(), 0);

    if (start.x > end.x) {
      double temp = start.x;
      start.x = end.x;
      end.x = temp;
    }

    laser.lastPos = start;
    laser.pos = end;

    laser.fuse = plus ? 12 : 30;

    laser.draw = _ -> {
      Graphics.easeSprite(
        sheet.slice(
          Vec.ofInt(
            88 + 5 - Math.floor(laser.fuse / (plus ? 6 : 30) * 6),
            64
          ),
          1, 8
        ),
        laser.lastPos.minus(0, 4),
        laser.pos.x - laser.lastPos.x,
        8
      );
    };

    laser.tick = l -> {
      // collide with discs
      for (Disc d : discs) {
        if (d.cooldown <= 0) {
          if (
            d.pos.x + d.radius >= start.x - 3 &&
            d.pos.x - d.radius <= end.x + 3 &&
            d.pos.y + d.radius >= start.y - 3 &&
            d.pos.y - d.radius <= end.y + 3
          ) {
            // hit compressed space
            hitDisc(d, 100);
          }
        }
      }

      // age
      l.fuse--;
      if (l.fuse <= 0) {
        l.removed = true;
      }
    };

    return laser;
  }

  Bullet makeExplosion(Vec pos, double startRadius, double endRadius, double p) {
    Bullet explosion = makeBullet(pos, Vec.zero(), 0);

    explosion.p = p;
    explosion.a = 0;

    shake(10);
    Audios.emit(sounds.get(SoundID.EXPLOSION));

    explosion.draw = e -> {
      Color col = palette[7];
      double alpha = e.a / e.p,
        radius = startRadius + (endRadius - startRadius) * alpha;
      if (alpha > 0.5) {
        col = palette[0];
      }
      Graphics.fillCircle(e.pos, radius, col);
    };

    explosion.tick = e -> {
      e.a++;
      double alpha = e.a / e.p,
        radius = startRadius + (endRadius - startRadius) * alpha;
      for (Disc d : discs) {
        if (d.cooldown <= 0) {
          if (d.pos.inRange(e.pos, d.radius + radius)) {
            hitDisc(d, 100);
          }
        }
      }
      if (e.a >= e.p) {
        e.removed = true;
      }
    };

    return explosion;
  }

  static void hitDisc(Disc hit, double damage) {
    hit.health -= damage;
    if (hit.health <= 0) {
      hit.removed = true;
      hit.cooldown = 9999;
      addNewSparks(hit, 2, 0, 1);
      addNewSparks(hit, 4, 0, 1);
      addNewSparks(hit, 8, 0, 1);
      addNewSparks(hit, 16, 0, 1);
      particles.add(makeFlash(hit.pos, hit.radius, palette[10], 5));
      // death sound
      if (hit.radius < 10) {
        if (hit.seek) {
          Audios.emit(sounds.get(SoundID.DISC_DEATH_SEEKER));
        } else {
          Audios.emit(sounds.get(SoundID.DISC_DEATH));
        }
      } else {
        Audios.emit(sounds.get(SoundID.DISC_DEATH_BIG));
      }
    } else {
      // damage sound
      if (hit.hitCooldown <= 0) {
        Audios.emit(sounds.get(SoundID.DISC_DAMAGE));
      }
    }
    hit.hitCooldown = 4;
  }

  static Disc firstRayHit(Vec start, Vec end, double r) {
    Disc nearest = null;
    double nearestDistSq = Double.MAX_VALUE;

    // Vector representing the ray
    double dx = end.x - start.x;
    double dy = end.y - start.y;
    double rayLenSq = dx * dx + dy * dy;

    for (Disc d : discs) {
      if (d.cooldown > 0) continue;

      // Vector from start to disc center
      double tcx = d.pos.x - start.x;
      double tcy = d.pos.y - start.y;

      // Project disc center onto the ray line to find the closest point 't'
      // t is normalized (0.0 = start, 1.0 = end)
      double t = (rayLenSq == 0) ? 0 : (tcx * dx + tcy * dy) / rayLenSq;

      // Clamp t to the segment [0, 1] to treat ray as a finite segment
      double closestX, closestY;
      if (t < 0) {
        closestX = start.x;
        closestY = start.y;
      } else if (t > 1) {
        closestX = end.x;
        closestY = end.y;
      } else {
        closestX = start.x + t * dx;
        closestY = start.y + t * dy;
      }

      // Distance from the closest point on segment to disc center
      double distSq = (d.pos.x - closestX) * (d.pos.x - closestX) +
        (d.pos.y - closestY) * (d.pos.y - closestY);

      // Check if distance is within combined radii
      double combinedR = d.radius + r;
      if (distSq <= combinedR * combinedR) {
        // We want the disc whose HIT point is closest to 'start'
        // For simplicity, we use the distance to the disc center
        double distToStartSq = tcx * tcx + tcy * tcy;
        if (distToStartSq < nearestDistSq) {
          nearest = d;
          nearestDistSq = distToStartSq;
        }
      }
    }
    return nearest;
  }

  Particle oldSpark(Vec v, double cooldown, Color c) {
    return Particle.builder()
      .pos(v)
      .cooldown(cooldown)
      .c(c)
      .tick(s -> {
        s.lastPos = s.pos;
        s.cooldown = Math.max(s.cooldown - 1, 0);
        s.pos = s.pos.add(Vec.ofPolar(
          s.a * -2 * Math.PI,
          s.cooldown * 0.1)
        );
        if (!(s.cooldown > 0 || (s.c != palette[7] && s.c != palette[10]))) {
          s.removed = true;
        }
      })
      .draw(s -> {
        if (s.cooldown > 0) {
          Vec[] offsets;
          if (s.c != palette[10] && s.c != palette[7]) {
            offsets = new Vec[]{
              Vec.of(0, 0),
              Vec.of(-1, 0),
              Vec.of(1, 0),
              Vec.of(0, -1),
              Vec.of(0, 1)
            };
          }
          else {
            offsets = new Vec[]{
              Vec.of(0, 0),
              Vec.of(0, 1),
              Vec.of(1, 0),
              Vec.of(1, 1)
            };
          }
          for (Vec o : offsets) {
            if (s.lastPos != null)
              Graphics.line(s.pos.add(o), s.lastPos.add(o), s.c);
          }
        }
        else {
          Graphics.fillCircle(s.pos, 2, palette[2]);
        }
      })
      .build();
  }

  void addSparks(Disc d, Color c, double minSpeed, double aMin, double aMax) {
    double ao = Math.random();
    for (int i = 0; i < 8; ++i) {
      double a = ((ao + i / 8d) % 1) * (aMax - aMin) + aMin;

      Particle spark = oldSpark(
        d.pos.add(Vec.ofPolar((a - 0.25) * -2 * Math.PI, d.radius)),
        minSpeed + Math.floor(Math.random() * 60 * Math.random()),
        c
      );
      spark.a = a;
      spark.update();
      particles.add(spark);
    }
  }

  static Particle newSpark(Vec v, double a, double spd, boolean big) {
    Vec[] offsets;
    if (big) {
      offsets = new Vec[]{
        Vec.of(0, 0),
        Vec.of(-1, 0),
        Vec.of(1, 0),
        Vec.of(0, -1),
        Vec.of(0, 1)
      };
    }
    else {
      offsets = new Vec[]{
        Vec.of(0, 0),
        Vec.of(0, 1),
        Vec.of(1, 0),
        Vec.of(1, 1)
      };
    }

    return Particle.builder()
      .pos(v)
      .vel(Vec.ofPolar(a * -2 * Math.PI, spd))
      .tick(s -> {
        s.lastPos = s.pos.clone();
        s.pos = s.pos.add(s.vel);
        s.vel.y += 0.25;
        if (s.pos.y > 128) {
          s.removed = true;
        }
      })
      .draw(s -> {
        for (Vec o : offsets) {
          if (s.lastPos != null)
            Graphics.line(s.pos.add(o), s.lastPos.add(o), palette[10]);
        }
        if (s.lastPos != null)
          Graphics.line(s.pos, s.lastPos, palette[10]);
      })
      .build();
  }

  static void addNewSparks(Disc d, double minSpeed, double aMin, double aMax) {
    double ao = Math.random();
    for (int i = 1; i <= 8; ++i) {
      double a = ((ao + i / 8d) % 1) * (aMax - aMin) + aMin;
      Particle spark = newSpark(
        d.pos.add(Vec.ofPolar((a - 0.25) * -2 * Math.PI, d.radius)),
        a,
        minSpeed + Math.floor(Math.random() * 6 * Math.random()),
        d.radius > 10
      );
      spark.update();
      particles.add(spark);
    }
  }

  Particle smokePuff(Vec v, double o, double delta, double lift) {
    double a = Math.random();

    Particle puff = Particle.builder()
      .pos(v.add(Vec.ofPolar(a * -2 * Math.PI, 0)))
      .cooldown(0)
      .build();
    puff.tick = p -> {
      p.cooldown += delta;
      p.pos.y -= lift;
      if (p.cooldown >= 1) {
        p.removed = true;
      }
    };
    puff.draw = p -> {
      double r = (1 - p.cooldown) * 6;
      if (r > 4) {
        Graphics.fillCircle(p.pos.add(0, 2), r, palette[0]);
      }
      if (r > 2) {
        Graphics.fillCircle(p.pos.add(0, 1), r, palette[0]);
      }
      Graphics.fillCircle(p.pos, r, palette[5]);
    };
    return puff;
  }

  void boxParticles() {
    for (int i = 1; i <= 3; ++i) {
      particles.add(smokePuff(box, 8, Math.random() * 0.05 + 0.025, 1));
    }
  }

  static Particle makeFlash(Vec v, double r, Color c, double period) {
    return Particle.builder()
      .pos(v)
      .c(c)
      .cooldown(period)
      .tick(f -> {
        f.cooldown--;
        if (f.cooldown <= 0) {
          f.removed = true;
        }
      })
      .draw(f -> {
        Color col = f.c;
        if (period - f.cooldown <= 2) {
          col = palette[7];
        }
        Graphics.fillCircle(f.pos, r + f.cooldown - 5, c);
      })
      .build();
  }

  void initSplashScreen() {
    Audios.emit(sounds.get(SoundID.TYPING));
  }

  boolean secondTypingSound = false;

  void updateSplashScreen() {
    if (buttons.oOnce() || buttons.xOnce() || time() >= 8) {
      state = GameState.TITLE;
      initTitleScreen();
      return;
    }

    if (time() >= 3 && !secondTypingSound) {
      secondTypingSound = true;
      Audios.emit(sounds.get(SoundID.TYPING));
    }
  }

  void drawSplashScreen() {
    Graphics.clear(palette[0]);
    String string = "Farbs";
    int len = (int) Math.floor(Math.min(Math.max(time() * 6 - 6, 0), string.length()));
    len = (int) Math.max(Math.min(len, 30 - time() * 6), 0);
    string = string.substring(0, len);
    if ((time() * 4) % 2 < 1) {
      string = string + "_";
    }
    if (time() < 7) {
      Graphics.print(string, Vec.of(64 - 6 * 2, 64 - 5), palette[7]);
    }
  }

  int titleScroll = 16;

  void initTitleScreen() {
    Graphics.clear(palette[0]);
    Audios.stopAll();
    Audios.music(sounds.get(SoundID.MUSIC_TITLE));
  }

  void updateTitleScreen() {
    if (buttons.oOnce() || buttons.xOnce()) {
      state = GameState.GAME;
      initGame();
      return;
    }

    titleScroll = (titleScroll + 1) % (16 * 5 * 8);
    frame += 0.5;
  }

  void drawTitleScreen() {
    Graphics.clear(palette[12]);
    Graphics.camera(Vec.of(titleScroll, 0));
    Graphics.tiles(titleTiles, Vec.of(0, 0));
    Graphics.print(
      "✽  v" + version + " ✽  GAME: Farbs ✽  MUSIC: Gruber ♪  THANKS: Farbs Jr, Lan, DinoPuncher, Pyjamads ♥  GREETZ: Disc Room Team & Vlambeer ˇ",
      Vec.of(128, 120),
      palette[9]
    );

    Graphics.camera();

    double alpha = Math.min(frame / 30, 1);
    alpha = (1 - Math.sqrt(1 - alpha * alpha));
    double scale = alpha * 3 + Math.sin(frame / 60 * -2 * Math.PI) * 1 * (1 - alpha);
    if (frame > 30 && frame < 45) {
      alpha = 1 - (frame - 30) / 15;
      alpha *= alpha * alpha;
      Graphics.camera(Vec.of(
        Math.random() * alpha * 10,
        Math.random() * alpha * 10
      ));
    }
    Graphics.map(palette[15], palette[0]);
    Graphics.map(palette[4], palette[0]);
    Graphics.map(palette[8], palette[0]);
    Graphics.map(palette[2], palette[0]);
    for (double i = 0; i <= scale; i += 0.1) {
      drawLogo(i);
    }
    Graphics.map(palette[15], palette[15]);
    Graphics.map(palette[4], palette[4]);
    Graphics.map(palette[8], palette[8]);
    Graphics.map(palette[2], palette[2]);
    drawLogo(scale);

    // prompt to start
    // "PRESS 🅾️  OR ❎  TO START"
    if (frame > 120 && (frame % 30 < 15)) {
      prettyPrint(
        "PRESS C OR X TO START",
        Vec.of(64 - 2 * 20, 80),
        palette[7]
      );
    }
  }

  void drawLogo(double scale) {
    int sw = 36, sh = 19;
    if (!spriteCache.containsKey(SpriteID.LOGO)) {
      spriteCache.put(
        SpriteID.LOGO,
        sheet.slice(
          Vec.ofInt(88, 96),
          sw, sh
        )
      );
    }
    Graphics.easeSprite(
      spriteCache.get(SpriteID.LOGO),
      Vec.of(
        64 - sw * scale * 0.5,
        64 - sh * scale * 0.5 - scale * 9
      ),
      sw * scale,
      sh * scale
    );
  }
}
