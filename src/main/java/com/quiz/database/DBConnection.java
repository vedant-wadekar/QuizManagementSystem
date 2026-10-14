
package com.quiz.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database file will be created in the project folder.
    private static final String URL = "jdbc:sqlite:quiz.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("SQLite database connected successfully!");
            System.out.println("Database: quiz.db");
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}
