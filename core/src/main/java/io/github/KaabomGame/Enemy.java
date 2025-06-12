package io.github.KaabomGame;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import java.util.List;

public class Enemy {
    private static final float SIZE = 28f;
    private float x, y;
    private float speed = 60;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta, Player player, List<Bomb> bombs) {
        float bestMoveX = 0;
        float bestMoveY = 0;
        float bestDistance = Float.MAX_VALUE;

        // Opciones de movimiento: derecha, izquierda, arriba, abajo
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        for (int[] dir : directions) {
            float moveX = dir[0] * speed * delta;
            float moveY = dir[1] * speed * delta;

            float newX = x + moveX;
            float newY = y + moveY;

            if (canMoveTo(newX, newY, bombs)) {
                float dx = player.getX() - newX;
                float dy = player.getY() - newY;
                float distance = dx * dx + dy * dy; // No hace falta sqrt

                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestMoveX = moveX;
                    bestMoveY = moveY;
                }
            }
        }

        x += bestMoveX;
        y += bestMoveY;
    }

    public void render(float offsetX, float offsetY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(x + offsetX, y + offsetY, SIZE / 2f);
        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    public boolean canMoveTo(float newX, float newY, List<Bomb> bombs) {
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

                // Descomenta esto si quieres que las bombas bloqueen al enemigo:

                for (Bomb bomb : bombs) {
                    if (bomb.getTileX() == tx && bomb.getTileY() == ty) {
                        return false;
                    }
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
}
