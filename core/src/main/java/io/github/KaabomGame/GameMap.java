package io.github.KaabomGame;

public class GameMap {
    public static final int TILE_SIZE = 32;

    // Tipos de tile
    public static final int EMPTY = 0;
    public static final int SOLID_BLOCK = 1;
    public static final int DESTRUCTIBLE_BLOCK = 2;

    // Mapa 13x11
    public static final int[][] MAP = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,2,0,2,0,2,0,2,0,1},  // Fila 1 con (2,1) libre
        {1,0,1,2,1,2,1,2,1,2,1,2,1},  // Fila 2 con (1,2) libre
        {1,0,2,0,2,0,2,0,2,0,2,0,1},
        {1,2,1,2,1,2,1,2,1,2,1,2,1},
        {1,0,2,0,2,0,0,0,2,0,2,0,1},
        {1,2,1,2,1,2,1,2,1,2,1,2,1},
        {1,0,2,0,2,0,2,0,2,0,2,0,1},
        {1,0,1,2,1,2,1,2,1,2,1,2,1},
        {1,0,0,0,2,0,2,0,2,0,2,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1},
    };

    public static int mapY(int y) {
        return MAP.length - 1 - y;
    }

}

