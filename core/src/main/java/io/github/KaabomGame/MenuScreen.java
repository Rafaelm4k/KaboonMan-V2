package io.github.KaabomGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Music;// <-- IMPORTANTE

public class MenuScreen implements Screen {

    final Main game;
    private BitmapFont font;
    private GlyphLayout layout;
    private int selectedOption = 0;
    private final String[] options = {"Iniciar partida", "Salir"};
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture background;
    private Music menuMusic;// <-- NUEVO

    public MenuScreen(final Main game) {
        this.game = game;
        font = new BitmapFont();
        layout = new GlyphLayout();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Main.V_WIDTH, Main.V_HEIGHT, camera);
        viewport.apply();

        background = new Texture(Gdx.files.internal("FondoMenu.png"));

        // Carga y reproduce la música
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/MusicMenu.ogg"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.2f); // Puedes ajustar el volumen
        menuMusic.play();
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        game.batch.draw(background, 0, 0, Main.V_WIDTH, Main.V_HEIGHT);

        for (int i = 0; i < options.length; i++) {
            String text = options[i];
            layout.setText(font, text);
            float x = (Main.V_WIDTH - layout.width) / 2;
            float y = Main.V_HEIGHT / 2 + 30 * (options.length - i);

            font.setColor(i == selectedOption ? 1 : 1, i == selectedOption ? 1 : 1, i == selectedOption ? 0 : 1, 1);
            font.draw(game.batch, text, x, y);
        }

        game.batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption = (selectedOption + 1) % options.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption = (selectedOption - 1 + options.length) % options.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (selectedOption == 0) {
                menuMusic.stop();      // Detener música al iniciar juego
                game.setScreen(new GameScreen(game));
            } else if (selectedOption == 1) {
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void show() {}
    @Override public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        font.dispose();
        background.dispose();
        menuMusic.dispose(); // <-- Liberar recurso de música
    }
}
