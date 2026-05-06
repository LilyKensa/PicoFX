package dev.huey.picofx.api.bases;

import javafx.scene.image.Image;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder(buildMethodName = "internalBuild")
public class Font {

  public static class FontBuilder {
    public Font build() {
      Font font = this.internalBuild();
      font.init();
      return font;
    }
  }

  public static Font pico8() {
    return builder()
      .source("pico-8")
      .gridWidth(8).gridHeight(8)
      .charWidth(7).charHeight(5)
      .spaceWidth(4).spaceHeight(6)
      .rows(16).columns(15)
      .chars(List.of(
        "▮", "■", "□", "⁙", "⁘", "‖", "◀", "▶", "「", "」", "¥", "•", "、", "。", "゛", "゜",
        " ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/",
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?",
        "@", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
        "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "[", "\\", "]", "^", "_",
        "`", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "{", "|", "}", "~", "○",
        "█", "▒", "🐱", "⬇️", "░", "✽", "●", "♥", "☉", "웃", "⌂", "⬅️", "😐", "♪", "🅾️", "◆",
        "…", "➡️", "★", "⧗", "⬆️", "ˇ", "∧", "❎", "▤", "▥", "あ", "い", "う", "え", "お", "か",
        "き", "く", "け", "こ", "さ", "し", "す", "せ", "そ", "た", "ち", "つ", "て", "と", "な", "に",
        "ぬ", "ね", "の", "は", "ひ", "ふ", "へ", "ほ", "ま", "み", "む", "め", "も", "や", "ゆ", "よ",
        "ら", "り", "る", "れ", "ろ", "わ", "を", "ん", "っ", "ゃ", "ゅ", "ょ", "ア", "イ", "ウ", "エ",
        "オ", "カ", "キ", "ク", "ケ", "コ", "サ", "シ", "ス", "セ", "ソ", "タ", "チ", "ツ", "テ", "ト",
        "ナ", "ニ", "ヌ", "ネ", "ノ", "ハ", "ヒ", "フ", "ヘ", "ホ", "マ", "ミ", "ム", "メ", "モ", "ヤ",
        "ユ", "ヨ", "ラ", "リ", "ル", "レ", "ロ", "ワ", "ヲ", "ン", "ッ", "ャ", "ュ", "ョ", "◜", "◝"
      ))
      .build();
  }
  
  String source;
  
  @Getter
  int gridWidth, gridHeight, charWidth, charHeight, spaceWidth, spaceHeight, rows, columns;
  
  List<String> chars;

  public final Map<String, Sprite> map = new HashMap<>();
  
  public void init() {
    Sprite sheet = new Sprite(new Image(
      Sprite.class.getResource("/assets/fonts/%s.png".formatted(source)).toExternalForm()
    ));

    int index = 0;
    for (int y = 0; y < columns; ++y) {
      for (int x = 0; x < rows; ++x) {
        Vec.Int v = Vec.ofInt(x * gridWidth, y * gridHeight);

        map.put(chars.get(index++), sheet.slice(v, charWidth, charHeight));
      }
    }
  }
  
  public Sprite getSprite(String id) {
    return map.get(id);
  }
}
