package Exam_Management;

import User_Management.Admin;
import User_Management.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ExamAttempts {
    private static final Logger logger = Logger.getLogger(ExamAttempts.class.getName());


    //Prerequisite ->
    public void takeExamUsingID(Connection connection, Student student, Exam exam) {
        List<Question> examQuestions = new ArrayList<>();
        MCQ mcq = new MCQ();
        TrueFalse trueFalse = new TrueFalse();

        int questionAnswer = -1;

        mcq.returnExamQuestionsById(connection, exam);
        trueFalse.returnExamQuestionsById(connection, exam);
        examQuestions = exam.getExamQuestions();

        if (!examQuestions.isEmpty()) {
            for (int i = 0; i < examQuestions.size(); i++) {
                Question question = examQuestions.get(i);

                if (question.getQuestionType().equalsIgnoreCase(QuestionTypes.MultiChoiceQuestion)) {
                    MCQ mcqQuestion = (MCQ) question;  // Correct casting
                    mcqQuestion.fetchOptions(connection);
                    mcqQuestion.printQuestion();
                    questionAnswer = mcqQuestion.answerQuestion();

                } else if (question.getQuestionType().equalsIgnoreCase(QuestionTypes.TrueFalseQuestion)) {
                    TrueFalse trueFalseQuestion = (TrueFalse) question;  // Correct casting
                    trueFalseQuestion.printQuestion();
                    questionAnswer = trueFalseQuestion.answerQuestion();
                    // trueFalseQuestion.someMethod();  // Uncomment and call the appropriate method
                } else {
                    logger.warning("Question type is not correct!");
                }
                question.insertQuestionAnswer(connection, student.getUserId(), exam.getExamID(), question.getQuestionID(), questionAnswer);
            }
            getStudentGrade(connection, exam.getExamID(), student.getUserId());
        } else {
            logger.warning("Exam with ID: " + exam.getExamID() + " has no questions!");
        }
    }

    public boolean CheckStudentTakeExam(Connection connection, int examId, int studentId) {

        String checkQuery =
                "SELECT DISTINCT username\n" +
                        "FROM Users us  JOIN ExamAttempts ea\n" +
                        "ON us.userID = ea.student_id\n" +
                        "WHERE student_id = ? AND exam_id = ? AND role = 'Student';\n";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setInt(1, studentId);
            preparedStatement.setInt(2, examId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.warning("Only one submission is valid for this exam");
        }
        return false;
    }

    public void ShowStudentGradeByID(Connection connection, int examId, int studentId) {

        int grade = 0;
        String showQuery =
                "SELECT \n" +
                        "\t   ex.title,\n" +
                        "       us.username,\n" +
                        "\t   sg.correct_answers,\n" +
                        "\t   sg.Quescount\n" +
                        "FROM USERS us JOIN StudentsGrades sg\n" +
                        "JOIN EXAMS ex ON ex.exam_id = sg.exam_id\n" +
                        "ON us.userID = ? AND sg.student_id = ?\n" +
                        "Where ex.exam_id = ?";


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(showQuery);
            preparedStatement.setInt(1, studentId);
            preparedStatement.setInt(2, studentId);
            preparedStatement.setInt(3, examId);


            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                String username = resultSet.getString("username");
                String title = resultSet.getString("title");
                int total = resultSet.getInt("Quescount");
                int correct = resultSet.getInt("correct_answers");

                System.out.println("Student " + "(" + username + ") with ID: " + studentId + " got in '" + title + "' exam " +
                        correct + "/" + total + " correct answers");

            } else {
                System.out.println("Student with ID: " + studentId + " didn't take the exam with ID: " + examId);
            }

        } catch (SQLException e) {
            System.out.println("Error Showing student grade " + e.getMessage());
        }

    }


    public void getStudentGrade(Connection connection, int examId, int studentId) {

        String insertCAQuery = "INSERT INTO StudentsGrades (student_id, exam_id, correct_answers, Quescount)\n" +
                "SELECT \n" +
                "    student_id, \n" +
                "    exam_id, \n" +
                "    COUNT(*) AS correct_answers,\n" +
                "    (SELECT COUNT(*) FROM Questions WHERE exam_id = ?) AS Quescount\n" +
                "FROM (\n" +
                "    -- Handling Multiple-Choice Questions\n" +
                "    SELECT \n" +
                "        ea.student_id, \n" +
                "        ea.exam_id\n" +
                "    FROM \n" +
                "        ExamAttempts ea\n" +
                "    JOIN \n" +
                "        Questions q ON ea.question_id = q.question_id AND ea.exam_id = q.exam_id\n" +
                "    JOIN (\n" +
                "        SELECT \n" +
                "            question_id, \n" +
                "            option_id, \n" +
                "            is_correct, \n" +
                "            ROW_NUMBER() OVER (PARTITION BY question_id ORDER BY option_id) AS option_index\n" +
                "        FROM \n" +
                "            MultipleChoiceOptions\n" +
                "    ) mco ON ea.question_id = mco.question_id \n" +
                "         AND TRY_CAST(ea.student_answer AS BIGINT) = mco.option_index \n" +
                "         AND mco.is_correct = 1\n" +
                "    JOIN \n" +
                "        Users us ON ea.student_id = us.userID\n" +
                "    WHERE \n" +
                "        us.role = 'Student'\n" +
                "        AND ea.student_id = ?\n" +
                "        AND ea.exam_id = ?\n" +
                "\n" +
                "    UNION ALL\n" +
                "\n" +
                "    -- Handling True/False Questions\n" +
                "    SELECT \n" +
                "        ea.student_id, \n" +
                "        ea.exam_id\n" +
                "    FROM \n" +
                "        ExamAttempts ea\n" +
                "    JOIN \n" +
                "        Questions q ON ea.question_id = q.question_id AND ea.exam_id = q.exam_id\n" +
                "    JOIN \n" +
                "        TrueFalseAnswers tfa ON ea.question_id = tfa.question_id \n" +
                "    JOIN \n" +
                "        Users us ON ea.student_id = us.userID\n" +
                "    WHERE \n" +
                "        us.role = 'Student'\n" +
                "        AND (\n" +
                "            (ea.student_answer = 'true' AND tfa.correct_answer = 1) OR \n" +
                "            (ea.student_answer = 'false' AND tfa.correct_answer = 0) OR\n" +
                "            (TRY_CAST(ea.student_answer AS BIGINT) = tfa.correct_answer) -- If stored as 1/0\n" +
                "        )\n" +
                "        AND ea.student_id = ?\n" +
                "        AND ea.exam_id = ?\n" +
                ") correct_attempts\n" +
                "GROUP BY \n" +
                "    student_id, exam_id;";


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(insertCAQuery);
            preparedStatement.setInt(1, examId);
            preparedStatement.setInt(2, studentId);
            preparedStatement.setInt(3, examId);
            preparedStatement.setInt(4, studentId);
            preparedStatement.setInt(5, examId);

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 1) {
                System.out.println("Submission Succeed");
            } else {
                System.out.println("Submission Failed");
            }
        } catch (SQLException e) {
            System.out.println("Error submitting exam!" + e.getMessage());
        }
    }


}
