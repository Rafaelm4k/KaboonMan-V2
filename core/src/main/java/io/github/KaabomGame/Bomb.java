package io.github.KaabomGame;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Bomb {
    private static final float EXPLOSION_TIME = 3f; // segundos hasta explotar
    private static final float EXPLOSION_DURATION = 0.5f; // segundos que dura la explosión

    private int tileX, tileY;
    private float timer;
    private boolean exploded = false;
    private float explosionTimer = 0f;

    private ShapeRenderer shapeRenderer;

    public Bomb(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.timer = EXPLOSION_TIME;
        this.shapeRenderer = new ShapeRenderer();
    }

    public void update(float delta) {
        if (!exploded) {
            timer -= delta;
            if (timer <= 0) {
                exploded = true;
                explosionTimer = EXPLOSION_DURATION;

                destroyNearbyBlocks();
            }
        } else {
            explosionTimer -= delta;
        }
    }

    public void render(float offsetX, float offsetY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (!exploded) {
            // Dibuja la bomba antes de explotar
            shapeRenderer.setColor(Color.BLACK);
            shapeRenderer.circle(tileX * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f + offsetX,
                tileY * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f + offsetY,
                GameMap.TILE_SIZE / 2f);
        } else if (explosionTimer > 0) {
            // Dibuja la explosión en cruz (bomba + 4 direcciones)
            shapeRenderer.setColor(Color.RED);

            // Centro (celda de la bomba)
            drawExplosionTile(tileX, tileY, offsetX, offsetY);

            // Izquierda
            if (canExplodeAt(tileX - 1, tileY)) {
                drawExplosionTile(tileX - 1, tileY, offsetX, offsetY);
            }
            // Derecha
            if (canExplodeAt(tileX + 1, tileY)) {
                drawExplosionTile(tileX + 1, tileY, offsetX, offsetY);
            }
            // Arriba
            if (canExplodeAt(tileX, tileY + 1)) {
                drawExplosionTile(tileX, tileY + 1, offsetX, offsetY);
            }
            // Abajo
            if (canExplodeAt(tileX, tileY - 1)) {
                drawExplosionTile(tileX, tileY - 1, offsetX, offsetY);
            }
        }

        shapeRenderer.end();
    }

    private void tryDestroy(int x, int y) {
        if (x < 0 || y < 0 || y >= GameMap.MAP.length || x >= GameMap.MAP[0].length) return;

        if (GameMap.MAP[mapY(y)][x] == GameMap.DESTRUCTIBLE_BLOCK) {
            GameMap.MAP[mapY(y)][x] = GameMap.EMPTY;
            System.out.println("Bloque destruido en (" + x + "," + y + ")");
        }

    }

    private int mapY(int tileY) {
        return GameMap.MAP.length - 1 - tileY;
    }


    private void destroyNearbyBlocks() {
        tryDestroy(tileX, tileY); // centro
        if (canExplodeAt(tileX - 1, tileY)) tryDestroy(tileX - 1, tileY); // izquierda
        if (canExplodeAt(tileX + 1, tileY)) tryDestroy(tileX + 1, tileY); // derecha
        if (canExplodeAt(tileX, tileY + 1)) tryDestroy(tileX, tileY + 1); // arriba
        if (canExplodeAt(tileX, tileY - 1)) tryDestroy(tileX, tileY - 1); // abajo
    }



    private void drawExplosionTile(int x, int y, float offsetX, float offsetY) {
        shapeRenderer.rect(x * GameMap.TILE_SIZE + offsetX,
            y * GameMap.TILE_SIZE + offsetY,
            GameMap.TILE_SIZE, GameMap.TILE_SIZE);
    }

    private boolean canExplodeAt(int x, int y) {
        if (x < 0 || y < 0 || y >= GameMap.MAP.length || x >= GameMap.MAP[0].length)
            return false;

        int tile = GameMap.MAP[mapY(y)][x];
        // Explosión pasa por vacíos y destruye destructibles, pero no pasa sólidos
        return tile == GameMap.EMPTY || tile == GameMap.DESTRUCTIBLE_BLOCK;
    }

    public boolean hasExploded() {
        return exploded && explosionTimer <= 0;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }
}


