package io.github.KaabomGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Player {
    private static final int SIZE = 28;
    private float x, y;
    private ShapeRenderer shapeRenderer;
    private int ignoreBombTileX = -1;
    private int ignoreBombTileY = -1;
    private float offsetX = 0;
    private float offsetY = 0;

    private Texture camDerSheet;
    private Texture camIzqSheet;
    private Animation<TextureRegion> camDer;
    private Animation<TextureRegion> camIzq;
    private TextureRegion currentFrame;
    private float stateTime = 0f;
    private boolean facingRight = true;

    public enum PowerUpType {
        SPEED, DOUBLE_BOMB, IMMUNITY, NONE
    }

    private PowerUpType activePowerUp = PowerUpType.NONE;
    private float powerUpTimer = 0f;

    private float baseSpeed = 100f;
    private float speed = 100f;
    private int maxBombs = 1;
    private boolean immune = false;

    public Player() {
        shapeRenderer = new ShapeRenderer();
        x = 1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f;
        y = 1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f;

        camDerSheet = new Texture(Gdx.files.internal("spriteheetplayer1.png"));
        camIzqSheet = new Texture(Gdx.files.internal("spriteheetplayer2.png"));

        TextureRegion[][] tmpDer = TextureRegion.split(camDerSheet, camDerSheet.getWidth() / 4, camDerSheet.getHeight());
        TextureRegion[] walkFramesDer = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            walkFramesDer[i] = tmpDer[0][i];
        }
        camDer = new Animation<>(0.15f, walkFramesDer);

        TextureRegion[][] tmpIzq = TextureRegion.split(camIzqSheet, camIzqSheet.getWidth() / 4, camIzqSheet.getHeight());
        TextureRegion[] walkFramesIzq = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            walkFramesIzq[i] = tmpIzq[0][i];
        }
        camIzq = new Animation<>(0.15f, walkFramesIzq);

        currentFrame = camDer.getKeyFrame(0);
    }

    public void applyPowerUp(PowerUpType type) {
        System.out.println("Aplicando power-up: " + type);
        activePowerUp = type;

        switch (type) {
            case SPEED:
                speed = baseSpeed * 1.5f;
                powerUpTimer = 15;
                break;
            case DOUBLE_BOMB:
                maxBombs = 2;
                powerUpTimer = 15;
                break;
            case IMMUNITY:
                immune = true;
                powerUpTimer = 10;
                break;
        }
    }

    private void removeActivePowerUp() {
        switch (activePowerUp) {
            case SPEED:
                speed = baseSpeed;
                break;
            case DOUBLE_BOMB:
                maxBombs = 1;
                break;
            case IMMUNITY:
                immune = false;
                break;
        }
        activePowerUp = PowerUpType.NONE;
        System.out.println("Power-up expirado");
    }

    public void update(float delta, java.util.List<Bomb> bombs) {
        float moveSpeed = speed * delta;
        float newX = x;
        float newY = y;
        boolean moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            newX -= moveSpeed;
            moving = true;
            facingRight = false;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            newX += moveSpeed;
            moving = true;
            facingRight = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            newY += moveSpeed;
            moving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            newY -= moveSpeed;
            moving = true;
        }

        int currentTileX = (int)(x / GameMap.TILE_SIZE);
        int currentTileY = (int)(y / GameMap.TILE_SIZE);
        if (currentTileX != ignoreBombTileX || currentTileY != ignoreBombTileY) {
            ignoreBombTileX = -1;
            ignoreBombTileY = -1;
        }

        if (canMoveTo(newX, y, bombs)) x = newX;
        if (canMoveTo(x, newY, bombs)) y = newY;

        if (moving) {
            stateTime += delta;
        } else {
            stateTime = 0;
        }

        currentFrame = facingRight ? camDer.getKeyFrame(stateTime, true) : camIzq.getKeyFrame(stateTime, true);

        if (powerUpTimer > 0) {
            powerUpTimer -= delta;
            if (powerUpTimer <= 0) {
                removeActivePowerUp();
            }
        }
    }

    private boolean canMoveTo(float newX, float newY, java.util.List<Bomb> bombs) {
        float radius = SIZE / 2f;
        float left = newX - radius;
        float right = newX + radius;
        float bottom = newY - radius;
        float top = newY + radius;

        int nextTileX = (int)(newX / GameMap.TILE_SIZE);
        int nextTileY = (int)(newY / GameMap.TILE_SIZE);

        for (Bomb bomb : bombs) {
            if (bomb.getTileX() == nextTileX && bomb.getTileY() == nextTileY) {
                if (!(bomb.getTileX() == ignoreBombTileX && bomb.getTileY() == ignoreBombTileY)) {
                    return false;
                }
            }
        }

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

    public void render(float offsetX, float offsetY, SpriteBatch batch) {
        batch.begin();
        batch.draw(currentFrame, x + offsetX - SIZE / 2f, y + offsetY - SIZE / 2f, SIZE, SIZE);
        batch.end();
    }

    public void setIgnoreBombTile(int tileX, int tileY) {
        this.ignoreBombTileX = tileX;
        this.ignoreBombTileY = tileY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isImmune() {
        return immune;
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public void setOffsets(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void dispose() {
        shapeRenderer.dispose();
        camDerSheet.dispose();
        camIzqSheet.dispose();
    }
}
