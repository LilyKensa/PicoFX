package dev.huey.picofx.api;

import dev.huey.picofx.api.bases.Vec;
import dev.huey.picofx.api.items.Font;
import dev.huey.picofx.api.items.Sound;
import dev.huey.picofx.api.items.Sprite;
import dev.huey.picofx.api.modules.Audios;
import dev.huey.picofx.api.modules.Graphics;
import javafx.scene.paint.Color;

import java.util.Random;

public class Fantasy {

  static void slightShuffle(int[] array, int maxDisplacement) {
    if (array == null) return;
    if (maxDisplacement <= 0 || array.length <= 1) return;

    Random rand = new Random();
    int n = array.length;

    for (int i = 0; i < n; i++) {
      int minBound = Math.max(0, i - maxDisplacement);
      int maxBound = Math.min(n - 1, i + maxDisplacement);

      int randomIndex = rand.nextInt((maxBound - minBound) + 1) + minBound;

      // Manual swap for primitive array
      int temp = array[i];
      array[i] = array[randomIndex];
      array[randomIndex] = temp;
    }
  }

  final Color[] palette;
  final Font font;

  final Sprite logoSpr = Sprite.load("_builtin", "pico-8-logo");

  final Sound startupSound = Sound.load("_builtin", "pico-8-startup");

  int frame = 0;

  int[] darkMixCounts = {8, 16, 32, 32, 16, 16, 8};
  int[][] darkMixes;

  int[][] lightOverlays;

  public Fantasy() {
    palette = Graphics.pico8Palette.clone();
    font = Font.pico8();
  }

  void start() {
    Graphics.font(font);

    frame = 0;

    darkMixes = new int[16][128];

    for (int i = 0; i < darkMixes.length; ++i) {
      int j = 0;
      for (int c = 0; c < darkMixCounts.length; ++c) {
        for (int ci = 0; ci < darkMixCounts[c]; ++ci) {
          darkMixes[i][j] = c;
          j++;
        }
      }

      slightShuffle(darkMixes[i], 64);
    }

    lightOverlays = new int[32][44];

    for (int i = 0; i < lightOverlays.length; ++i) {
      for (int j = 0; j < lightOverlays[i].length; ++j) {
        lightOverlays[i][j] = 10 + (int) (Math.random() * 4);
      }
    }
  }

  void update() {
    frame++;

    if (frame == 80) {
      Audios.emit(startupSound);
    }
  }

  void render() {
    Graphics.camera();
    Graphics.clear(palette[0]);

    if (frame >= 20 && frame < 48) {
      int t = frame - 20;

      for (int f : t >= 16 ? new int[]{-1, 0} : new int[]{0}) {
        for (int i = 0; i < darkMixes.length; ++i) {
          int x = 2 + 8 * i + f;

          for (int y = 0; y < darkMixes[i].length; ++y) {
            if (t < 22 || y % 4 == 0) {
              Graphics.pixel(Vec.of(x, y), palette[darkMixes[i][y]]);
            }
          }
        }

        if (t >= 6) {
          for (int i = 0; i < 32; ++i) {
            int x = 4 * i + f;
            for (int j = 0; j < 64; ++j) {
              int y = j * 2;
              int c = 6 + ((j + i * 2) / 4) % 8;

              if (t < 22 || y % 4 == 0) {
                Graphics.pixel(Vec.of(x, y), palette[c]);
              }
            }
          }
        }

        if (t >= 12) {
          for (int i = 0; i < lightOverlays.length; ++i) {
            int x = 2 + i * 4 + f;
            for (int j = 0; j < lightOverlays[i].length; ++j) {
              int y = j * 3;

              if (t < 22 || y % 4 == 0) {
                Graphics.pixel(Vec.of(x, y), palette[lightOverlays[i][j]]);
              }
            }
          }
        }
      }
    }

    if (frame >= 78) {
      Graphics.sprite(logoSpr, Vec.of(0, 0));
    }

    if (frame > 88) {
      Graphics.print("PICO-FX 0.2.9B", Vec.of(0, 18), palette[6]);
    }

    if (frame > 90) {
      Graphics.print("(C) 2025-26 HUEY", Vec.of(0, 24), palette[6]);
    }

    if (frame > 98) {
      Graphics.print("PRESS CTRL + (NUMBER)", Vec.of(0, 36), palette[6]);
    }
  }
}
