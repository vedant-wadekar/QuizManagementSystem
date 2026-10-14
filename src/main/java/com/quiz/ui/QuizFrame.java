
package com.quiz.ui;

import com.quiz.database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizFrame extends JFrame {

    private static class Question {
        int id;
        String text;
        String[] options;
        String correctAnswer;

        Question(int id, String text, String[] options,
                 String correctAnswer) {
            this.id = id;
            this.text = text;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    private final int studentId;
    private final List<Question> questions = new ArrayList<>();
    private final List<String> answers = new ArrayList<>();

    private int currentIndex = 0;

    private final JLabel progressLabel = new JLabel();
    private final JLabel questionLabel = new JLabel();
    private final JRadioButton[] optionButtons = new JRadioButton[4];
    private final ButtonGroup optionGroup = new ButtonGroup();
    private final JButton nextButton = new JButton("Next");

    public QuizFrame(int studentId) {
        this.studentId = studentId;

        setTitle("Quiz Management System - Quiz");
        setSize(650, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loadQuestions();

        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(
                null, "No questions available. Please add questions first.");
            dispose();
            return;
        }

        for (int i = 0; i < questions.size(); i++) {
            answers.add(null);
        }

        buildUI();
        showQuestion();
    }

    private void loadQuestions() {
        String sql = """
            SELECT id, question_text, option_a, option_b,
                   option_c, option_d, correct_answer
            FROM questions
            ORDER BY id
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("question_text"),
                    new String[] {
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d")
                    },
                    rs.getString("correct_answer")
                ));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                null, "Could not load questions: " + e.getMessage());
        }
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        progressLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        mainPanel.add(progressLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(questionLabel);
        centerPanel.add(Box.createVerticalStrut(20));

        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 15));
            optionButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            optionGroup.add(optionButtons[i]);
            centerPanel.add(optionButtons[i]);
            centerPanel.add(Box.createVerticalStrut(8));
        }

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        nextButton.addActionListener(e -> handleNext());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(nextButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void showQuestion() {
        Question q = questions.get(currentIndex);

        progressLabel.setText(
            "Question " + (currentIndex + 1) + " of " + questions.size());

        questionLabel.setText(
            "<html><div style='width:540px'>"
            + escapeHtml(q.text) + "</div></html>");

        String savedAnswer = answers.get(currentIndex);

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(
                (char) ('A' + i) + ". " + q.options[i]);
            optionButtons[i].setActionCommand(String.valueOf((char) ('A' + i)));
            optionButtons[i].setSelected(
                savedAnswer != null
                && savedAnswer.equals(String.valueOf((char) ('A' + i))));
        }

        optionGroup.clearSelection();

        if (savedAnswer != null) {
            for (JRadioButton button : optionButtons) {
                if (button.getActionCommand().equals(savedAnswer)) {
                    button.setSelected(true);
                    break;
                }
            }
        }

        nextButton.setText(
            currentIndex == questions.size() - 1 ? "Submit Quiz" : "Next");
    }

    private void handleNext() {
        ButtonModel selected = optionGroup.getSelection();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                this, "Please select an answer before continuing.");
            return;
        }

        answers.set(currentIndex, selected.getActionCommand());

        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            submitQuiz();
        }
    }

    private void submitQuiz() {
        int score = 0;

        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).correctAnswer.equals(answers.get(i))) {
                score++;
            }
        }

        String sql = """
            INSERT INTO results (student_id, score, total_questions)
            VALUES (?, ?, ?)
            """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            statement.setInt(2, score);
            statement.setInt(3, questions.size());
            statement.executeUpdate();

            JOptionPane.showMessageDialog(
                this,
                "Quiz completed!\nYour score: " + score + " / "
                + questions.size() + "\nPercentage: "
                + String.format("%.2f",
                    score * 100.0 / questions.size()) + "%",
                "Quiz Result",
                JOptionPane.INFORMATION_MESSAGE
            );

            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this, "Could not save result: " + e.getMessage());
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
