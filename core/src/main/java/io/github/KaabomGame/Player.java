package io.github.KaabomGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Player {
    private static final int SIZE = 28;
    private float x, y;
    private ShapeRenderer shapeRenderer;

    private float offsetX = 0;
    private float offsetY = 0;

    public Player() {
        shapeRenderer = new ShapeRenderer();
        x = 1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f;
        y = 1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f;
    }

    public void setOffsets(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void update(float delta) {
        float moveSpeed = 110 * delta;
        float newX = x;
        float newY = y;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            newX -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            newX += moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            newY += moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            newY -= moveSpeed;
        }

        if (canMoveTo(newX, y)) {
            x = newX;
        }
        if (canMoveTo(x, newY)) {
            y = newY;
        }
    }

    public void render(float offsetX, float offsetY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(x + offsetX, y + offsetY, SIZE / 2f);
        shapeRenderer.end();
    }

    private boolean canMoveTo(float newX, float newY) {
        float radius = SIZE / 2f;

        float left = newX - radius;
        float right = newX + radius;
        float bottom = newY - radius;
        float top = newY + radius;

        int[] checkX = {
            (int)(left / GameMap.TILE_SIZE),
            (int)(right / GameMap.TILE_SIZE)
        };
        int[] checkY = {
            (int)(bottom / GameMap.TILE_SIZE),
            (int)(top / GameMap.TILE_SIZE)
        };

        for (int tx : checkX) {
            for (int ty : checkY) {
                if (tx < 0 || ty < 0 || ty >= GameMap.MAP.length || tx >= GameMap.MAP[0].length) {
                    return false;
                }

                int tile = GameMap.MAP[GameMap.mapY(ty)][tx];
                if (tile != GameMap.EMPTY) {
                    return false;
                }
            }
        }

        return true;
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }


}







