package ru.itis.tanki.common;


import ru.itis.tanki.model.Bullet;
import ru.itis.tanki.model.Tank;

import java.awt.event.KeyListener;
import java.util.List;

public interface Paintable {

    Boolean isServer();

    List<Tank> getTanks();

    List<Bullet> getBullets();

    GameMap getMap();

    Boolean getGameOver();

    String getWinnerName();

    KeyListener getInputHandler();

    void showStatistics();
}