package ru.itis.tanki.client.ui;

import ru.itis.tanki.client.GameClient;
import ru.itis.tanki.common.GamePanel;

import javax.swing.*;
import java.awt.event.*;

public class ClientGameWindow extends JFrame {

    private Timer updateTimer;

    private GamePanel gamePanel;

    public ClientGameWindow(GameClient client) {
        gamePanel = new GamePanel(client);

        setTitle("Танки-Онлайн (Клиент)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        add(gamePanel);

        addKeyListener(client.getInputHandler());


        setFocusable(true);
        requestFocusInWindow();


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
            }
        });

        updateTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePanel.repaint();

                if (client.getGameOver() && !gamePanel.isStatsButtonVisible()) {
                    gamePanel.showStatsButton();
                }

                if (!client.getGameOver() && gamePanel.isStatsButtonVisible()) {
                    gamePanel.hideStatsButton();
                }
            }
        });
        updateTimer.start();
    }
}