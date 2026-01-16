package ru.itis.tanki.common;

public interface GameMap {

    int getWidthInTiles();

    int getHeightInTiles();

    int getTileSize();

    int getTile(int x, int y);
}
