package dev.huey.picofx.api.items;

import dev.huey.picofx.api.Entry;
import dev.huey.picofx.api.bases.Vec;
import dev.huey.picofx.api.modules.Utils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import lombok.Getter;

import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

public class Sprite {
  static public Sprite load(String namespace, String path) {
    return new Sprite(Utils.loadImage("/assets/%s/sprites/%s.png".formatted(namespace, path)));
  }

  static public Sprite load(String path) {
    return load(Entry.instance.getId(), path);
  }

  @Getter
  public boolean sliced = false;
  public Vec.Int sliceOrigin;

  @Getter
  int width, height;
  Sprite parent;
  IntBuffer buffer;
  
  public Set<Integer> flags = new HashSet<>();

  public Sprite(Sprite parent, Vec.Int sliceOrigin, int width, int height) {
    sliced = true;
    this.parent = parent;
    this.sliceOrigin = sliceOrigin;
    this.width = width;
    this.height = height;
  }

  public Sprite(Image image) {
    width = (int) image.getWidth();
    height = (int) image.getHeight();
    
    buffer = IntBuffer.allocate(width * height);
    
    image.getPixelReader().getPixels(
      0, 0,
      width, height,
      PixelFormat.getIntArgbInstance(),
      buffer,
      width
    );
  }

  public Sprite flag(int flag) {
    flags.add(flag);
    return this;
  }

  public Sprite flag(int... list) {
    for (int f : list) {
      flags.add(f);
    }
    return this;
  }
  
  public Sprite unflag(int flag) {
    flags.remove(flag);
    return this;
  }
  
  public Sprite slice(Vec.Int v, int width, int height) {
    if (isSliced()) {
      return parent.slice(Vec.ofInt(v.x + sliceOrigin.x, v.y + sliceOrigin.y), width, height);
    }
    
    return new Sprite(this, v, width, height);
  }

  public Sprite slice(Vec v, int width, int height) {
    return slice(v.toInt(), width, height);
  }

  public Sprite slice(Vec v, Vec s) {
    return slice(v, (int) s.x, (int) s.y);
  }

  public int getPixel(Vec.Int v) {
    if (isSliced()) {
      return parent.getPixel(Vec.ofInt(v.x + sliceOrigin.x, v.y + sliceOrigin.y));
    }

    return buffer.get(v.y * width + v.x);
  }
}
