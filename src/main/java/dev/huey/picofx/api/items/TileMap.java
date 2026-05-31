package dev.huey.picofx.api.items;

import dev.huey.picofx.api.Entry;
import dev.huey.picofx.api.modules.Utils;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.*;

@Builder(buildMethodName = "internalBuild")
public class TileMap {

  public static class TileMapBuilder {
    public TileMap build() {
      TileMap tiles = this.internalBuild();
      tiles.init();
      return tiles;
    }
  }


  @Builder.Default
  String namespace = Entry.instance.getId();
  String source;

  @Getter
  int gridWidth, gridHeight;

  @Singular("define")
  public Map<Character, Sprite> map;

  public String[] tiles;

  @Builder.Default
  List<List<Sprite>> grid = new ArrayList<>();

  public void init() {
    tiles = Utils.loadText("/assets/%s/tiles/%s.txt".formatted(namespace, source)).split("\n");
  }
}
