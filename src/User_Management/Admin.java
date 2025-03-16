package User_Management;

import Exam_Management.Exam;
import Exam_Management.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Admin extends User {
    private static final Logger logger = Logger.getLogger(Admin.class.getName());

    public static String role = "Admin";

    public Admin(int userId, String userName, String userPassword) {
        super(userId, userName, userPassword);
    }

    public Admin() {

    }

    @Override
    public void printUser() {
        System.out.println("Admin ID: " + this.getUserId());
        System.out.println("Name: " + this.getUserName());
        System.out.println("Role: " + Admin.role);
        System.out.println("-----------------------------");
    }

    public void AddUser(Connection connection, String userName, String userPassword, String role) {

        String insertQuery = "INSERT INTO Users(username, password, role) Values(?,?,?)";

        try {

            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, userPassword);
            preparedStatement.setString(3, role);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                logger.info("User added successfully!");
            } else {
                logger.warning("Failed to add user!");
            }

        } catch (SQLException e) {
            logger.severe("Error Adding new User: " + e.getMessage());
        }
    }

    public List<Student> ViewAllStudents(Connection connection) {

        List<Student> studentsList = new ArrayList<>();

        String selectQuery = "SELECT * FROM Users WHERE Role = 'Student'";

        try {

            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                studentsList.add(new Student(resultSet.getInt("userID"), resultSet.getString("username"), resultSet.getString("password")));
            }

            if (studentsList.isEmpty()) {
                logger.info("There is no students in the system");
                return new ArrayList<Student>();

            }

        } catch (SQLException e) {
            logger.severe("Error viewing all students: " + e.getMessage());
        }
        return studentsList;
    }

    public Student GetStudentByID(Connection connection, int id) {

        String selectQuery = "SELECT * FROM Users WHERE Role = 'Student' AND userID = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();


            if (resultSet.next()) {
                int studentID = resultSet.getInt("userID");
                String userName = resultSet.getString("username");
                String userPassword = resultSet.getString("password");

                return new Student(studentID, userName, userPassword);
            } else {
                logger.warning("Student with ID " + id + " does not exist");
            }

        } catch (SQLException e) {
            logger.severe("Error viewing Student by ID: " + e.getMessage());
        }
        return null;

    }

    public boolean CheckIdBelongsToStudent(Connection connection, int id) {

        String checkQuery =
                "SELECT userID\n" +
                        "FROM Users\n" +
                        "WHERE userID = ? AND role = 'Student'";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {
            logger.severe("Error Checking Student ID: " + e.getMessage());
        }
        return false;
    }

    public boolean CheckUserExist(Connection connection, int id) {
        String checkQuery =
                "SELECT userID, username\n" +
                        "FROM Users\n" +
                        "WHERE userID = ?;";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();

        } catch (SQLException e) {
            logger.severe("Error checking user exist!" + e.getMessage());
        }
        return false;
    }



}
