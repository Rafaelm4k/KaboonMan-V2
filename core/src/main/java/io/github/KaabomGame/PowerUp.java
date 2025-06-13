package io.github.KaabomGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.awt.*;

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
            // Aquí cambia esto:
            player.applyPowerUp(convertToPlayerPowerUpType(type));
        }
    }

    private Player.PowerUpType convertToPlayerPowerUpType(Type type) {
        switch(type) {
            case SPEED:
                return Player.PowerUpType.SPEED;
            case DOUBLE_BOMB:
                return Player.PowerUpType.DOUBLE_BOMB;
            case INVINCIBILITY:
                return Player.PowerUpType.IMMUNITY;
            default:
                return Player.PowerUpType.NONE;
        }
    }


    public Type getType() {
        return type;
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Player.PowerUpType getAsPlayerType() {
        return convertToPlayerPowerUpType(type);
    }




    private boolean playerCollides(Player player) {
        float range = GameMap.TILE_SIZE * 0.65f; // en vez de 0.5 * TILE_SIZE
        return Math.abs(player.getX() - x) < range &&
                Math.abs(player.getY() - y) < range;
    }


    public void dispose() {
        texture.dispose();
    }

    public boolean isTaken() {
        return taken;
    }
}

