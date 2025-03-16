package Exam_Management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class TrueFalse extends Question {
    private static final Logger logger = Logger.getLogger(TrueFalse.class.getName());

    boolean correctAnswer;

    public TrueFalse(String questionTitle, String questionType, int questionId) {
        super(questionTitle, questionType, questionId);
    }

    public TrueFalse() {

    }

    public void addCorrectAnswerTF(Connection connection, Question question, Boolean correctAnswer) {
        String insertCorrectAQuery = "INSERT INTO TrueFalseAnswers(question_id, correct_answer) Values(?,?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertCorrectAQuery);
            preparedStatement.setInt(1, question.getQuestionID());
            preparedStatement.setBoolean(2, correctAnswer);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Correct answer is inserted Successfully for Question: " + question.getQuestionID());
            } else {
                logger.warning("Failed to import correct answer for Question: " + question.getQuestionID());
            }


        } catch (SQLException e) {
            logger.severe("Error Adding T/F answer: " + e.getMessage());
        }
    }

    @Override
    public void printQuestion() {

        System.out.println("-------------------------------------------------------------");
        System.out.println(this.getQuestionTitle());
        System.out.println("-------------------------------------------------------------");
        System.out.print("1)False\n2)True\n");
    }


    @Override
    public Question insertNewQuestion(Connection connection, int examId, String questionTitle, String questionType) {
        int generatedQuestionId = insertQuestion(connection, examId, questionTitle, questionType);
        if (generatedQuestionId != -1) {
            logger.info("True/False Question added successfully!");
            return new TrueFalse(questionTitle, questionType, generatedQuestionId);
        } else {
            logger.warning("Failed to add True/False Question!");
            return null;
        }
    }


    @Override
    public void returnExamQuestionsById(Connection connection, Exam exam) {
        List<Question> questions = new ArrayList<>();

        String selectQuery =
                "SELECT * FROM Questions\n" +
                        "Where exam_id = ? AND question_type = 'True/False';";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setInt(1, exam.getExamID());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String QTitle = resultSet.getString("question_text");
                String QType = resultSet.getString("question_type");
                int QId = resultSet.getInt("question_id");

                questions.add(new TrueFalse(QTitle, QType, QId));
            }
            exam.setExamQuestions(questions);

        } catch (SQLException e) {
            logger.severe("Error fetching Questions: " + e.getMessage());
        }
//        return questions;

    }

    @Override
    public int answerQuestion() {
        Scanner scanner = new Scanner(System.in);
        int option = -1;

        System.out.println("Enter Number of Your chosen option");
        while (true) {
            option = scanner.nextInt();
            option--;
            if (option == 0 || option == 1) {
                break;
            }
            System.out.println("Invalid Option Number");
        }
        return option;
    }


}
