package io.github.KaabomGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

import java.util.List;
import java.util.Random;

public class Enemy {
    private static final float SIZE = 28f;
    private float x, y;
    private float speed = 60;
    private boolean alive = true;

    private float moveTimer = 0;
    private float moveInterval = 1.0f; // cada 1 segundo decide nueva dirección
    private int[] currentDirection = {0, 0};
    private Random random = new Random();

    // Para animación
    private static Texture enemySheet;
    private static Animation<TextureRegion> animUp, animDown, animLeft, animRight;
    private static final int FRAME_COLS = 3, FRAME_ROWS = 4; // ajusta según tu spritesheet

    private float stateTime = 0f;
    private Direction facing = Direction.DOWN;

    private SpriteBatch batch;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;

        batch = new SpriteBatch();

        // Cargar animaciones solo una vez
        if (enemySheet == null) {
            enemySheet = new Texture("spritesheetenemy1.png"); // pon aquí el nombre correcto

            TextureRegion[][] tmp = TextureRegion.split(enemySheet,
                enemySheet.getWidth() / FRAME_COLS,
                enemySheet.getHeight() / FRAME_ROWS);

            // Supongamos filas: 0=Down, 1=Left, 2=Right, 3=Up
            animDown = new Animation<>(0.15f, tmp[0]);
            animLeft = new Animation<>(0.15f, tmp[1]);
            animRight = new Animation<>(0.15f, tmp[2]);
            animUp = new Animation<>(0.15f, tmp[3]);
        }
    }

    public void update(float delta, Player player, List<Bomb> bombs) {
        if (!alive) return;

        stateTime += delta;

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

                        // Actualizar dirección para animación
                        if (Math.abs(moveX) > Math.abs(moveY)) {
                            facing = moveX > 0 ? Direction.RIGHT : Direction.LEFT;
                        } else if (Math.abs(moveY) > 0) {
                            facing = moveY > 0 ? Direction.UP : Direction.DOWN;
                        }
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

                // Actualizar dirección para animación
                if (Math.abs(moveX) > Math.abs(moveY)) {
                    facing = moveX > 0 ? Direction.RIGHT : Direction.LEFT;
                } else if (Math.abs(moveY) > 0) {
                    facing = moveY > 0 ? Direction.UP : Direction.DOWN;
                }
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

        batch.begin();

        Animation<TextureRegion> anim;
        switch (facing) {
            case UP:
                anim = animUp;
                break;
            case DOWN:
                anim = animDown;
                break;
            case LEFT:
                anim = animLeft;
                break;
            case RIGHT:
            default:
                anim = animRight;
                break;
        }

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
