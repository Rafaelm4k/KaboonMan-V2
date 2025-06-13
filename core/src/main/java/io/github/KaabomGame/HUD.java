package io.github.KaabomGame;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

public class HUD {

    private int score;
    private int lives;
    private float timeRemaining; // en segundos
    private final BitmapFont font;

    public HUD() {
        this.score = 0;
        this.lives = 3;
        this.timeRemaining = 240; // 3 minutos por ejemplo
        this.font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2);
    }

    public void update(float delta) {
        timeRemaining -= delta;
        if (timeRemaining < 0) timeRemaining = 0;
    }

    public void render(SpriteBatch batch) {
        batch.begin();
        font.draw(batch, "Puntaje: " + score, 20, Main.V_HEIGHT - 20);
        font.draw(batch, "Vidas: " + lives, 20, Main.V_HEIGHT - 60);
        font.draw(batch, "Tiempo: " + (int)timeRemaining, Main.V_WIDTH - 200, Main.V_HEIGHT - 20);
        batch.end();
    }

    // Método que aumenta la puntuación del jugador cuando recoge una moneda
    public void addScore(int value) {
        this.score += value;
    }

    // Nuevo método para obtener el puntaje actual
    public int getScore() {
        return score;
    }

    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }

    public boolean isTimeUp() {
        return timeRemaining <= 0;
    }

    public boolean isGameOver() {
        return lives <= 0 || isTimeUp();
    }

    public void dispose() {
        font.dispose();
    }
}
