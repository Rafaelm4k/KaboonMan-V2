package io.github.KaabomGame;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import java.util.List;
import java.util.Random;

public class Enemy {
    private static final float SIZE = 26f;
    private float x, y;
    private float speed = 90;

    // Dirección de movimiento actual (empezamos a la derecha)
    private int dirX = 1;
    private int dirY = 0;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Random random = new Random();

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta, Player player, List<Bomb> bombs) {
        float moveX = dirX * speed * delta;
        float moveY = dirY * speed * delta;

        if (canMoveTo(x + moveX, y + moveY, bombs)) {
            x += moveX;
            y += moveY;
        } else {
            // Elige aleatoriamente una nueva dirección válida
            tryNewRandomDirection(delta, bombs);
        }
    }

    private void tryNewRandomDirection(float delta, List<Bomb> bombs) {
        int[][] directions = {
            {1, 0},  // derecha
            {-1, 0}, // izquierda
            {0, 1},  // arriba
            {0, -1}  // abajo
        };

        // Mezclamos las direcciones para probar en orden aleatorio
        for (int i = 0; i < directions.length; i++) {
            int swapIndex = random.nextInt(directions.length);
            int[] temp = directions[i];
            directions[i] = directions[swapIndex];
            directions[swapIndex] = temp;
        }

        for (int[] dir : directions) {
            float moveX = dir[0] * speed * delta;
            float moveY = dir[1] * speed * delta;

            if (canMoveTo(x + moveX, y + moveY, bombs)) {
                dirX = dir[0];
                dirY = dir[1];
                x += moveX;
                y += moveY;
                return;
            }
        }
        // Si ninguna dirección es posible, no se mueve este frame
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

                // Si quieres considerar bombas como obstáculo:
                /*
                for (Bomb bomb : bombs) {
                    if (bomb.getTileX() == tx && bomb.getTileY() == ty) {
                        return false;
                    }
                }
                */
            }
        }
        return true;
    }
}
