package ru.itis.tanki.common;


import ru.itis.tanki.model.Bullet;
import ru.itis.tanki.server.GameServer;
import ru.itis.tanki.server.ui.ServerGameMap;
import ru.itis.tanki.model.Tank;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;


public class GamePanel extends JPanel {

    private Paintable entity;

    private JButton restartButton;
    private JButton statsButton;


    public GamePanel(Paintable entity) {
        this.entity = entity;
        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        addKeyListener(entity.getInputHandler());

        // Создаем кнопку (не показываем сразу)
        restartButton = new JButton("Новая игра");
        restartButton.setBounds(0, 0, 150, 40);
        restartButton.setVisible(false);


        restartButton.addActionListener(e -> {
            if (entity.isServer()) {
                GameServer server = (GameServer) entity;
                hideStatsButton();
                server.restartGame();
            }
        });

        statsButton = new JButton("Статистика");
        statsButton.setBounds(0, 0, 150, 40);
        statsButton.setVisible(false);
        statsButton.addActionListener(e -> {
            entity.showStatistics();
        });

        setLayout(null);
        add(restartButton);
        add(statsButton);
    }

    public void showStatsButton() {
        if (!entity.getGameOver()) return;

        // Позиционирование
        int centerX = getWidth() / 2;
        int buttonY = getHeight() / 2 + 100;

        if (entity.isServer()) {
            // У сервера две кнопки рядом
            restartButton.setBounds(centerX - 160, buttonY, 150, 40);
            statsButton.setBounds(centerX + 10, buttonY, 150, 40);
            restartButton.setVisible(true);
        } else {
            // У клиента одна кнопка по центру
            statsButton.setBounds(centerX - 75, buttonY, 150, 40);
        }

        statsButton.setVisible(true);
        statsButton.repaint();
    }

    public void hideStatsButton() {
        statsButton.setVisible(false);
        if (entity.isServer() && restartButton != null) {
            restartButton.setVisible(false);
        }
        requestFocusInWindow();
    }

    public boolean isStatsButtonVisible() {
        return statsButton.isVisible();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;


        if (entity.getGameOver()) {
            drawEnd(g);
            return;
        }

        // 2. Рисуем карту
        drawMap(g2d);


        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);


        // Рисуем с копиями
        drawGame(g2d, entity.getTanks(), entity.getBullets());

