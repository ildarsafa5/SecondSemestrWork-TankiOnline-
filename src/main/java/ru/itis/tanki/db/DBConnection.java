package ru.itis.tanki.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static Connection connection;

    public static Connection getConnection() {
        if (connection != null) {
            return connection;
        } else {
            try {
                Properties props = new Properties();
                InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("database.properties");
                props.load(in);
                connection = DriverManager.getConnection(props.getProperty("db.url"), props.getProperty("db.username"), props.getProperty("db.password"));
                return connection;
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}