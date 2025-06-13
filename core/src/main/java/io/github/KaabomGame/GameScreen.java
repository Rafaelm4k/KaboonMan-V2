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
    private ArrayList<Coin> coins = new ArrayList<>();
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

    public GameScreen(final Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Main.V_WIDTH, Main.V_HEIGHT, camera);
        viewport.apply();
        pauseFont = new BitmapFont();
        pauseFont.getData().setScale(2);
        mapRenderer = new MapRenderer();
        enemies = new ArrayList<>();
        enemies.add(new Enemy(6 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            5 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f));
        enemies.add(new Enemy(7 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f));
        enemies.add(new Enemy(13 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f,
            1 * GameMap.TILE_SIZE + GameMap.TILE_SIZE / 2f));
        player = new Player();

        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("music/MusicInGame.ogg"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.2f);
        gameMusic.play();

        generateCoins();

        bombPlaceSound = Gdx.audio.newSound(Gdx.files.internal("sounds/PlaceBomb.ogg"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Explosion.ogg"));
        enemyDieSound = Gdx.audio.newSound(Gdx.files.internal("sounds/EnemyDie.ogg"));
    }

    private void generateCoins() {
        int coinsGenerated = 0;
        while (coinsGenerated < 15) {
            int x = (int) (Math.random() * GameMap.MAP[0].length);
            int y = (int) (Math.random() * GameMap.MAP.length);

            if (GameMap.MAP[GameMap.mapY(y)][x] == GameMap.DESTRUCTIBLE_BLOCK) {
                coins.add(new Coin(x, y));
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

        mapRenderer.render(offsetX, offsetY, game.batch);
        player.render(offsetX, offsetY, game.batch);

        Iterator<Bomb> iter = bombs.iterator();
        while (iter.hasNext()) {
            Bomb bomb = iter.next();
            bomb.update(delta);
            if (bomb.hasExploded()) {
                iter.remove();
                bomb.dispose();
                explosionSound.play(0.7f);
                revealCoinsAffectedByExplosion(bomb);
                killEnemiesInExplosion(bomb);
                damagePlayerIfInExplosion(bomb);
            }
        }

        for (Bomb bomb : bombs) {
            bomb.render(offsetX, offsetY);
        }

        for (Coin coin : coins) {
            if (coinIsRevealed(coin)) {
                coin.render(offsetX, offsetY);
            }
        }

        collectCoins();
        hud.render(game.batch);

        if (paused) {
            drawPauseMenu();
        }
    }

    private void checkPlayerEnemyCollision() {
        if (damageCooldown > 0) return;

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float dx = Math.abs(player.getX() - enemy.getX());
            float dy = Math.abs(player.getY() - enemy.getY());

            if (dx < GameMap.TILE_SIZE / 2 && dy < GameMap.TILE_SIZE / 2) {
                hud.loseLife();
                damageCooldown = 2f;

                if (hud.isGameOver()) {
                    gameMusic.stop();
                    game.setScreen(new GameOverScreen(game));
                    dispose();
                    return;
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
                    paused = false;
                } else if (pauseOption == 1) {
                    game.setScreen(new MenuScreen(game));
                }
            }
        }
    }

    private void placeBomb() {
        int tileX = (int)(player.getX() / GameMap.TILE_SIZE);
        int tileY = (int)(player.getY() / GameMap.TILE_SIZE);

        if (bombs.size() < 1) {
            bombs.add(new Bomb(tileX, tileY));
            player.setIgnoreBombTile(tileX, tileY);
            bombPlaceSound.play(0.7f);
        }
    }

    private void revealCoinsAffectedByExplosion(Bomb bomb) {
        revealCoinIfExploded(bomb.getTileX(), bomb.getTileY());
        revealCoinIfExploded(bomb.getTileX() - 1, bomb.getTileY());
        revealCoinIfExploded(bomb.getTileX() + 1, bomb.getTileY());
        revealCoinIfExploded(bomb.getTileX(), bomb.getTileY() + 1);
        revealCoinIfExploded(bomb.getTileX(), bomb.getTileY() - 1);
    }

    private void revealCoinIfExploded(int x, int y) {
        for (Coin coin : coins) {
            int coinX = (int) (coin.getX() / GameMap.TILE_SIZE);
            int coinY = (int) (coin.getY() / GameMap.TILE_SIZE);
            if (coinX == x && coinY == y) {
                coin.reveal();
            }
        }
    }

    private boolean coinIsRevealed(Coin coin) {
        return coin.revealed;
    }

    private void collectCoins() {
        Iterator<Coin> coinIterator = coins.iterator();
        while (coinIterator.hasNext()) {
            Coin coin = coinIterator.next();
            if (isPlayerOnCoin(coin)) {
                coinIterator.remove();
                hud.addScore(1);
            }
        }
    }

    private boolean isPlayerOnCoin(Coin coin) {
        float playerX = player.getX();
        float playerY = player.getY();
        float coinX = coin.getX();
        float coinY = coin.getY();

        return Math.abs(playerX - coinX) < GameMap.TILE_SIZE / 2 && Math.abs(playerY - coinY) < GameMap.TILE_SIZE / 2;
    }

    private void drawPauseMenu() {
        game.batch.begin();

        pauseFont.setColor(1, 1, 1, 1);
        pauseFont.draw(game.batch, "PAUSA", Main.V_WIDTH / 2f - 50, Main.V_HEIGHT / 2f + 60);

        for (int i = 0; i < pauseOptions.length; i++) {
            if (i == pauseOption) {
                pauseFont.setColor(1, 1, 0, 1);
            } else {
                pauseFont.setColor(1, 1, 1, 1);
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
    @Override
    public void dispose() {
        mapRenderer.dispose();
        hud.dispose();
        pauseFont.dispose();

        for (Coin coin : coins) {
            coin.dispose();
        }

        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.dispose();
        }

        if (bombPlaceSound != null) bombPlaceSound.dispose();
        if (explosionSound != null) bombPlaceSound.dispose();
        if (enemyDieSound != null) enemyDieSound.dispose();
    }
}
