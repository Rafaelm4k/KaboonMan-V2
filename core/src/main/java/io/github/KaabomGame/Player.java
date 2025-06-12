package io.github.KaabomGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Player {
    private static final int SIZE = 28;
    private float x, y;
    private ShapeRenderer shapeRenderer;
    private int ignoreBombTileX = -1;
    private int ignoreBombTileY = -1;
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

    public void update(float delta, java.util.List<Bomb> bombs) {
        float moveSpeed = 110 * delta;
        float newX = x;
        float newY = y;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) newX -= moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) newX += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) newY += moveSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) newY -= moveSpeed;

        int currentTileX = (int)(x / GameMap.TILE_SIZE);
        int currentTileY = (int)(y / GameMap.TILE_SIZE);

        // Si el jugador ya salió del tile donde puso la bomba, ya no ignorar
        if (currentTileX != ignoreBombTileX || currentTileY != ignoreBombTileY) {
            ignoreBombTileX = -1;
            ignoreBombTileY = -1;
        }

        if (canMoveTo(newX, y, bombs)) x = newX;
        if (canMoveTo(x, newY, bombs)) y = newY;
    }




    public void render(float offsetX, float offsetY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(x + offsetX, y + offsetY, SIZE / 2f);
        shapeRenderer.end();
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
                // Solo ignorar si es la bomba que acabas de poner
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

    public void dispose() {
        shapeRenderer.dispose();
    }


}







