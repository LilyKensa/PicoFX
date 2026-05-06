package dev.huey.picofx.api.bases;

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

  String source;

  @Getter
  int gridWidth, gridHeight;

  @Singular("define")
  public Map<Character, Sprite> map;

  public String[] tiles;

  @Builder.Default
  List<List<Sprite>> grid = new ArrayList<>();

  public void init() {
    tiles = Utils.loadText("/assets/tiles/%s.txt".formatted(source)).split("\n");
  }
}
