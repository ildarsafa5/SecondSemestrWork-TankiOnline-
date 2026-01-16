package ru.itis.tanki.server.ui;

import ru.itis.tanki.common.GamePanel;
import ru.itis.tanki.server.GameServer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGameWindow extends JFrame {

    private GamePanel panel;
    private Timer updateTimer;

    public ServerGameWindow(GameServer gameServer) {
        panel = new GamePanel(gameServer);
        setTitle("Танки-Онлайн (Сервер)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);
        add(panel);

        addKeyListener(gameServer.getInputHandler());

        setFocusable(true);
        requestFocusInWindow();

        updateTimer = new Timer(16, new  ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.repaint();
                if (gameServer.getGameOver() && !panel.isStatsButtonVisible()) {
                    panel.showStatsButton();
                }
            }
        });
        updateTimer.start();
    }
}
