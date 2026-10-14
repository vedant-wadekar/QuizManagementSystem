
package com.quiz.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QuestionSeeder {

    public static void main(String[] args) {

        String sql = """
            INSERT INTO questions
            (question_text, option_a, option_b, option_c, option_d, correct_answer)
            SELECT ?, ?, ?, ?, ?, ?
            WHERE NOT EXISTS (
                SELECT 1 FROM questions WHERE question_text = ?
            )
            """;

        Object[][] questions = {
            {"Which language runs on the JVM?",
                "Python", "Java", "C", "HTML", "B"},
            {"Which keyword creates an object in Java?",
                "class", "void", "new", "static", "C"},
            {"Which SQL command retrieves records?",
                "INSERT", "UPDATE", "DELETE", "SELECT", "D"},
            {"Which data structure follows FIFO?",
                "Stack", "Queue", "Tree", "Graph", "B"},
            {"Which method is the usual Java program entry point?",
                "start()", "run()", "main()", "init()", "C"}
        };

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (Object[] q : questions) {
                statement.setString(1, (String) q[0]);
                statement.setString(2, (String) q[1]);
                statement.setString(3, (String) q[2]);
                statement.setString(4, (String) q[3]);
                statement.setString(5, (String) q[4]);
                statement.setString(6, (String) q[5]);
                statement.setString(7, (String) q[0]);

                statement.executeUpdate();
            }

            System.out.println("Sample questions inserted successfully!");

        } catch (SQLException e) {
            System.err.println("Could not insert sample questions.");
            e.printStackTrace();
        }
    }
}
