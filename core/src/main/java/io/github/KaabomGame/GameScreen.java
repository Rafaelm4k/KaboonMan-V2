package io.github.KaabomGame;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {
    private ArrayList<Bomb> bombs = new ArrayList<>();
    private HUD hud;
    final Main game;
    private OrthographicCamera camera;
    private Viewport viewport;
    private MapRenderer mapRenderer;
    private boolean paused = false;
    private int pauseOption = 0;
    private final String[] pauseOptions = {"Reanudar", "Salir al menú"};
    private BitmapFont pauseFont;
    private Player player;

    public GameScreen(final Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Main.V_WIDTH, Main.V_HEIGHT, camera);
        viewport.apply();
        pauseFont = new BitmapFont();
        pauseFont.getData().setScale(2);
        mapRenderer = new MapRenderer();

        player = new Player(); // Inicializa el player
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        int mapWidth = GameMap.MAP[0].length * GameMap.TILE_SIZE;
        int mapHeight = GameMap.MAP.length * GameMap.TILE_SIZE;

        float offsetX = (Main.V_WIDTH - mapWidth) / 2f;
        float offsetY = (Main.V_HEIGHT - mapHeight) / 2f;

        player.setOffsets(offsetX, offsetY);

        if (!paused) {
            player.update(delta);
            hud.update(delta);
        }

        // Renderizamos el mapa primero
        mapRenderer.render(offsetX, offsetY);

        // Luego el jugador
        player.render(offsetX, offsetY);

        Iterator<Bomb> iter = bombs.iterator();
        while (iter.hasNext()) {
            Bomb bomb = iter.next();
            bomb.update(delta);
            if (bomb.hasExploded()) {
                // Aquí luego pondremos la lógica para la explosión y borrar la bomba
                iter.remove();
                bomb.dispose();
            }
        }

        // Dibuja bombas
        for (Bomb bomb : bombs) {
            bomb.render(offsetX, offsetY);
        }

        // Finalmente la HUD
        hud.render(game.batch);

        if (paused) {
            drawPauseMenu();
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
        }

        if (!paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                placeBomb();
            }
        }

        if (paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                pauseOption = (pauseOption - 1 + pauseOptions.length) % pauseOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                pauseOption = (pauseOption + 1) % pauseOptions.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (pauseOption == 0) {
                    paused = false; // Reanudar
                } else if (pauseOption == 1) {
                    game.setScreen(new MenuScreen(game)); // Salir al menú
                }
            }
        }
    }


    private void placeBomb() {
        int tileX = (int)(player.getX() / GameMap.TILE_SIZE);
        int tileY = (int)(player.getY() / GameMap.TILE_SIZE); // <-- NO invertir

        if (bombs.size() < 1) {
            bombs.add(new Bomb(tileX, tileY));
        }
    }



    private void drawPauseMenu() {
        game.batch.begin();

        pauseFont.setColor(1, 1, 1, 1);
        pauseFont.draw(game.batch, "PAUSA", Main.V_WIDTH / 2f - 50, Main.V_HEIGHT / 2f + 60);

        for (int i = 0; i < pauseOptions.length; i++) {
            if (i == pauseOption) {
                pauseFont.setColor(1, 1, 0, 1); // Amarillo
            } else {
                pauseFont.setColor(1, 1, 1, 1); // Blanco
            }
            pauseFont.draw(game.batch, pauseOptions[i], Main.V_WIDTH / 2f - 80, Main.V_HEIGHT / 2f - i * 40);
        }

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {
        hud = new HUD();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        mapRenderer.dispose();
        hud.dispose();
        pauseFont.dispose();
    }
}



