package ru.itis.tanki.client.ui;

import ru.itis.tanki.client.GameClient;

import javax.swing.*;
import java.awt.*;



public class ConnectionWindow extends JFrame {
    private JTextField ipField = new JTextField("localhost", 15);
    private JTextField nameField = new JTextField("Игрок", 15);
    private JButton connectButton = new JButton("Подключиться");

    private String playerName;

    public ConnectionWindow() {
        setTitle("Подключение к серверу");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 200);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        JPanel ipPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ipPanel.add(new JLabel("IP сервера:"));
        ipPanel.add(ipField);


        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Ваше имя:"));
        namePanel.add(nameField);


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(connectButton);


        mainPanel.add(ipPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(namePanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        add(mainPanel);

        connectButton.addActionListener(e -> connect());

        ipField.addActionListener(e -> connect());
        nameField.addActionListener(e -> connect());
    }

    private void connect() {
        String ip = ipField.getText().trim();
        playerName = nameField.getText().trim();


        if (playerName.isEmpty()) {
            playerName = "Игрок";
        }

        new Thread(() -> {
                GameClient client = new GameClient();
                boolean connected = client.connect(ip, 12345, playerName);
                // возвращаемся в EDT для UI операций
                SwingUtilities.invokeLater(() -> {
                    if (connected) {
                        setVisible(false);
                        dispose();
                        new ClientGameWindow(client).setVisible(true);
                    }
                });
        }).start();
    }
}