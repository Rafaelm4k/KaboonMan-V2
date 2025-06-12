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
    private static final float FRAME_DURATION = 0.1f; // duración de cada frame de explosión

    private static Animation<TextureRegion> explosionCenterAnimation;
    private static Animation<TextureRegion> explosionLeftAnimation;
    private static Animation<TextureRegion> explosionRightAnimation;
    private static Animation<TextureRegion> explosionUpAnimation;
    private static Animation<TextureRegion> explosionDownAnimation;

    private float explosionTimer = 0f;
    private boolean exploded = false;

    private int tileX, tileY;
    private float timer;
    private float stateTime = 0f;

    private Texture bombTexture;
    private SpriteBatch batch;

    public Bomb(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
        this.timer = EXPLOSION_TIME;

        bombTexture = new Texture("bomba.png");
        batch = new SpriteBatch();

        // Inicializar animaciones solo una vez
        if (explosionCenterAnimation == null) {
            explosionCenterAnimation = loadAnimation("spriteheetbomba1.png");
            explosionLeftAnimation   = loadAnimation("spriteheetbomba2.png");
            explosionRightAnimation  = loadAnimation("spriteheetbomba3.png");
            explosionUpAnimation     = loadAnimation("spriteheetbomba4.png");
            explosionDownAnimation   = loadAnimation("spriteheetbomba5.png");
        }
    }

    private Animation<TextureRegion> loadAnimation(String path) {
        Texture sheet = new Texture(path);
        TextureRegion[][] tmp = TextureRegion.split(sheet, 32, 32); // Ajusta 16x16 si es otro tamaño
        TextureRegion[] frames = tmp[0]; // Suponiendo que está en la fila 0
        return new Animation<>(FRAME_DURATION, frames);
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
            stateTime += delta;
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
            // Centro
            TextureRegion centerFrame = explosionCenterAnimation.getKeyFrame(stateTime, false);
            batch.draw(centerFrame, tileX * GameMap.TILE_SIZE + offsetX,
                tileY * GameMap.TILE_SIZE + offsetY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);

            // Izquierda
            if (canExplodeAt(tileX - 1, tileY)) {
                TextureRegion leftFrame = explosionLeftAnimation.getKeyFrame(stateTime, false);
                batch.draw(leftFrame, (tileX - 1) * GameMap.TILE_SIZE + offsetX,
                    tileY * GameMap.TILE_SIZE + offsetY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
            }

            // Derecha
            if (canExplodeAt(tileX + 1, tileY)) {
                TextureRegion rightFrame = explosionRightAnimation.getKeyFrame(stateTime, false);
                batch.draw(rightFrame, (tileX + 1) * GameMap.TILE_SIZE + offsetX,
                    tileY * GameMap.TILE_SIZE + offsetY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
            }

            // Arriba
            if (canExplodeAt(tileX, tileY + 1)) {
                TextureRegion upFrame = explosionUpAnimation.getKeyFrame(stateTime, false);
                batch.draw(upFrame, tileX * GameMap.TILE_SIZE + offsetX,
                    (tileY + 1) * GameMap.TILE_SIZE + offsetY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
            }

            // Abajo
            if (canExplodeAt(tileX, tileY - 1)) {
                TextureRegion downFrame = explosionDownAnimation.getKeyFrame(stateTime, false);
                batch.draw(downFrame, tileX * GameMap.TILE_SIZE + offsetX,
                    (tileY - 1) * GameMap.TILE_SIZE + offsetY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
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
        // Las texturas de animación no se eliminan aquí para evitar problemas si hay más bombas activas
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }
}
