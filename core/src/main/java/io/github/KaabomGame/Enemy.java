package io.github.KaabomGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;
import java.util.Random;

public class Enemy {
    private static final int SIZE = 28;
    private float x, y;
    private float speed = 60f;
    private boolean alive = true;

    private float moveTimer = 0;
    private float moveInterval = 1.0f;
    private int[] currentDirection = {0, 0};
    private Random random = new Random();

    private static Texture enemySheet;
    private static Animation<TextureRegion> anim;

    private float stateTime = 0f;
    private SpriteBatch batch;

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
        batch = new SpriteBatch();

        if (enemySheet == null) {
            enemySheet = new Texture("spritesheetenemy1.png"); // 1 fila x 3 columnas (20x20 cada frame)
            TextureRegion[][] tmp = TextureRegion.split(enemySheet, 20, 20);

            TextureRegion[] frames = new TextureRegion[3];
            for (int i = 0; i < 3; i++) {
                frames[i] = tmp[0][i]; // Primera fila, columna 0, 1, 2
            }
            anim = new Animation<>(0.15f, frames);
        }
    }

    public void update(float delta, Player player, List<Bomb> bombs) {
        if (!alive) return;

        stateTime += delta;
        moveTimer += delta;

        int enemyTileX = (int)(x / GameMap.TILE_SIZE);
        int enemyTileY = (int)(y / GameMap.TILE_SIZE);
        int playerTileX = (int)(player.getX() / GameMap.TILE_SIZE);
        int playerTileY = (int)(player.getY() / GameMap.TILE_SIZE);

        int dx = playerTileX - enemyTileX;
        int dy = playerTileY - enemyTileY;

        boolean playerNearby = Math.abs(dx) <= 3 && Math.abs(dy) <= 3;

        if (playerNearby) {
            // Perseguir al jugador (prioridad eje X, luego eje Y)
            if (Math.abs(dx) > Math.abs(dy)) {
                currentDirection[0] = Integer.signum(dx);
                currentDirection[1] = 0;
            } else {
                currentDirection[0] = 0;
                currentDirection[1] = Integer.signum(dy);
            }
        } else if (moveTimer >= moveInterval) {
            // Movimiento aleatorio
            moveTimer = 0;
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
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
            moveTimer = moveInterval; // Fuerza cambio de dirección
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

        batch.begin();
        TextureRegion currentFrame = anim.getKeyFrame(stateTime, true);
        batch.draw(currentFrame, x + offsetX - SIZE / 2f, y + offsetY - SIZE / 2f, SIZE, SIZE);
        batch.end();
    }

    public void dispose() {
        batch.dispose();
        if (enemySheet != null) enemySheet.dispose();
    }

    public boolean canMoveTo(float newX, float newY, List<Bomb> bombs) {
        float radius = SIZE / 2f;

        float left = newX - radius;
        float right = newX + radius;
        float bottom = newY - radius;
        float top = newY + radius;

        int[] checkX = {(int)(left / GameMap.TILE_SIZE), (int)(right / GameMap.TILE_SIZE)};
        int[] checkY = {(int)(bottom / GameMap.TILE_SIZE), (int)(top / GameMap.TILE_SIZE)};

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

    public float getX() { return x; }
    public float getY() { return y; }
}
