package dev.huey.picofx.game;

import dev.huey.picofx.api.bases.Game;
import dev.huey.picofx.api.bases.Vec;
import dev.huey.picofx.api.modules.Graphics;
import dev.huey.picofx.api.modules.Inputs;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class TestGame implements Game {
    Vec pos = Vec.of(64, 64);

    @Override
    public String getId() {
        return "test";
    }

    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {
        if (Inputs.key(KeyCode.LEFT)) {
            pos.x--;
        }
        if (Inputs.key(KeyCode.RIGHT)) {
            pos.x++;
        }
        if (Inputs.key(KeyCode.UP)) {
            pos.y--;
        }
        if (Inputs.key(KeyCode.DOWN)) {
            pos.y++;
        }
    }

    @Override
    public void render() {
        Graphics.clear();
        Graphics.fillCircle(pos, 3, Color.AQUA);
    }
}
