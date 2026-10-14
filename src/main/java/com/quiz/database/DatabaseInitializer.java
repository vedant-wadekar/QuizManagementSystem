
package com.quiz.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String studentsTable = """
            CREATE TABLE IF NOT EXISTS students (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT UNIQUE
            )
            """;

        String questionsTable = """
            CREATE TABLE IF NOT EXISTS questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                question_text TEXT NOT NULL,
                option_a TEXT NOT NULL,
                option_b TEXT NOT NULL,
                option_c TEXT NOT NULL,
                option_d TEXT NOT NULL,
                correct_answer TEXT NOT NULL
                    CHECK (correct_answer IN ('A', 'B', 'C', 'D'))
            )
            """;

        String resultsTable = """
            CREATE TABLE IF NOT EXISTS results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id INTEGER NOT NULL,
                score INTEGER NOT NULL,
                total_questions INTEGER NOT NULL,
                taken_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES students(id)
            )
            """;

        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(studentsTable);
            statement.execute(questionsTable);
            statement.execute(resultsTable);

            System.out.println("Database tables created successfully!");

        } catch (SQLException e) {
            System.err.println("Failed to initialize database.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initialize();
    }
}
