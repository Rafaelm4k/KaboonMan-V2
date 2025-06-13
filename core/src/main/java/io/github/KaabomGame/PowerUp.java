package io.github.KaabomGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PowerUp {
    public enum Type {
        SPEED, DOUBLE_BOMB, INVINCIBILITY
    }

    private float x, y;
    private Texture texture;
    private Type type;
    private boolean taken = false;

    public PowerUp(float x, float y, Type type) {
        this.x = x * GameMap.TILE_SIZE;
        this.y = y * GameMap.TILE_SIZE;
        this.type = type;

        switch (type) {
            case SPEED:
                texture = new Texture("Velocidad_resized2.png");
                break;
            case DOUBLE_BOMB:
                texture = new Texture("DobleBomba_resized2.png");
                break;
            case INVINCIBILITY:
                texture = new Texture("Chaleco_resized2.png");
                break;
        }
    }

    public void render(SpriteBatch batch, float offsetX, float offsetY) {
        if (!taken) {
            batch.draw(texture, offsetX + x, offsetY + y);
        }
    }

    public void update(Player player) {
        if (!taken && playerCollides(player)) {
            taken = true;
            player.activatePowerUp(type);
        }
    }

    private boolean playerCollides(Player player) {
        return Math.abs(player.getX() - x) < GameMap.TILE_SIZE / 2 &&
            Math.abs(player.getY() - y) < GameMap.TILE_SIZE / 2;
    }

    public void dispose() {
        texture.dispose();
    }

    public boolean isTaken() {
        return taken;
    }
}

