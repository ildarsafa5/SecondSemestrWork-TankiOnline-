package ru.itis.tanki.server;


import ru.itis.tanki.server.ui.ServerStatisticsWindow;
import ru.itis.tanki.db.ScoreRepository;
import ru.itis.tanki.handler.GameInputHandler;
import ru.itis.tanki.common.Paintable;
import ru.itis.tanki.model.Score;
import ru.itis.tanki.server.ui.ServerGameMap;
import ru.itis.tanki.message.Message;
import ru.itis.tanki.model.Bullet;
import ru.itis.tanki.model.Tank;
import ru.itis.tanki.server.ui.ServerGameWindow;

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Timer;


public class GameServer implements Paintable {
    private Tank serverTank;
    private Tank clientTank;
    private List<Bullet> bullets = new ArrayList<>();
    private ServerGameMap map = new ServerGameMap();
    private Boolean gameOver = false;
    private String winnerName;

    private ServerSocket serverSocket;
    private boolean running = false;


    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    private Timer gameTimer;

    private GameInputHandler serverInputHandler;

    private ServerGameWindow window;
    private ServerStatisticsWindow statsWindow;

    private ScoreRepository scoreRepository;


    public void start(int port) {
        try {
            scoreRepository = new ScoreRepository();
            serverInputHandler = new GameInputHandler(true);
            serverSocket = new ServerSocket(port);
            running = true;

            System.out.println("=== СЕРВЕР ЗАПУЩЕН ===");
            System.out.println("Порт: " + port);
            System.out.println("Сервер играет как Игрок 1");
            System.out.println("Ожидаю подключения клиента (Игрок 2)...");

            serverTank = new Tank(1, "Сервер", 220, 200);

            // ожидаем подключение клиента
            clientSocket = serverSocket.accept();
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            System.out.println("Клиент подключился: " + clientSocket.getInetAddress());

            String joinMessage = in.readUTF();
            Message message = Message.deserialize(joinMessage);
            String clientName;

            clientName = message.getData();
            System.out.println("Игрок под ником " + clientName + " подключился");

            Message resp = new Message(Message.JOIN, "OK");
            out.writeUTF(resp.serialize());
            out.flush();


            sendMapToClient();


            clientTank = new Tank(2,clientName, 1060, 620);
            scoreRepository.addScore(clientName);

            sendGameState();

            SwingUtilities.invokeLater(() -> {
                window = new ServerGameWindow(this);
                window.setVisible(true);
            });


            startGameLoop();


            startClientInputThread();

            startServerInput();

        } catch (Exception e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            stop();
        }
    }


    private void sendMapToClient() {
        try {
            // формируем данные карты
            StringBuilder mapData = new StringBuilder();
            mapData.append(map.getWidthInTiles()).append(",")
                    .append(map.getHeightInTiles()).append(",")
                    .append(map.getTileSize()).append(":");

            // добавляем все тайлы
            for (int y = 0; y < map.getHeightInTiles(); y++) {
                for (int x = 0; x < map.getWidthInTiles(); x++) {
                    if (x > 0 || y > 0) mapData.append(",");
                    mapData.append(map.getTile(x, y));
                }
            }

            // отправляем как отдельное сообщение
            Message mapMsg = new Message(Message.MAP, mapData.toString());
            out.writeUTF(mapMsg.serialize());
            out.flush();

            System.out.println("Карта отправлена клиенту");
        } catch (Exception e) {
            System.err.println("Ошибка отправки карты: " + e.getMessage());
        }
    }

