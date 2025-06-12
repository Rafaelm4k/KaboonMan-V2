package io.github.KaabomGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MapRenderer {

    private Texture solidTexture;  // Textura para los bloques sólidos
    private Texture destructibleTexture; // Textura para los bloques destructibles

    public MapRenderer() {
        // Cargamos las texturas para los bloques
        solidTexture = new Texture("solido.png");  // Cargar textura de bloque sólido
        destructibleTexture = new Texture("destructibles.png"); // Cargar textura de bloque destructible
    }

    public void render(float offsetX, float offsetY, SpriteBatch batch) {
        // Recorrer el mapa y dibujar cada celda
        for (int y = 0; y < GameMap.MAP.length; y++) {
            for (int x = 0; x < GameMap.MAP[0].length; x++) {
                // Obtener el tipo de bloque (0, 1, 2, etc)
                int tile = GameMap.MAP[GameMap.mapY(y)][x];

                // Si el bloque es sólido (por ejemplo, representado con 1 en el mapa)
                if (tile == GameMap.SOLID_BLOCK) {
                    // Dibujar el bloque sólido en la posición correspondiente
                    float drawX = x * GameMap.TILE_SIZE + offsetX;
                    float drawY = y * GameMap.TILE_SIZE + offsetY;

                    // Dibujamos la textura del bloque sólido
                    batch.begin();
                    batch.draw(solidTexture, drawX, drawY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
                    batch.end();
                }

                // Si el bloque es destructible (por ejemplo, representado con 2 en el mapa)
                if (tile == GameMap.DESTRUCTIBLE_BLOCK) {
                    // Dibujar el bloque destructible en la posición correspondiente
                    float drawX = x * GameMap.TILE_SIZE + offsetX;
                    float drawY = y * GameMap.TILE_SIZE + offsetY;

                    // Dibujamos la textura del bloque destructible
                    batch.begin();
                    batch.draw(destructibleTexture, drawX, drawY, GameMap.TILE_SIZE, GameMap.TILE_SIZE);
                    batch.end();
                }
            }
        }
    }

    // Método para liberar recursos
    public void dispose() {
        // Liberar las texturas cuando ya no se necesiten
        solidTexture.dispose();
        destructibleTexture.dispose();
    }
}
