package Exam_Management;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MCQ extends Question {
    private static final Logger logger = Logger.getLogger(MCQ.class.getName());

    List<String> optionsList = new ArrayList<>(); // List for Options

    public MCQ(String questionTitle, String questionType, int questionId) {
        super(questionTitle, questionType, questionId);
    }

    public MCQ() {

    }


    public void addOption(Connection connection, String option, boolean isCorrect) {
        String insertQuery =
                "INSERT INTO MultipleChoiceOptions(question_id, option_text, is_correct) VALUES\n" +
                        "(?, ?, ?);";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setInt(1, this.getQuestionID());
            preparedStatement.setString(2, option);
            preparedStatement.setBoolean(3, isCorrect);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Option for Question: " + this.getQuestionID() + " has been added successfully!");
            } else {
                logger.warning("Failed to add Option for Question: " + this.getQuestionID());
            }

        } catch (SQLException e) {
            logger.severe("Error Adding Options: " + e.getMessage());
        }

    }

    public void fetchOptions(Connection connection) {
        String fetchOptionsQuery =
                "SELECT option_text \n" +
                        "FROM Questions q JOIN MultipleChoiceOptions mco\n" +
                        "ON q.question_id = mco.question_id\n" +
                        "WHERE q.question_id = ?;";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(fetchOptionsQuery);
            preparedStatement.setInt(1, this.getQuestionID());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                logger.info("No options found for question ID: " + this.getQuestionID());
            } else {
                do {
                    this.optionsList.add(resultSet.getString("option_text"));
                } while (resultSet.next());
            }
//            while (resultSet.next()) {
//                mcq.optionsList.add(resultSet.getString("option_text"));
//            }

        } catch (SQLException e) {
            logger.severe("Error fetching Options: " + e.getMessage());
        }
//        return mcq;
    }

    @Override
    public Question insertNewQuestion(Connection connection, int examId, String questionTitle, String questionType) {
        int generatedQuestionId = insertQuestion(connection, examId, questionTitle, questionType);
        if (generatedQuestionId != -1) {
            logger.info("MCQ Question added successfully!");
            return new MCQ(questionTitle, questionType, generatedQuestionId);
        } else {
            logger.warning("Failed to add MCQ Question!");
            return null;
        }

    }

    @Override
    public void returnExamQuestionsById(Connection connection, Exam exam) {
        List<Question> questions = new ArrayList<>();

        String selectQuery =
                "SELECT * FROM Questions\n" +
                        "Where exam_id = ? AND question_type = 'MCQ';";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setInt(1, exam.getExamID());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int QId = resultSet.getInt("question_id");
                String QTitle = resultSet.getString("question_text");
                String QType = resultSet.getString("question_type");

                questions.add(new MCQ(QTitle, QType, QId));
            }
            exam.setExamQuestions(questions);

        } catch (SQLException e) {
            logger.severe("Error fetching Questions: " + e.getMessage());
        }
//        return questions;

    }

    @Override
    public void printQuestion() {
        System.out.println("-------------------------------------------------------------");
        System.out.println(this.getQuestionTitle());
        System.out.println("-------------------------------------------------------------");
        for (int i = 0; i < this.optionsList.size(); i++) {
            System.out.println((i + 1) + ") " + this.optionsList.get(i));
        }
    }

    @Override
    public int answerQuestion() {
        Scanner scanner = new Scanner(System.in);
        int option = -1;
        int size = this.optionsList.size();
        System.out.println("Enter Number of Your chosen option(1-" + size + ")");
        while (true) {
            option = scanner.nextInt();
            if (option >= 1 && option <= size) {
                break;
            }
            System.out.println("Invalid Option Number");
        }
        return option;
    }

//  ********  addOption   METHOD

    @Override
    public String toString() {
        return super.toString();
    }
}


