package ru.itis.tanki.client.ui;


import ru.itis.tanki.common.GameMap;

public class ClientGameMap implements GameMap {
    private int[][] tiles;
    private int width;
    private int height;
    private int tileSize;

    public ClientGameMap(int width, int height, int tileSize, int[][] tiles) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.tiles = tiles;
    }

    public void setTile(int x, int y, int tileType) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x][y] = tileType;
        }
    }

    public int getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return -1;
    }

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