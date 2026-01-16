package ru.itis.tanki.client;


import ru.itis.tanki.client.ui.ClientStatisticsWindow;
import ru.itis.tanki.handler.GameInputHandler;
import ru.itis.tanki.common.Paintable;
import ru.itis.tanki.client.ui.ClientGameMap;
import ru.itis.tanki.client.ui.ConnectionWindow;
import ru.itis.tanki.message.Message;
import ru.itis.tanki.model.Bullet;
import ru.itis.tanki.model.Score;
import ru.itis.tanki.model.Tank;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameClient implements Paintable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private ClientGameMap map;

    private ClientStatisticsWindow statsWindow;


    private List<Tank> tanks = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private Boolean gameOver = false;
    private String winnerName;


    private GameInputHandler inputHandler;


    private boolean connected = false;

    private List<Score> killsStats = new ArrayList<>();
    private List<Score> winsStats = new ArrayList<>();


    public boolean connect(String serverIp, int port, String playerName) {
        try {
            System.out.println("Подключение к " + serverIp + ":" + port);

            socket = new Socket(serverIp, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            System.out.println("Соединение установлено");

            Message joinMessage = new Message(Message.JOIN, playerName);
            out.writeUTF(joinMessage.serialize());
            out.flush();

            Message resp = Message.deserialize(in.readUTF());
            if (resp == null || resp.getType() != Message.JOIN ||
                    !resp.getData().equals("OK")) {
                System.err.println("Сервер отверг подключение");
                socket.close();
                return false;
            }

            System.out.println("Сервер принял подключение");


            String mapMessage = in.readUTF();
            Message mapMsg = Message.deserialize(mapMessage);

            if (mapMsg.getType() == Message.MAP) {
                parseMap(mapMsg.getData());
                System.out.println("Карта получена");
            }


            String initialState = in.readUTF();
            Message msg = Message.deserialize(initialState);

            if (msg.getType() == Message.STATE) {
                parseGameState(msg.getData());
                System.out.println("Получено начальное состояние");

                inputHandler = new GameInputHandler(false);

                connected = true;

                startNetworkThread();

                return true;
            }

        } catch (Exception e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        }

        return false;
    }

    public void requestStatistics() {
        try {
            if (out != null) {
                Message request = new Message(Message.STATS_REQUEST, "");
                out.writeUTF(request.serialize());
                out.flush();
                System.out.println("Запрос статистики отправлен");
            }
        } catch (Exception e) {
            System.err.println("Ошибка запроса статистики: " + e.getMessage());
        }
    }

    private void parseMap(String mapData) {
        try {
            String[] parts = mapData.split(":", 2);
            if (parts.length != 2) return;

            String[] header = parts[0].split(",");
            int width = Integer.parseInt(header[0]);
            int height = Integer.parseInt(header[1]);
            int tileSize = Integer.parseInt(header[2]);

            String[] tileStrings = parts[1].split(",");
            int[][] tiles = new int[width][height];


            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (index < tileStrings.length) {
                        tiles[x][y] = Integer.parseInt(tileStrings[index++]);
                    }
                }
            }

            map = new ClientGameMap(width, height, tileSize, tiles);
            System.out.println("Карта создана");

        } catch (Exception e) {
            System.err.println("Ошибка парсинга карты: " + e.getMessage());
        }
    }


    private void startNetworkThread() {
        new Thread(() -> {
            try {
                while (connected) {
                    String received = in.readUTF();

                    Message msg = Message.deserialize(received);
                    switch (msg.getType()) {
                        case Message.STATE: parseGameState(msg.getData()); break;
                        case Message.MAP: parseMap(msg.getData()); break;
                        case Message.MAP_UPDATE: parseMapUpdate(msg.getData()); break;
                        case Message.STATS_RESPONSE: parseStatistics(msg.getData()); break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Сетевая ошибка: " + e.getMessage());
                disconnect();
            }
        }).start();

        // отдельный поток для отправки ввода
        new Thread(() -> {
            try {
                while (connected) {
                    sendInputUpdate();
                    //spaceJustPressed быстро меняется и чтобы сервер успел считать выстрел, отправляем данные с задержкой
                    Thread.sleep(16);
                }
            } catch (Exception e) {}
        }).start();
    }

    private void parseStatistics(String data) {
        try {
            // формат: "игрок1:убийства;игрок2:убийства...|игрок1:победы;игрок2:победы..."
            String[] parts = data.split("\\|");
            if (parts.length >= 2) {
                killsStats.clear();
                if (!parts[0].isEmpty()) {
                    String[] kills = parts[0].split(";");
                    for (String kill : kills) {
                        String[] playerData = kill.split(":");
                        if (playerData.length >= 2) {
                            killsStats.add(new Score(
                                    playerData[0],
                                    Integer.parseInt(playerData[1]),
                                    0
                            ));
                        }
                    }
                }

                winsStats.clear();
                if (!parts[1].isEmpty()) {
                    String[] wins = parts[1].split(";");
                    for (String win : wins) {
                        String[] playerData = win.split(":");
                        if (playerData.length >= 2) {
                            winsStats.add(new Score(
                                    playerData[0],
                                    0,
                                    Integer.parseInt(playerData[1])
                            ));
                        }
                    }
                }

                if (statsWindow != null) {
                    SwingUtilities.invokeLater(() -> {
                        statsWindow.updateStatistics(killsStats, winsStats);
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга статистики: " + e.getMessage());
        }
    }

    public void showStatistics() {
        SwingUtilities.invokeLater(() -> {
            if (statsWindow == null) {
                statsWindow = new ClientStatisticsWindow();
            }
            statsWindow.setVisible(true);

            requestStatistics();
        });
    }

    private void parseMapUpdate(String data) {
        try {
            String[] parts = data.split(":");
            if (parts.length >= 3) {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int tileType = Integer.parseInt(parts[2]);

                synchronized (map) {
                    if (map != null) {
                        map.setTile(x, y, tileType);
                    }
                }
                System.out.println("Карта обновлена: " + x + "," + y + " = " + tileType);
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга обновления карты: " + e.getMessage());
        }
    }

    private void parseGameState(String data) {
        try {
            // Формат: "танки|пули"
            String[] parts = data.split("\\|");
            if (parts.length >= 2) {
                parseTanks(parts[0]);
                parseBullets(parts[1]);
                gameOver = Boolean.parseBoolean(parts[2]);
                System.out.println("состояние игры " + gameOver);
                winnerName = parts[3];
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга состояния: " + e.getMessage());
        }
    }

    private void parseTanks(String tanksData) {
        List<Tank> newTanks = new ArrayList<>();

        if (tanksData == null || tanksData.isEmpty()) return;

        tanksData = tanksData.replace(',', '.');

        String[] tankStrings = tanksData.split(";");
        for (String tankStr : tankStrings) {
            if (tankStr.isEmpty()) continue;

            String[] parts = tankStr.split(":");
            if (parts.length >= 6) {
                try {
                    int id = Integer.parseInt(parts[0]);
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float angle = Float.parseFloat(parts[3]);
                    int health = Integer.parseInt(parts[4]);
                    boolean alive = Boolean.parseBoolean(parts[5]);
                    String name  = parts[6];
                    int kills = Integer.parseInt(parts[7]);
                    int deaths = Integer.parseInt(parts[8]);

                    Tank tank = new Tank(id, name, x, y);
                    tank.setAngle(angle);
                    tank.setHealth(health);
                    tank.setAlive(alive);
                    tank.setKills(kills);
                    tank.setDeaths(deaths);

                    newTanks.add(tank);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка парсинга танка: " + tankStr);
                }
            }
        }

            tanks.clear();
            tanks.addAll(newTanks);
    }


    private void parseBullets(String bulletsData) {
        List<Bullet> newBullets = new ArrayList<>();

        if (bulletsData == null || bulletsData.isEmpty()) return;

        bulletsData = bulletsData.replace(',', '.');

        String[] bulletStrings = bulletsData.split(";");
        for (String bulletStr : bulletStrings) {
            if (bulletStr.isEmpty()) continue;

            String[] parts = bulletStr.split(":");
            if (parts.length >= 5) {
                try {
                    float x = Float.parseFloat(parts[0]);
                    float y = Float.parseFloat(parts[1]);
                    float velX = Float.parseFloat(parts[2]);
                    float velY = Float.parseFloat(parts[3]);
                    int ownerId = Integer.parseInt(parts[4]);

                    newBullets.add(new Bullet(x, y, velX, velY, ownerId));
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка парсинга пули: " + bulletStr);
                }
            }
        }

            bullets.clear();
            bullets.addAll(newBullets);

    }

    private void sendInputUpdate() {
        if (!connected || out == null) return;

        try {
            String inputData = String.format("%d,%d,%d,%d,%d",
                    inputHandler.iswPressed() ? 1 : 0,
                    inputHandler.isaPressed() ? 1 : 0,
                    inputHandler.issPressed() ? 1 : 0,
                    inputHandler.isdPressed() ? 1 : 0,
                    inputHandler.isSpaceJustPressed() ? 1 : 0);

            Message inputMsg = new Message(Message.INPUT, inputData);
            out.writeUTF(inputMsg.serialize());
            out.flush();

            if (inputHandler.isSpaceJustPressed()) {
                inputHandler.setSpaceJustPressed(false);
            }

        } catch (Exception e) {
            System.err.println("Ошибка отправки ввода: " + e.getMessage());
        }
    }


    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
        }
    }


    public GameInputHandler getInputHandler() {
        return inputHandler;
    }

    public List<Tank> getTanks() {
            return tanks;
    }

    public List<Bullet> getBullets() {
            return bullets;
    }

    public ClientGameMap getMap() {
        return map;
    }

    public Boolean getGameOver() {
        return gameOver;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public Boolean isServer() {
        return false;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConnectionWindow window = new ConnectionWindow();
            window.setVisible(true);
        });
    }
}