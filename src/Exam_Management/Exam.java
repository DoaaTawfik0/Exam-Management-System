package Exam_Management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Exam {
    private static final Logger logger = Logger.getLogger(Exam.class.getName());

    private int examID;
    private String examTitle;
    private List<Question> examQuestions = new ArrayList<>();  // work on questions

    public Exam(int examID, String examTitle) {
        this.examID = examID;
        this.examTitle = examTitle;
    }


    public Exam() {

    }

    public void setExamQuestions(List<Question> examQuestions) {
        this.examQuestions.addAll(examQuestions);
    }

    public List<Question> getExamQuestions() {
        return examQuestions;
    }

    public int getExamID() {
        return examID;
    }

    public void setExamID(int examID) {
        this.examID = examID;
    }

    public String getExamTitle() {
        return examTitle;
    }
//    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public boolean setExamTitle(String examTitle) {
        if (!examTitle.isBlank() && examTitle.length() >= 3) {
            this.examTitle = examTitle;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Exam { " +
                "examID = " + examID +
                ", title = '" + examTitle + '\'' +
                " }";
    }

    public void createNewExam(Connection connection, String title) {
        String insertTitle = "INSERT INTO Exams(title) VALUES(?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertTitle, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, title);
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        int examID = resultSet.getInt(1);
                        Exam exam = new Exam(examID, title);
                        logger.info("Exam created successfully with ID: " + examID);
                    }
                }
            } else {
                logger.warning("Exam not created successfully!");
            }
        } catch (SQLException e) {
            logger.severe("Error creating exam: " + e.getMessage());
        }
    }

    public Exam GetExamByID(Connection connection, int examID) {
        String searchQuery = "SELECT title FROM Exams WHERE exam_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(searchQuery)) {
            preparedStatement.setInt(1, examID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Exam(examID, resultSet.getString("title"));
                } else {
                    logger.warning("Exam with ID: " + examID + " does not exist!");
                }
            }
        } catch (SQLException e) {
            logger.severe("Error checking exam existence: " + e.getMessage());
        }
        return null;
    }

    public List<Exam> getAvailableExams(Connection connection) {
        List<Exam> exams = new ArrayList<>();
        String selectQuery = "SELECT exam_id, title FROM Exams";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                exams.add(new Exam(resultSet.getInt("exam_id"), resultSet.getString("title")));
            }

            if (exams.isEmpty()) {
                logger.info("No available Exams!");
            }
        } catch (SQLException e) {
            logger.severe("Error fetching exams: " + e.getMessage());
        }
        return exams;
    }

//    public List<Question> getExamQuestionsById(Connection connection, Exam exam) {
//        List<Question> questions = new ArrayList<>();
//
//        String selectQuery =
//                "SELECT * FROM Questions\n" +
//                        "Where exam_id = 1?;";
//
//        try {
//            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
//            preparedStatement.setInt(1, exam.getExamID());
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                String QTitle = resultSet.getString("question_text");
//                String QType = resultSet.getString("question_type");
//                int QId = resultSet.getInt("question_id");
//
//                if (QType.equalsIgnoreCase("MCQ")) {
//                    questions.add(new MCQ(QTitle, QType, QId));
//                } else if (QType.equalsIgnoreCase("True/False")) {
//                    questions.add(new TrueFalse(QTitle, QType, QId));
//                } else {
//                    logger.warning("Invalid Question Type");
//                }
//            }
//
//        } catch (SQLException e) {
//            logger.severe("Error fetching Questions: " + e.getMessage());
//        }
//        return questions;
//    }
}