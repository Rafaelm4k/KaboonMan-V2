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
import java.util.List;

public class GameScreen implements Screen {
    private ArrayList<Bomb> bombs = new ArrayList<>();
    private ArrayList<Enemy> enemies;
    private ArrayList<Coin> coins = new ArrayList<>(); // Lista de monedas
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
        enemies = new ArrayList<>();
        enemies.add(new Enemy(
            5 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            5 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f
        ));
        enemies.add(new Enemy(
            7 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f
        ));


        // Por ejemplo
        player = new Player(); // Inicializa el player

        // Genera las monedas aleatoriamente sobre bloques destructibles
        generateCoins();
    }

    // Método para generar monedas sobre bloques destructibles
    private void generateCoins() {
        // Vamos a generar 5 monedas
        int coinsGenerated = 0;
        while (coinsGenerated < 5) {
            int x = (int) (Math.random() * GameMap.MAP[0].length); // Obtener una columna aleatoria
            int y = (int) (Math.random() * GameMap.MAP.length);    // Obtener una fila aleatoria

            // Comprobar que haya un bloque destructible en esa posición
            if (GameMap.MAP[GameMap.mapY(y)][x] == GameMap.DESTRUCTIBLE_BLOCK) {
                coins.add(new Coin(x, y)); // Si es así, generar la moneda
                coinsGenerated++;
            }
        }
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
            player.update(delta, bombs);
            hud.update(delta);
            for (Enemy enemy : enemies) {
                enemy.update(delta, player, bombs);
            }
        }

        for (Enemy enemy : enemies) {
            enemy.render(offsetX, offsetY);
        }


        // Renderizamos el mapa primero
        mapRenderer.render(offsetX, offsetY, game.batch);

        // Luego el jugador
        player.render(offsetX, offsetY);

        // Dibujamos las bombas
        Iterator<Bomb> iter = bombs.iterator();
        while (iter.hasNext()) {
            Bomb bomb = iter.next();
            bomb.update(delta);
            if (bomb.hasExploded()) {
                iter.remove();
                bomb.dispose();
                // Aquí podemos marcar las monedas que se han revelado
                revealCoinsAffectedByExplosion(bomb);
                killEnemiesInExplosion(bomb);
            }
        }
        for (Bomb bomb : bombs) {
            bomb.render(offsetX, offsetY);
        }

        // Dibujamos las monedas solo si están reveladas
        for (Coin coin : coins) {
            if (coinIsRevealed(coin)) {
                coin.render(offsetX, offsetY);
            }
        }

        // Verificamos si el jugador ha recogido alguna moneda
        collectCoins();

        // Finalmente la HUD
        hud.render(game.batch);

        if (paused) {
            drawPauseMenu();
        }
    }

    private void killEnemiesInExplosion(Bomb bomb) {
        List<int[]> affectedTiles = bomb.getAffectedTiles();

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            int enemyTileX = (int)(enemy.getX() / GameMap.TILE_SIZE);
            int enemyTileY = (int)(enemy.getY() / GameMap.TILE_SIZE);

            for (int[] tile : affectedTiles) {
                if (tile[0] == enemyTileX && tile[1] == enemyTileY) {
                    enemy.kill();
                    break;
                }
            }
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
        int tileY = (int)(player.getY() / GameMap.TILE_SIZE);

        if (bombs.size() < 1) {
            bombs.add(new Bomb(tileX, tileY));
            player.setIgnoreBombTile(tileX, tileY); // <- MUY IMPORTANTE
        }
    }

    // Método para revelar las monedas afectadas por una explosión
    private void revealCoinsAffectedByExplosion(Bomb bomb) {
        // Aquí comprobamos las celdas afectadas por la explosión
        revealCoinIfExploded(bomb.getTileX(), bomb.getTileY()); // Centro
        revealCoinIfExploded(bomb.getTileX() - 1, bomb.getTileY()); // Izquierda
        revealCoinIfExploded(bomb.getTileX() + 1, bomb.getTileY()); // Derecha
        revealCoinIfExploded(bomb.getTileX(), bomb.getTileY() + 1); // Arriba
        revealCoinIfExploded(bomb.getTileX(), bomb.getTileY() - 1); // Abajo
    }

    // Verifica si la moneda está en la posición y si es oculta, la revela
    private void revealCoinIfExploded(int x, int y) {
        for (Coin coin : coins) {
            int coinX = (int) (coin.getX() / GameMap.TILE_SIZE);
            int coinY = (int) (coin.getY() / GameMap.TILE_SIZE);
            if (coinX == x && coinY == y) {
                coin.reveal(); // Revela la moneda
            }
        }
    }

    private boolean coinIsRevealed(Coin coin) {
        return coin.revealed; // Verifica si la moneda está revelada
    }

    // Método para comprobar si el jugador ha recogido una moneda
    private void collectCoins() {
        Iterator<Coin> coinIterator = coins.iterator();
        while (coinIterator.hasNext()) {
            Coin coin = coinIterator.next();
            if (isPlayerOnCoin(coin)) {
                coinIterator.remove(); // Eliminar la moneda de la lista
                hud.addScore(1); // Aumentar la puntuación del jugador
            }
        }
    }

    // Verifica si el jugador está sobre la moneda
    private boolean isPlayerOnCoin(Coin coin) {
        float playerX = player.getX();
        float playerY = player.getY();
        float coinX = coin.getX();
        float coinY = coin.getY();

        // Comprobar si el jugador está dentro de un rango cercano de la moneda
        return Math.abs(playerX - coinX) < GameMap.TILE_SIZE / 2 && Math.abs(playerY - coinY) < GameMap.TILE_SIZE / 2;
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

        // Dispose de las monedas
        for (Coin coin : coins) {
            coin.dispose();
        }
    }
}
