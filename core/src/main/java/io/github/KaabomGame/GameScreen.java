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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

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
    private Music gameMusic;
    private Sound bombPlaceSound;
    private Sound explosionSound;
    private Sound enemyDieSound;
    private float damageCooldown = 0f;
    private ArrayList<PowerUp> powerUps = new ArrayList<>();





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
            6 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            5 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f
        ));
        enemies.add(new Enemy(
            7 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f
        ));
        enemies.add(new Enemy(
            13 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f
        ));
        enemies.add(new Enemy(
            13 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            9 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f
        ));
        enemies.add(new Enemy(
            1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            9 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f
        ));
        enemies.add(new Enemy(
            10 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            13 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f
        ));


        // Por ejemplo
        player = new Player();// Inicializa el player

        // Cargar y reproducir música de fondo
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("music/MusicInGame.ogg"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.2f); // Puedes ajustar el volumen (0.0 a 1.0)
        gameMusic.play();


        // Genera las monedas aleatoriamente sobre bloques destructibles
        generateCoins();

        // Genera PowerUps
        generatePowerUps();


        // Cargar sonidos
        bombPlaceSound = Gdx.audio.newSound(Gdx.files.internal("sounds/PlaceBomb.ogg"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Explosion.ogg"));
        enemyDieSound = Gdx.audio.newSound(Gdx.files.internal("sounds/EnemyDie.ogg"));


    }

    private void generatePowerUps() {
        // Ubicaciones fijas (x, y)
        int[][] fixedPositions = {
            {5, 3}, {5, 10}, {10,11}, {17, 9},
            {10, 7}, {12, 5}, {16, 7}, {1, 7},
            {2, 1}, {7, 9}, {13, 3}, {11, 11}
        };

        for (int i = 0; i < 12; i++) {
            PowerUp.Type type;
            if (i < 4) type = PowerUp.Type.SPEED;
            else if (i < 8) type = PowerUp.Type.DOUBLE_BOMB;
            else type = PowerUp.Type.INVINCIBILITY;

            powerUps.add(new PowerUp(fixedPositions[i][0], fixedPositions[i][1], type));
        }
    }



    // Método para generar monedas sobre bloques destructibles
    private void generateCoins() {
        // Vamos a generar 5 monedas
        int coinsGenerated = 0;
        while (coinsGenerated < 15) {
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
        if (damageCooldown > 0) {
            damageCooldown -= delta;
        }

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
            checkPlayerEnemyCollision();


            for (Enemy enemy : enemies) {
                enemy.update(delta, player, bombs);
            }

            if (hud.isGameOver()) {
                gameMusic.stop();
                game.setScreen(new GameOverScreen(game));
                dispose();
                return;
            }
        }

        for (Enemy enemy : enemies) {
            enemy.render(offsetX, offsetY);
        }


        // Renderizamos el mapa primero
        mapRenderer.render(offsetX, offsetY, game.batch);

        // Luego el jugador
        player.render(offsetX, offsetY, game.batch);

        // Dibujamos las bombas
        Iterator<Bomb> iter = bombs.iterator();
        while (iter.hasNext()) {
            Bomb bomb = iter.next();
            bomb.update(delta);
            if (bomb.hasExploded()) {
                iter.remove();
                bomb.dispose();
                explosionSound.play(0.7f);
                // Aquí podemos marcar las monedas que se han revelado
                revealCoinsAffectedByExplosion(bomb);
                killEnemiesInExplosion(bomb);
                damagePlayerIfInExplosion(bomb);
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


        // recolectar powerUps
        collectPowerUps();


        game.batch.begin();

        for (PowerUp powerUp : powerUps) {
            powerUp.update(player);
            powerUp.render(game.batch, offsetX, offsetY);
        }

        game.batch.end();



        // Finalmente la HUD
        hud.render(game.batch);

        if (hud.getScore() >= 15) {
            gameMusic.stop();
            game.setScreen(new VictoryScreen(game));
            dispose();
            return;
        }

        if (paused) {
            drawPauseMenu();
        }


    }

    private void checkPlayerEnemyCollision() {
        if (damageCooldown > 0) {
            damageCooldown -= Gdx.graphics.getDeltaTime();
            return;
        }

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float dx = Math.abs(player.getX() - enemy.getX());
            float dy = Math.abs(player.getY() - enemy.getY());

            if (dx < GameMap.TILE_SIZE / 2 && dy < GameMap.TILE_SIZE / 2) {
                hud.loseLife();
                damageCooldown = 2f; // 2 segundos de invulnerabilidad

                if (hud.isGameOver()) {
                    // Mostrar Game Over o regresar al menú
                    game.setScreen(new MenuScreen(game)); // puedes cambiar esto a una pantalla de Game Over si quieres
                }

                break;
            }
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
                    enemyDieSound.play(0.7f);
                    break;
                }
            }
        }
    }

    private void collectPowerUps() {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            if (isPlayerOnPowerUp(powerUp)) {
                player.applyPowerUp(powerUp.getAsPlayerType());
                iterator.remove(); // Lo elimina del mapa
                powerUp.dispose(); // Liberá la textura si tenés
            }
        }
    }

    private boolean isPlayerOnPowerUp(PowerUp powerUp) {
        float playerX = player.getX();
        float playerY = player.getY();
        float puX = powerUp.getX();
        float puY = powerUp.getY();

        return Math.abs(playerX - puX) < GameMap.TILE_SIZE / 2 &&
            Math.abs(playerY - puY) < GameMap.TILE_SIZE / 2;
    }


    private void damagePlayerIfInExplosion(Bomb bomb) {
        List<int[]> affectedTiles = bomb.getAffectedTiles();

        int playerTileX = (int)(player.getX() / GameMap.TILE_SIZE);
        int playerTileY = (int)(player.getY() / GameMap.TILE_SIZE);

        for (int[] tile : affectedTiles) {
            if (tile[0] == playerTileX && tile[1] == playerTileY) {
                if (damageCooldown <= 0) {
                    hud.loseLife();
                    damageCooldown = 2f;

                    if (hud.isGameOver()) {
                        gameMusic.stop();
                        game.setScreen(new GameOverScreen(game));
                        dispose();
                        return;
                    }
                }
                break;
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
            bombPlaceSound.play(0.7f); // Volumen de 0.0 a 1.0
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

        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.dispose();
        }

        if (bombPlaceSound != null) bombPlaceSound.dispose();
        if (explosionSound != null) explosionSound.dispose();
        if (enemyDieSound != null) enemyDieSound.dispose();


        // powerUps
        for (PowerUp powerUp : powerUps) {
            powerUp.dispose();
        }


    }
}


