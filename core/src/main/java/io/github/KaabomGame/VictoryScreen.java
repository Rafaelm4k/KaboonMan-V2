package io.github.KaabomGame;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.Input;

public class VictoryScreen implements Screen {
    final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;
    private GlyphLayout layout;

    public VictoryScreen(final Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Main.V_WIDTH, Main.V_HEIGHT, camera);
        viewport.apply();
        font = new BitmapFont();
        font.getData().setScale(3f);
        layout = new GlyphLayout();
    }

    @Override
    public void show() {
        // Nada especial aquí
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.4f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // Mensaje principal: "¡Victoria!"
        String victoryText = "¡Victoria!";
        layout.setText(font, victoryText);
        float victoryX = (Main.V_WIDTH - layout.width) / 2;
        float victoryY = Main.V_HEIGHT / 2 + 60;
        font.draw(game.batch, victoryText, victoryX, victoryY);

        // Mensaje secundario: "Has recogido 15 monedas."
        String coinsText = "Has recogido 15 monedas.";
        font.getData().setScale(2f); // Un poco más pequeño
        layout.setText(font, coinsText);
        float coinsX = (Main.V_WIDTH - layout.width) / 2;
        float coinsY = Main.V_HEIGHT / 2 + 10;
        font.draw(game.batch, coinsText, coinsX, coinsY);

        // Mensaje instrucción: "Presiona ENTER para volver al menú."
        String instructionText = "Presiona ENTER para volver al menú.";
        layout.setText(font, instructionText);
        float instructionX = (Main.V_WIDTH - layout.width) / 2;
        float instructionY = Main.V_HEIGHT / 2 - 30;
        font.draw(game.batch, instructionText, instructionX, instructionY);

        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new MenuScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        font.dispose();
    }
}