        // Рисуем UI с копиями
        drawUI(g2d, entity.getTanks());
    }

    private void drawMap(Graphics2D g2d) {
        GameMap map = entity.getMap();

        int tileSize = map.getTileSize();

        for (int y = 0; y < map.getHeightInTiles(); y++) {
            for (int x = 0; x < map.getWidthInTiles(); x++) {
                int tileType = map.getTile(x, y);
                Color tileColor = getTileColor(tileType);

                int screenX = x * tileSize;
                int screenY = y * tileSize;

                //  Заливка
                g2d.setColor(tileColor);
                g2d.fillRect(screenX, screenY, tileSize, tileSize);

                //  ОБВОДКА для непроходимых тайлов
                if (tileType == ServerGameMap.TILE_STONE ||
                        tileType == ServerGameMap.TILE_BRICK ||
                        tileType == ServerGameMap.TILE_WATER) {

                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRect(screenX, screenY, tileSize, tileSize);

                    // Дополнительная внутренняя обводка
                    g2d.setColor(tileColor.darker());
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRect(screenX + 2, screenY + 2, tileSize - 4, tileSize - 4);
                }

                //  ТЕКСТУРА для кирпича
                if (tileType == ServerGameMap.TILE_BRICK) {
                    drawBrickTexture(g2d, screenX, screenY, tileSize);
                }

                //  ТЕКСТУРА для камня
                if (tileType == ServerGameMap.TILE_STONE) {
                    drawStoneTexture(g2d, screenX, screenY, tileSize);
                }
            }
        }
    }

    private void drawBrickTexture(Graphics2D g2d, int x, int y, int size) {
        // Горизонтальные линии между кирпичами
        g2d.setColor(new Color(120, 60, 40));
        g2d.drawLine(x, y + size/3, x + size, y + size/3);
        g2d.drawLine(x, y + 2*size/3, x + size, y + 2*size/3);

        // Вертикальные линии (со смещением)
        for (int i = 0; i < 3; i++) {
            int lineX = x + i * size/3;
            if (i % 2 == 0) {
                // Длинные кирпичи в шахматном порядке
                g2d.drawLine(lineX, y, lineX, y + size/3);
                g2d.drawLine(lineX, y + 2*size/3, lineX, y + size);
            } else {
                g2d.drawLine(lineX, y + size/3, lineX, y + 2*size/3);
            }
        }
    }

    private void drawStoneTexture(Graphics2D g2d, int x, int y, int size) {
        // Случайные точки и линии для текстуры камня
        Random rand = new Random(x * 31 + y);

        // Тёмные пятна
        g2d.setColor(new Color(60, 60, 60, 150));
        for (int i = 0; i < 5; i++) {
            int px = x + rand.nextInt(size - 4) + 2;
            int py = y + rand.nextInt(size - 4) + 2;
            int r = 2 + rand.nextInt(4);
            g2d.fillOval(px, py, r, r);
        }

        // Светлые блики
        g2d.setColor(new Color(180, 180, 180, 100));
        for (int i = 0; i < 3; i++) {
            int px = x + rand.nextInt(size - 4) + 2;
            int py = y + rand.nextInt(size - 4) + 2;
            int r = 1 + rand.nextInt(3);
            g2d.fillOval(px, py, r, r);
        }
    }

    private Color getTileColor(int tileType) {
        switch (tileType) {
            case ServerGameMap.TILE_GRASS:
                return new Color(100, 180, 100);   // ЯРКО-зелёный

            case ServerGameMap.TILE_STONE:
                return new Color(80, 80, 80);      // ТЁМНО-серый (видно!)

            case ServerGameMap.TILE_BRICK:
                return new Color(160, 80, 60);     // ЯРКО-коричневый

            case ServerGameMap.TILE_WATER:
                return new Color(60, 60, 180);     // ЯРКО-синий

            case ServerGameMap.TILE_SAND:
                return new Color(210, 190, 150);   // Светло-бежевый

            default:
                return Color.MAGENTA;
        }
    }

    private void drawGame(Graphics2D g2d, List<Tank> tanks, List<Bullet> bullets) {
        // Рисуем танки
        for (Tank tank : tanks) {
            if (tank.isAlive()) {
                drawTank(g2d, tank);
                drawHealthBar(g2d, tank);
            }
        }

        // Рисуем пули
        g2d.setColor(Color.YELLOW);
        for (Bullet bullet : bullets) {
            int x = (int)(bullet.getX());
            int y = (int)(bullet.getY());
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }
    }

    private void drawEnd(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        // Фон
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Окно
        int w = 500, h = 350;
        int x = (getWidth() - w) / 2;
        int y = (getHeight() - h) / 2;

        // Градиентный фон окна
        GradientPaint gradient = new GradientPaint(
                x, y, new Color(30, 30, 60),
                x, y + h, new Color(10, 10, 30)
        );
        g2d.setPaint(gradient);
        g2d.fillRoundRect(x, y, w, h, 25, 25);

        // Обводка
        g2d.setColor(new Color(100, 100, 200));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRoundRect(x, y, w, h, 25, 25);


        // Имя победителя
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(new Color(255, 215, 0)); // золотой

        FontMetrics fm = g2d.getFontMetrics();
        int nameX = x + (w - fm.stringWidth(entity.getWinnerName())) / 2;
        int nameY = y + 180;
        g2d.drawString(entity.getWinnerName(), nameX, nameY);

        // Статистика (опционально)
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        String stats = "Одержал победу";
        fm = g2d.getFontMetrics();
        g2d.drawString(stats, x + (w - fm.stringWidth(stats))/2, y + 220);
    }


    private void drawTank(Graphics2D g2d, Tank tank) {
        // Позиция на экране
        int screenX = (int)(tank.getX());
        int screenY = (int)(tank.getY());

        // Сохраняем трансформацию
        AffineTransform oldTransform = g2d.getTransform();

        // Поворачиваем
        g2d.translate(screenX, screenY);
        g2d.rotate(tank.getAngle());

        // Цвет в зависимости от ID
        Color tankColor = (tank.getId() == 1) ? Color.RED : Color.BLUE;

        // Корпус
        g2d.setColor(tankColor);
        int halfWidth = (int)tank.getWidth() / 2;
        int halfHeight = (int)tank.getHeight() / 2;
        g2d.fillRoundRect(-halfWidth, -halfHeight,
                (int)tank.getWidth(), (int)tank.getHeight(), 10, 10);

        // Обводка
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(-halfWidth, -halfHeight,
                (int)tank.getWidth(), (int)tank.getHeight(), 10, 10);

        // Башня
        int turretSize = (int)(Math.min(tank.getWidth(), tank.getHeight()) * 0.7);
        g2d.setColor(tankColor.darker());
        g2d.fillOval(-turretSize/2, -turretSize/2, turretSize, turretSize);

        // Дуло
        int barrelLength = halfWidth + 8;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, -3, barrelLength, 6);

        // Восстанавливаем трансформацию
        g2d.setTransform(oldTransform);
    }


    private void drawHealthBar(Graphics2D g2d, Tank tank) {
        int screenX = (int)(tank.getX());
        int screenY = (int)(tank.getY());

        int barWidth = 40;
        int barHeight = 5;
        int barY = screenY - (int)tank.getHeight()/2 - 10;

        // Фон
        g2d.setColor(Color.BLACK);
        g2d.fillRect(screenX - barWidth/2 - 1, barY - 1,
                barWidth + 2, barHeight + 2);

        // Здоровье
        Color healthColor;
        if (tank.getHealth() > 60) healthColor = Color.GREEN;
        else if (tank.getHealth() > 30) healthColor = Color.YELLOW;
        else healthColor = Color.RED;

        g2d.setColor(healthColor);
        int healthWidth = (int)(barWidth * (tank.getHealth() / 100.0));
        g2d.fillRect(screenX - barWidth/2, barY, healthWidth, barHeight);
    }


    private void drawUI(Graphics2D g2d, List<Tank> tanks) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(10, 10, 350, 150);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        g2d.drawString("ТАНКИ-ОНЛАЙН", 20, 30);

        int y = 50;
        for (Tank tank : tanks) {
            String status = tank.isAlive() ?
                    String.format("Здоровье: %d Убийств: %d Смертей: %d", tank.getHealth(),tank.getKills(),tank.getDeaths()) : "УНИЧТОЖЕН";
            g2d.drawString(tank.getName() + ": " + status, 20, y) ;
            y += 20;
        }

        y += 10;
        g2d.drawString("Управление:", 20, y);
        if (entity.isServer()) {
            y += 20;
            g2d.drawString("UP/LEFT/DOWN/RIGHT - движение", 30, y);
            y += 20;
            g2d.drawString("M - выстрел", 30, y);
        } else {
            y += 20;
            g2d.drawString("W/A/S/D - движение", 30, y);
            y += 20;
            g2d.drawString("ПРОБЕЛ - выстрел", 30, y);
        }
    }
}