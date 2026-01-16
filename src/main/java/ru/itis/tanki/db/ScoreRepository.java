package ru.itis.tanki.db;

import ru.itis.tanki.model.Score;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScoreRepository {

    private final String SQL_ADD_SCORE = "insert into score " +
            "(player_name) " +
            "values (?)";
    private final String SQL_GET_TOP20_FROM_KILLS = "select * " +
            "from score " +
            "order by best_kills desc " +
            "limit 20";
    private final String SQL_GET_TOP20_FROM_WINS = "select * " +
            "from score" +
            " order by best_wins desc " +
            "limit 20";
    private final String SQL_GET_BY_NAME = "select * " +
            "from score " +
            "where player_name = ?";
    private final String SQL_UPDATE_KILLS = "update score" +
            " set best_kills = ? " +
            " where player_name = ?";
    private final String SQL_UPDATE_WINS = "update score " +
            "set best_wins = ? " +
            "where player_name= ? ";


    public void addScore(String playerName) {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL_ADD_SCORE)) {
            if (getScore(playerName).isEmpty()) {
                preparedStatement.setString(1, playerName);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateKills(String playerName, int bestKills) {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL_UPDATE_KILLS)) {
            Score score = getScore(playerName).orElse(null);
            if (score != null) {
                preparedStatement.setInt(1, bestKills + score.getBestKills());
                preparedStatement.setString(2, playerName);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateWins(String playerName) {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL_UPDATE_WINS)) {
            Score score = getScore(playerName).orElse(null);
            if (score != null) {
                preparedStatement.setInt(1, score.getBestWins() + 1);
                preparedStatement.setString(2, playerName);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Score> getTop20FromKills() {
        Connection conn = DBConnection.getConnection();
        List<Score> scores = new ArrayList<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL_GET_TOP20_FROM_KILLS);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                Score score = new Score();
                score.setPlayerName(rs.getString("player_name"));
                score.setBestKills(rs.getInt("best_kills"));
                score.setBestWins(rs.getInt("best_wins"));
                scores.add(score);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return scores;
    }

    public List<Score> getTop20FromWins() {
        Connection conn = DBConnection.getConnection();
        List<Score> scores = new ArrayList<>();
        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL_GET_TOP20_FROM_WINS);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                Score score = new Score();
                score.setPlayerName(rs.getString("player_name"));
                score.setBestKills(rs.getInt("best_kills"));
                score.setBestWins(rs.getInt("best_wins"));
                scores.add(score);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return scores;
    }

    public Optional<Score> getScore(String playerName) {
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement preparedStatement = conn.prepareStatement(SQL_GET_BY_NAME)) {
            preparedStatement.setString(1, playerName);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Score score = new Score();
                score.setPlayerName(rs.getString("player_name"));
                score.setBestKills(rs.getInt("best_kills"));
                score.setBestWins(rs.getInt("best_wins"));
                rs.close();
                return Optional.of(score);
            } else {
                rs.close();
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