    private void startGameLoop() {
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateGame();
                if (gameOver) gameTimer.cancel();
            }
        }, 0, 16);
    }


    private void startClientInputThread() {
        new Thread(() -> {
            try {
                while (running && clientSocket != null && !clientSocket.isClosed()) {
                        String received = in.readUTF();
                        handleClientMessage(received);
                }
            } catch (Exception e) {
                System.err.println("Ошибка чтения от клиента: " + e.getMessage());
            }
        }).start();
    }


    private void handleClientMessage(String messageStr) {
        try {
            Message msg = Message.deserialize(messageStr);
            switch (msg.getType()) {
                case Message.INPUT:
                    handleClientInput(msg.getData());
                    break;
                case Message.STATS_REQUEST:
                    sendStatistics();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    public void showStatistics() {
        SwingUtilities.invokeLater(() -> {
            if (statsWindow == null) {
                statsWindow = new ServerStatisticsWindow(scoreRepository);
            }
            statsWindow.setVisible(true);
            statsWindow.loadStatistics();
        });
    }

    private void sendStatistics() {
        try {
            List<Score> killsStats = scoreRepository.getTop20FromKills();
            List<Score> winsStats = scoreRepository.getTop20FromWins();

            StringBuilder data = new StringBuilder();

            for (Score score : killsStats) {
                if (data.length() > 0) data.append(";");
                data.append(String.format("%s:%d",
                        score.getPlayerName(), score.getBestKills()));
            }

            data.append("|");

            for (Score score : winsStats) {
                if (data.length() > 0 && !data.toString().endsWith("|")) {
                    data.append(";");
                }
                data.append(String.format("%s:%d",
                        score.getPlayerName(), score.getBestWins()));
            }

            Message statsMsg = new Message(Message.STATS_RESPONSE, data.toString());
            out.writeUTF(statsMsg.serialize());
            out.flush();

            System.out.println("Статистика отправлена клиенту");

        } catch (Exception e) {
            System.err.println("Ошибка отправки статистики: " + e.getMessage());
        }
    }


    private void handleClientInput(String inputData) {
        String[] parts = inputData.split(",");
        if (parts.length == 5) {
            if (clientTank != null) {
                clientTank.setMovingForward(parts[0].equals("1"));
                clientTank.setMovingBackward(parts[2].equals("1"));
                clientTank.setTurningLeft(parts[1].equals("1"));
                clientTank.setTurningRight(parts[3].equals("1"));
                clientTank.setWantToShoot(parts[4].equals("1"));
            }
        }
    }

    private void updateGame() {

        try {
            for (Tank tank : List.of(clientTank, serverTank)) {
                if (tank.getKills()==1) {
                    gameOver = true;
                    winnerName = tank.getName();
                    scoreRepository.updateKills(clientTank.getName(),clientTank.getKills());
                    scoreRepository.updateKills(serverTank.getName(),serverTank.getKills());
                    scoreRepository.updateWins(winnerName);
                    break;
                }
                if (tank.isAlive()) {
                    // сохраняем старую позицию для проверки столкновений
                    float oldX = tank.getX();
                    float oldY = tank.getY();

                    tank.update();

                    if (map.checkTankCollision(tank.getX(), tank.getY(), tank.getWidth(), tank.getHeight())) {
                        tank.setX(oldX);
                        tank.setY(oldY);
                        tank.setSpeed(0);
                        System.out.println("Танк " + tank.getId() + " столкнулся со стеной");
                    }

                    if (tank.getX() < 0 || tank.getX() > map.getWidth() ||
                            tank.getY() < 0 || tank.getY() > map.getHeight()) {
                        tank.setX(oldX);
                        tank.setY(oldY);
                        tank.setSpeed(0);
                    }

                    // обработка выстрела
                    if (tank.isWantToShoot()) {
                        Bullet bullet = tank.shoot();
                        if (bullet != null) {
                            bullets.add(bullet);
                        }
                        tank.setWantToShoot(false);
                        if (tank.equals(serverTank)) serverInputHandler.setSpaceJustPressed(false);
                    }
                }
            }

            // обновляем пули
            Iterator<Bullet> bulletIter = bullets.iterator();
            while (bulletIter.hasNext()) {
                Bullet bullet = bulletIter.next();
                bullet.update();

                // проверка попадания в стены
                if (checkBulletWallCollision(bullet)) {
                    bulletIter.remove();
                    continue;
                }

                // проверка попадания в танки
                for (Tank tank : List.of(clientTank, serverTank)) {
                    if (tank.isAlive() && tank.getId() != bullet.getOwnerId()) {
                        float dx = bullet.getX() - tank.getX();
                        float dy = bullet.getY() - tank.getY();
                        float distance = (float) Math.sqrt(dx * dx + dy * dy);

                        if (distance < 20) {
                            tank.takeDamage(25);
                            bulletIter.remove();

                            if (!tank.isAlive()) {
                                System.out.println("Танк " + tank.getX() + " уничтожен!");
                                Tank killer = getTankById(bullet.getOwnerId());
                                killer.setKills(killer.getKills() + 1);
                                tank.setDeaths(tank.getDeaths() + 1);
                                // возрождение через 3 секунды
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                         serverTank.respawn(220, 200);
                                         clientTank.respawn(1060, 620);
                                    }
                                }, 3000);
                            }
                            break;
                        }
                    }
                }
            }

            // отправляем состояние клиенту
            sendGameState();


        } catch (Exception e) {
            System.err.println("Ошибка в игровом цикле: " + e.getMessage());
        }
    }

    private boolean checkBulletWallCollision(Bullet bullet) {
        int tileX = (int)(bullet.getX() / map.getTileSize());
        int tileY = (int)(bullet.getY() / map.getTileSize());

        if (map.isBulletBlocked(tileX, tileY)) {
            if (map.getTile(tileX, tileY) == ServerGameMap.TILE_BRICK) {
                if (map.destroyBrick(tileX, tileY)) {
                    System.out.println("КИРПИЧНАЯ СТЕНА РАЗРУШЕНА!");
                    sendMapUpdate(tileX, tileY, ServerGameMap.TILE_GRASS);
                }
            }
            return true;
        }

        return false;
    }

    private void sendMapUpdate(int tileX, int tileY, int newTileType) {
        try {
            if (out != null) {
                String data = String.format("%d:%d:%d", tileX, tileY, newTileType);
                Message msg = new Message(Message.MAP_UPDATE, data);
                out.writeUTF(msg.serialize());
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Ошибка отправки обновления карты: " + e.getMessage());
        }
    }


    private void sendGameState() {
        if (out != null && clientSocket != null && !clientSocket.isClosed()) {
            // формируем данные о танках
            StringBuilder tanksData = new StringBuilder();
            for (Tank tank : List.of(clientTank, serverTank)) {
                if (tanksData.length() > 0) tanksData.append(";");
                tanksData.append(String.format(Locale.US, "%d:%.1f:%.1f:%.2f:%d:%b:%s:%d:%d",
                        tank.getId(), tank.getX(), tank.getY(), tank.getAngle(), tank.getHealth(), tank.isAlive(),tank.getName(),tank.getKills(),tank.getDeaths()));
            }

            // формируем данные о пулях
            StringBuilder bulletsData = new StringBuilder();
            for (Bullet bullet : bullets) {
                if (bulletsData.length() > 0) bulletsData.append(";");
                bulletsData.append(String.format(Locale.US, "%.1f:%.1f:%.2f:%.2f:%d",
                        bullet.getX(), bullet.getY(), bullet.getVelX(), bullet.getVelY(), bullet.getOwnerId()));
            }

            String data = tanksData + "|" + bulletsData + "|" + gameOver + "|" + winnerName;
            Message stateMsg = new Message(Message.STATE, data);
            try {
                out.writeUTF(stateMsg.serialize());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void startServerInput() {
        new Thread(() -> {
            while (true) {
                serverTank.setMovingForward(serverInputHandler.iswPressed());
                serverTank.setMovingBackward(serverInputHandler.issPressed());
                serverTank.setTurningLeft(serverInputHandler.isaPressed());
                serverTank.setTurningRight(serverInputHandler.isdPressed());
                serverTank.setWantToShoot(serverInputHandler.isSpaceJustPressed());
            }
        }).start();
    }


    public void stop() {
        running = false;

        try {
            if (gameTimer != null) gameTimer.cancel();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {}

        System.out.println("Сервер остановлен");
        System.exit(0);
    }

    public void restartGame() {

        gameOver = false;
        winnerName = null;

        serverTank = new Tank(1, "Сервер", 220, 200);
        if (clientTank != null) {
            String clientName = clientTank.getName();
            clientTank = new Tank(2, clientName, 1060, 620);
        }

        bullets.clear();

        map = new ServerGameMap();
        sendMapToClient();

        sendGameState();

        startGameLoop();

        System.out.println("Игра перезапущена");
    }


    public List<Bullet> getBullets() {
        return bullets;
    }

    public GameInputHandler getInputHandler() {
        return serverInputHandler;
    }

    public List<Tank> getTanks() {
        return List.of(clientTank, serverTank);
    }

    public Tank getTankById(int id) {
        return id==1?serverTank:clientTank;
    }

    public ServerGameMap getMap() {
        return map;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public Boolean getGameOver() {
        return gameOver;
    }

    public Boolean isServer() {
        return true;
    }


    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start(12345);
    }
}