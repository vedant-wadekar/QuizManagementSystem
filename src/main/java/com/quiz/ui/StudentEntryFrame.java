
package com.quiz.ui;

import com.quiz.database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StudentEntryFrame extends JFrame {

    private final JTextField nameField = new JTextField(22);
    private final JTextField emailField = new JTextField(22);

    public StudentEntryFrame() {
        setTitle("Quiz Management System");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(
            BorderFactory.createEmptyBorder(25, 35, 25, 35)
        );

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel(
            "Welcome to the Quiz", SwingConstants.CENTER
        );
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Student Name:"), gbc);

        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        panel.add(emailField, gbc);

        JButton startButton = new JButton("Start Quiz");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        startButton.addActionListener(e -> registerStudent());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(startButton, gbc);

        add(panel);
    }

    private void registerStudent() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter your name and email."
            );
            return;
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            JOptionPane.showMessageDialog(
                this, "Please enter a valid email address."
            );
            return;
        }

        String sql = """
            INSERT INTO students (name, email)
            VALUES (?, ?)
            """;

        try (
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS
            )
        ) {
            statement.setString(1, name);
            statement.setString(2, email);

            statement.executeUpdate();

            int studentId;

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException(
                        "Could not retrieve student ID."
                    );
                }

                studentId = generatedKeys.getInt(1);
            }

            JOptionPane.showMessageDialog(
                this, "Registration successful! Starting quiz."
            );

            dispose();

            SwingUtilities.invokeLater(() ->
                new QuizFrame(studentId).setVisible(true)
            );

        } catch (SQLException ex) {
            if (ex.getMessage() != null &&
                ex.getMessage().contains("UNIQUE constraint failed")) {

                JOptionPane.showMessageDialog(
                    this, "This email is already registered."
                );

            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Registration failed: " + ex.getMessage()
                );
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new StudentEntryFrame().setVisible(true)
        );
    }
}
