package ru.itis.tanki.model;

public class Score {
    private String playerName;
    private int bestKills;
    private int bestWins;

    public Score() {}

    public Score(String playerName, int bestKills, int bestWins) {
        this.playerName = playerName;
        this.bestKills = bestKills;
        this.bestWins = bestWins;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getBestKills() {
        return bestKills;
    }

    public void setBestKills(int bestKills) {
        this.bestKills = bestKills;
    }

    public int getBestWins() {
        return bestWins;
    }

    public void setBestWins(int bestWins) {
        this.bestWins = bestWins;
    }
}
