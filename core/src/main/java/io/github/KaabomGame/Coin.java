package io.github.KaabomGame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Coin {
    private static final float SIZE = GameMap.TILE_SIZE; // Tamaño de la moneda igual al de los bloques del mapa
    private float x, y;
    boolean revealed = false; // Indica si la moneda está visible
    private Texture texture;

    public Coin(int x, int y) {
        this.x = x * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f; // Posicion en el centro de la celda
        this.y = y * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f;
        this.texture = new Texture("moneda.png"); // Cargar la imagen de la moneda
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void reveal() {
        this.revealed = true;
    }

    // Método para renderizar la moneda
    public void render(float offsetX, float offsetY) {
        if (revealed) {  // Solo se dibuja la moneda si está revelada
            SpriteBatch batch = new SpriteBatch();
            batch.begin();
            batch.draw(texture, x + offsetX - SIZE / 2f, y + offsetY - SIZE / 2f, SIZE, SIZE); // Dibuja la moneda
            batch.end();
        }
    }

    public void dispose() {
        texture.dispose(); // Liberar recursos cuando ya no se necesite
    }
}
