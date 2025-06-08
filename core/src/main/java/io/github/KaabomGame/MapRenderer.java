package io.github.KaabomGame;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class MapRenderer {
    private final ShapeRenderer shapeRenderer;

    public MapRenderer() {
        shapeRenderer = new ShapeRenderer();
    }

    public void render(float offsetX, float offsetY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int row = 0; row < GameMap.MAP.length; row++) {
            for (int col = 0; col < GameMap.MAP[0].length; col++) {
                int tile = GameMap.MAP[row][col];
                float x = offsetX + col * GameMap.TILE_SIZE;
                float y = offsetY + (GameMap.MAP.length - 1 - row) * GameMap.TILE_SIZE;

                switch (tile) {
                    case GameMap.SOLID_BLOCK:
                        shapeRenderer.setColor(Color.DARK_GRAY);
                        break;
                    case GameMap.DESTRUCTIBLE_BLOCK:
                        shapeRenderer.setColor(new Color(0.55f, 0.27f, 0.07f, 1));
                        break;
                    case GameMap.EMPTY:
                        shapeRenderer.setColor(Color.BLACK);
                        break;
                }

                shapeRenderer.rect(x, y, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
            }
        }

        shapeRenderer.end();
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}


