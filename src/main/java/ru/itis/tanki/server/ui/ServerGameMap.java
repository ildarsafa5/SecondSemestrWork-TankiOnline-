package ru.itis.tanki.server.ui;


import ru.itis.tanki.common.GameMap;

import java.util.Random;

public class ServerGameMap implements GameMap {

    public static final int TILE_GRASS = 0;
    public static final int TILE_STONE = 1;
    public static final int TILE_BRICK = 2;
    public static final int TILE_WATER = 3;
    public static final int TILE_SAND = 4;

    private int[][] tiles;
    private int width = 21;
    private int height = 12;
    private int tileSize = 61;

    public ServerGameMap() {
        tiles = new int[width][height];
        generateMap();
    }

    private void generateMap() {
        Random rand = new Random(42);

        // всё травой
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[x][y] = TILE_GRASS;
            }
        }

        // каменная рамка по краям (неразрушаемая)
        for (int x = 0; x < width; x++) {
            tiles[x][0] = TILE_STONE; // верх
            tiles[x][height-1] = TILE_STONE; // низ
        }
        for (int y = 0; y < height; y++) {
            tiles[0][y] = TILE_STONE; // лево
            tiles[width-1][y] = TILE_STONE; // право
        }

        // центральная кирпичная стена (разрушаемая)
        for (int x = width/2 - 2; x <= width/2 + 2; x++) {
            for (int y = height/2 - 1; y <= height/2 + 1; y++) {
                tiles[x][y] = TILE_BRICK;
            }
        }
        // проход посередине
        tiles[width/2][height/2] = TILE_GRASS;

        // кирпичные укрытия по углам
        for (int x = 2; x <= 4; x++) {
            for (int y = 2; y <= 4; y++) {
                tiles[x][y] = TILE_BRICK;
            }
        }
        tiles[3][3] = TILE_GRASS; // место для танка

        for (int x = width-5; x <= width-3; x++) {
            for (int y = 2; y <= 4; y++) {
                tiles[x][y] = TILE_BRICK;
            }
        }
        tiles[width-4][3] = TILE_GRASS;

        for (int x = 2; x <= 4; x++) {
            for (int y = height-5; y <= height-3; y++) {
                tiles[x][y] = TILE_BRICK;
            }
        }
        tiles[3][height-4] = TILE_GRASS;

        for (int x = width-5; x <= width-3; x++) {
            for (int y = height-5; y <= height-3; y++) {
                tiles[x][y] = TILE_GRASS;
            }
        }
        tiles[width-4][height-4] = TILE_GRASS;

        // несколько случайных кирпичей
        for (int i = 0; i < 10; i++) {
            int x = 3 + rand.nextInt(width-6);
            int y = 3 + rand.nextInt(height-6);
            if (tiles[x][y] == TILE_GRASS) {
                tiles[x][y] = TILE_BRICK;
            }
        }

        // река посередине
        for (int x = 0; x < width; x++) {
            if (x != width/2 && x != width/2 - 1 && x != width/2 + 1) {
                tiles[x][height/3] = TILE_WATER;
                tiles[x][2*height/3] = TILE_WATER;
            }
        }

        // мосты через реку
        for (int x = width/4 - 1; x <= width/4 + 1; x++) {
            tiles[x][height/3] = TILE_SAND;
            tiles[x][2*height/3] = TILE_SAND;
        }
        for (int x = 3*width/4 - 1; x <= 3*width/4 + 1; x++) {
            tiles[x][height/3] = TILE_SAND;
            tiles[x][2*height/3] = TILE_SAND;
        }
    }



    public boolean isPassableForTank(int tileX, int tileY) {
        int tile = getTile(tileX, tileY);
        return tile == TILE_GRASS || tile == TILE_SAND;
    }

    public boolean isBulletBlocked(int tileX, int tileY) {
        int tile = getTile(tileX, tileY);
        return tile == TILE_STONE || tile == TILE_BRICK;
    }

    public boolean destroyBrick(int tileX, int tileY) {
        if (tileX >= 0 && tileX < width && tileY >= 0 && tileY < height) {
            if (tiles[tileX][tileY] == TILE_BRICK) {
                tiles[tileX][tileY] = TILE_GRASS; // превращаем в траву
                return true;
            }
        }
        return false;
    }

    public boolean checkTankCollision(float tankX, float tankY, float tankWidth, float tankHeight) {
        // Преобразуем координаты танка в тайлы
        float halfWidth = tankWidth / 2;
        float halfHeight = tankHeight / 2;


        int[][] checkPoints = {
                {(int)((tankX - halfWidth) / tileSize), (int)((tankY - halfHeight) / tileSize)},
                {(int)((tankX + halfWidth) / tileSize), (int)((tankY - halfHeight) / tileSize)},
                {(int)((tankX - halfWidth) / tileSize), (int)((tankY + halfHeight) / tileSize)},
                {(int)((tankX + halfWidth) / tileSize), (int)((tankY + halfHeight) / tileSize)},
                {(int)(tankX / tileSize), (int)(tankY / tileSize)}
        };

        for (int[] point : checkPoints) {
            int tileX = point[0];
            int tileY = point[1];

            if (!isPassableForTank(tileX, tileY)) {
                return true; // столкновение
            }
        }

        return false;
    }


    public int getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return -1;
    }

    public int getWidth() {
        return width * tileSize;
    }

    public int getHeight() {
        return height * tileSize;    }

    public int getWidthInTiles() {
        return width;
    }

    public int getHeightInTiles() {
        return height;
    }

    public int getTileSize() {
        return tileSize;
    }
}