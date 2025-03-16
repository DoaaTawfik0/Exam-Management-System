package Exam_Management;

import User_Management.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public abstract class Question {
    private static final Logger logger = Logger.getLogger(Question.class.getName());

    String questionTitle;
    String questionType;

    public int getQuestionID() {
        return questionID;
    }

    private int questionID;

    public Question() {

    }

    public Question(String questionTitle, String questionType, int questionID) {
        setQuestionTitle(questionTitle);
        setQuestionType(questionType);
        this.questionID = questionID; /// more validation need to be taken in concern        **********
    }

    public boolean setQuestionTitle(String questionTitle) {
        if (!questionTitle.isBlank() && questionTitle.length() >= 6) {
            this.questionTitle = questionTitle;
            return true;
        }
        return false;
    }

    public boolean setQuestionType(String questionType) {
        if (questionType.equalsIgnoreCase(QuestionTypes.MultiChoiceQuestion) || questionType.equalsIgnoreCase(QuestionTypes.TrueFalseQuestion)) {
            this.questionType = questionType;
            return true;
        }
        return false;

    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public String getQuestionType() {
        return questionType;
    }

    public abstract void printQuestion();

    protected int insertQuestion(Connection connection, int examId, String questionTitle, String questionType) {
        String insertQuestionData = "INSERT INTO Questions(exam_id, question_text, question_type) VALUES(?, ?, ?)";
        int generatedQuestionId = -1;

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuestionData, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, examId);
            preparedStatement.setString(2, questionTitle);
            preparedStatement.setString(3, questionType);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        generatedQuestionId = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Error inserting question: " + e.getMessage());
        }
        return generatedQuestionId;
    }

    // Abstract method for subclasses to implement
    public abstract Question insertNewQuestion(Connection connection, int examId, String questionTitle, String questionType);

    public abstract void returnExamQuestionsById(Connection connection, Exam exam);

    public abstract int answerQuestion();

    public void insertQuestionAnswer(Connection connection, int stuId, int examId, int questionId, int answer) {
        String insertQuery = "INSERT INTO ExamAttempts(student_id,exam_id,question_id,student_answer) VALUES\n" +
                "(?,?,?,?);";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setInt(1, stuId);
            preparedStatement.setInt(2, examId);
            preparedStatement.setInt(3, questionId);
            preparedStatement.setInt(4, answer);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("your answer has been submitted successfully");
            } else {
                logger.warning("Failed to submit answer!");
            }


        } catch (SQLException e) {
            logger.severe("Error submitting answer: " + e.getMessage());
        }
    }


}