package io.github.KaabomGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;

public class Bomb {
    private static final float EXPLOSION_TIME = 3f;
    private static final float EXPLOSION_DURATION = 0.5f;

    private static Texture explosionSheet;
    private static Animation<TextureRegion>[] explosionAnimations;

    private float explosionTimer = 0f;
    private boolean exploding = false;
    private boolean rendered = false;

    private int tileX, tileY;
    private float timer;
    private boolean exploded = false;

    private Texture bombTexture;
    private SpriteBatch batch;

    private enum Direction { CENTER, UP, DOWN, LEFT, RIGHT }

    public Bomb(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.timer = EXPLOSION_TIME;

        bombTexture = new Texture("bomba.png");
        batch = new SpriteBatch();

        // Inicialización de animaciones si no están cargadas
        if (explosionSheet == null) {
            explosionSheet = new Texture("explosion.png"); // Asegúrate de tener la textura correcta
            TextureRegion[][] tmp = TextureRegion.split(explosionSheet, 16, 16); // Ajusta tamaño si es necesario

            explosionAnimations = new Animation[5]; // CENTER, UP, DOWN, LEFT, RIGHT

            // Fila 0 → Centro
            TextureRegion[] centerFrames = tmp[0];
            explosionAnimations[Direction.CENTER.ordinal()] = new Animation<>(0.1f, centerFrames);

            // Fila 1 → Medio (para partes verticales)
            TextureRegion[] middleFrames = tmp[1];
            explosionAnimations[Direction.UP.ordinal()] = new Animation<>(0.1f, middleFrames);
            explosionAnimations[Direction.DOWN.ordinal()] = new Animation<>(0.1f, middleFrames);

            // Fila 2 → Finales (para partes horizontales)
            TextureRegion[] endFrames = tmp[2];
            explosionAnimations[Direction.LEFT.ordinal()] = new Animation<>(0.1f, endFrames);
            explosionAnimations[Direction.RIGHT.ordinal()] = new Animation<>(0.1f, endFrames);
        }
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

    public List<int[]> getAffectedTiles() {
        List<int[]> tiles = new ArrayList<>();
        tiles.add(new int[]{tileX, tileY});
        tiles.add(new int[]{tileX - 1, tileY});
        tiles.add(new int[]{tileX + 1, tileY});
        tiles.add(new int[]{tileX, tileY + 1});
        tiles.add(new int[]{tileX, tileY - 1});
        return tiles;
    }

    public void render(float offsetX, float offsetY) {
        batch.begin();

        if (!exploded) {
            batch.draw(bombTexture, tileX * GameMap.TILE_SIZE + offsetX,
                tileY * GameMap.TILE_SIZE + offsetY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
        } else if (explosionTimer > 0) {
            float elapsed = EXPLOSION_DURATION - explosionTimer;

            // Dibuja explosión en el centro
            drawExplosionTile(tileX, tileY, offsetX, offsetY, Direction.CENTER, elapsed);

            // Dibuja explosión a la izquierda
            if (canExplodeAt(tileX - 1, tileY)) {
                drawExplosionTile(tileX - 1, tileY, offsetX, offsetY, Direction.LEFT, elapsed);
            }
            // Dibuja explosión a la derecha
            if (canExplodeAt(tileX + 1, tileY)) {
                drawExplosionTile(tileX + 1, tileY, offsetX, offsetY, Direction.RIGHT, elapsed);
            }
            // Dibuja explosión arriba
            if (canExplodeAt(tileX, tileY + 1)) {
                drawExplosionTile(tileX, tileY + 1, offsetX, offsetY, Direction.UP, elapsed);
            }
            // Dibuja explosión abajo
            if (canExplodeAt(tileX, tileY - 1)) {
                drawExplosionTile(tileX, tileY - 1, offsetX, offsetY, Direction.DOWN, elapsed);
            }
        }

        batch.end();
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
        tryDestroy(tileX, tileY);
        if (canExplodeAt(tileX - 1, tileY)) tryDestroy(tileX - 1, tileY);
        if (canExplodeAt(tileX + 1, tileY)) tryDestroy(tileX + 1, tileY);
        if (canExplodeAt(tileX, tileY + 1)) tryDestroy(tileX, tileY + 1);
        if (canExplodeAt(tileX, tileY - 1)) tryDestroy(tileX, tileY - 1);
    }

    private void drawExplosionTile(int x, int y, float offsetX, float offsetY, Direction direction, float stateTime) {
        Animation<TextureRegion> anim = explosionAnimations[direction.ordinal()];
        TextureRegion currentFrame = anim.getKeyFrame(stateTime, false);
        batch.draw(currentFrame, x * GameMap.TILE_SIZE + offsetX,
            y * GameMap.TILE_SIZE + offsetY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
    }

    private boolean canExplodeAt(int x, int y) {
        if (x < 0 || y < 0 || y >= GameMap.MAP.length || x >= GameMap.MAP[0].length)
            return false;

        int tile = GameMap.MAP[mapY(y)][x];
        return tile == GameMap.EMPTY || tile == GameMap.DESTRUCTIBLE_BLOCK;
    }

    public boolean hasExploded() {
        return exploded && explosionTimer <= 0;
    }

    public void dispose() {
        batch.dispose();
        bombTexture.dispose();
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }
}
