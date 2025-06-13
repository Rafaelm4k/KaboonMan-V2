package io.github.KaabomGame;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class GameOverScreen implements Screen {
    final Main game;
    private BitmapFont font;
    private GlyphLayout layout;

    public GameOverScreen(final Main game) {
        this.game = game;
        font = new BitmapFont();
        font.getData().setScale(3); // Tamaño de la fuente grande
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        String gameOverText = "GAME OVER";
        String instructionText = "Pulsa ENTER para volver al menú";

        // Centrar "GAME OVER"
        layout.setText(font, gameOverText);
        float gameOverX = (Main.V_WIDTH - layout.width) / 2;
        float gameOverY = Main.V_HEIGHT / 2 + 40;
        font.draw(game.batch, gameOverText, gameOverX, gameOverY);

        // Centrar la instrucción
        font.getData().setScale(2); // Hacemos la instrucción un poco más pequeña
        layout.setText(font, instructionText);
        float instructionX = (Main.V_WIDTH - layout.width) / 2;
        float instructionY = Main.V_HEIGHT / 2 - 20;
        font.draw(game.batch, instructionText, instructionX, instructionY);

        game.batch.end();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override
    public void dispose() {
        font.dispose();
    }
}
