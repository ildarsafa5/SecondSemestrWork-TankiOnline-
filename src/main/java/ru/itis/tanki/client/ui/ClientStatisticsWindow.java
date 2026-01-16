package ru.itis.tanki.client.ui;

import ru.itis.tanki.model.Score;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientStatisticsWindow extends JFrame {
    private JTable statsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statTypeCombo;


    private List<Score> killsStats;
    private List<Score> winsStats;

    public ClientStatisticsWindow() {
        setTitle("Статистика игроков");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());


        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(new JLabel("Топ по:"));

        statTypeCombo = new JComboBox<>(new String[]{"Убийствам", "Победам"});
        statTypeCombo.addActionListener(e -> updateTable());


        topPanel.add(statTypeCombo);


        add(topPanel, BorderLayout.NORTH);

        String[] initialColumns = {"Игрок", "Убийства"};
        tableModel = new DefaultTableModel(initialColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        statsTable = new JTable(tableModel);
        statsTable.setRowHeight(30);
        statsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));


        JScrollPane scrollPane = new JScrollPane(statsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(scrollPane, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> setVisible(false));
        bottomPanel.add(closeButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void updateStatistics(List<Score> killsStats, List<Score> winsStats) {
        this.killsStats = killsStats;
        this.winsStats = winsStats;
        updateTable();
    }


    private void updateTable() {
        if (killsStats == null || winsStats == null) {
            return;
        }


        boolean isKillsMode = statTypeCombo.getSelectedIndex() == 0;


        String[] columns = isKillsMode ?
                new String[]{"Игрок", "Убийства"} :
                new String[]{"Игрок", "Победы"};

        tableModel.setColumnIdentifiers(columns);


        tableModel.setRowCount(0);


        List<Score> stats;
        if (isKillsMode) {
            stats = killsStats;
        } else {
            stats = winsStats;
        }


        for (Score score : stats) {
            if (isKillsMode) {
                tableModel.addRow(new Object[] {
                        score.getPlayerName(),
                        score.getBestKills()
                });
            } else {
                tableModel.addRow(new Object[] {
                        score.getPlayerName(),
                        score.getBestWins()
                });
            }
        }

        statsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Имя игрока
        statsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Цифры
    }
}