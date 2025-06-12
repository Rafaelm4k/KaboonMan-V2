package io.github.KaabomGame;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import java.util.List;
import java.util.Random;

public class Enemy {
    private static final float SIZE = 28f;
    private float x, y;
    private float speed = 60;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private boolean alive = true;
    private float moveTimer = 0;
    private float moveInterval = 1.0f; // cada 1 segundo decide nueva dirección
    private int[] currentDirection = {0, 0};
    private Random random = new Random();

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void update(float delta, Player player, List<Bomb> bombs) {
        if (!alive) return;

        int enemyTileX = (int)(x / GameMap.TILE_SIZE);
        int enemyTileY = (int)(y / GameMap.TILE_SIZE);
        int playerTileX = (int)(player.getX() / GameMap.TILE_SIZE);
        int playerTileY = (int)(player.getY() / GameMap.TILE_SIZE);

        boolean playerInRange = Math.abs(enemyTileX - playerTileX) <= 3 && Math.abs(enemyTileY - playerTileY) <= 3;

        if (playerInRange) {
            float bestMoveX = 0;
            float bestMoveY = 0;
            float bestDistance = Float.MAX_VALUE;

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
                    float distance = dx * dx + dy * dy;

                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestMoveX = moveX;
                        bestMoveY = moveY;
                    }
                }
            }

            x += bestMoveX;
            y += bestMoveY;
        } else {
            moveTimer += delta;
            if (moveTimer >= moveInterval) {
                moveTimer = 0;
                int[][] directions = {
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}
                };
                currentDirection = directions[random.nextInt(directions.length)];
            }

            float moveX = currentDirection[0] * speed * delta;
            float moveY = currentDirection[1] * speed * delta;
            float newX = x + moveX;
            float newY = y + moveY;

            if (canMoveTo(newX, newY, bombs)) {
                x = newX;
                y = newY;
            } else {
                moveTimer = moveInterval; // fuerza cambio de dirección en siguiente frame
            }
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public void kill() {
        alive = false;
    }

    public void render(float offsetX, float offsetY) {
        if (!alive) return;

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
