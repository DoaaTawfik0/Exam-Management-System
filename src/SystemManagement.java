import Exam_Management.*;
import User_Management.*;
import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class SystemManagement {
    private static final String url = "jdbc:sqlserver://localhost:50607;databaseName=EXAM_DB;integratedSecurity=true;encrypt=true;trustServerCertificate=true";
    private static int currentId;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in); Connection connection = DriverManager.getConnection(url)) {
            System.out.println("Connected Successfully with the Database!!");
            int roleOption = enterSystem(scanner);
            insertBreakLine();

            if (roleOption == 1) {
                handleAdminRole(connection, scanner);
            } else if (roleOption == 2) {
                handleStudentRole(connection, scanner);
            } else {
                System.out.println("Invalid role option. Exiting...");
            }
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    private static void handleAdminRole(Connection connection, Scanner scanner) {
        if (loginFunctionality(connection, scanner, 1)) {
            System.out.println("Logged in successfully....");
            displayAdminOptions(connection, scanner);
        } else {
            retryLoginOrExit(connection, scanner, 1); // Retry login or exit
        }
    }

    private static void handleStudentRole(Connection connection, Scanner scanner) {
        if (loginFunctionality(connection, scanner, 2)) {
            System.out.println("Logged in successfully....");
            displayStudentOptions(connection, scanner);
        } else {
            retryLoginOrExit(connection, scanner, 2); // Retry login or exit
        }
    }

    private static boolean loginFunctionality(Connection connection, Scanner scanner, int roleId) {
        String role = roleId == 1 ? "Admin" : "Student";
        System.out.println("Enter your Username: ");
        scanner.nextLine();
        String userName = scanner.next();

        System.out.println("Enter your Password: ");
        scanner.nextLine();
        String password = scanner.next();

        String searchQuery = """
                SELECT COUNT(*) AS userCnt, userID, username
                FROM Users
                WHERE role = ? AND username = ? AND password = ?
                GROUP BY userID, username;
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(searchQuery)) {
            preparedStatement.setString(1, role);
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int userCount = resultSet.getInt("userCnt");
                if (userCount == 1) {
                    currentId = resultSet.getInt("userID");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error trying to login: " + e.getMessage());
        }
        return false;
    }

    private static void displayAdminOptions(Connection connection, Scanner scanner) {
        int adminOption;
        do {
            displayAdminMenu();
            adminOption = getValidOption(scanner, Admin_Options.existSystemOption, Admin_Options.viewStudentDataOption);

            if (adminOption != Admin_Options.existSystemOption) {
                processAdminOption(connection, scanner, adminOption);
            }
        } while (adminOption != Admin_Options.existSystemOption);
        System.out.println("Exiting the system. Goodbye!");
    }

    private static void processAdminOption(Connection connection, Scanner scanner, int option) {
        Admin admin = new Admin();
        Exam exam = new Exam();
        List<Student> students;

        switch (option) {
            case Admin_Options.addNewUserOption -> getDataOfNewUser(connection, scanner, admin);
            case Admin_Options.viewAllStudentsOption -> {
                students = admin.ViewAllStudents(connection);
                students.forEach(System.out::println);
            }
            case Admin_Options.checkUserExistOption -> validateUserExist(connection, scanner, admin);
            case Admin_Options.getStudentByIdOption -> getStudentById(connection, scanner);
            case Admin_Options.addNewExamOption -> addDataOfNewExam(connection, scanner);
            case Admin_Options.addNewQuestionOption -> addNewQuestion(connection, scanner, exam);
            case Admin_Options.viewAllExamsOption -> getAllAvailableExams(connection);
            case Admin_Options.viewStudentDataOption -> fetchStudentDataFromDB(connection, scanner, admin);
            default -> System.out.println("Invalid option selected.");
        }
    }

    private static void displayStudentOptions(Connection connection, Scanner scanner) {
        int studentOption;
        do {
            displayStudentMenu();
            studentOption = getValidOption(scanner, Student_Options.exitSystemOption, Student_Options.showExamResultOption);

            if (studentOption != Student_Options.exitSystemOption) {
                processStudentOption(connection, scanner, studentOption);
            }
        } while (studentOption != Student_Options.exitSystemOption);
        System.out.println("Exiting the system. Goodbye!");
    }

    private static void processStudentOption(Connection connection, Scanner scanner, int option) {
        Exam exam = new Exam();
        ExamAttempts examAttempts = new ExamAttempts();

        switch (option) {
            case Student_Options.enterExamOption -> studentTakeExam(connection, scanner, exam, examAttempts);
            case Student_Options.showExamResultOption -> showGradeOfExam(connection, scanner, examAttempts);
            case Student_Options.viewAllAvailableExamsOption -> getAllAvailableExams(connection);
            default -> System.out.println("Invalid option selected.");
        }
    }

    private static int getValidOption(Scanner scanner, int minOption, int maxOption) {
        int option;
        while (true) {
            if (scanner.hasNextInt()) {
                option = scanner.nextInt();
                if (option >= minOption && option <= maxOption) {
                    return option;
                } else {
                    System.out.println("Invalid option. Please enter a number between " + minOption + " and " + maxOption);
                }
            } else {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next(); // Clear the invalid input
            }
        }
    }

    private static void displayAdminMenu() {
        System.out.println("************************************************");
        System.out.println("0) Exit");
        System.out.println("1) Add new user");
        System.out.println("2) View all students");
        System.out.println("3) Check user exist");
        System.out.println("4) Get Student by ID");
        System.out.println("5) Add new Exam");
        System.out.println("6) Add new Question");
        System.out.println("7) View all exams");
        System.out.println("8) View Student data");
        System.out.println("************************************************");
        System.out.print("Enter your option: ");
    }

    private static void displayStudentMenu() {
        System.out.println("************************************************");
        System.out.println("0) Exit");
        System.out.println("1) Enter Exam");
        System.out.println("2) View all available Exams");
        System.out.println("3) View result of exam");
        System.out.println("************************************************");
        System.out.print("Enter your option: ");
    }

    private static void getDataOfNewUser(Connection connection, Scanner scanner, Admin admin) {
        String userName = getValidUsername(scanner);
        String password = getValidPassword(scanner);
        String role = getValidRole(scanner);

        System.out.println("Saving user to database...");
        admin.AddUser(connection, userName, password, role);
    }

    private static String getValidUsername(Scanner scanner) {
        while (true) {
            System.out.println("Enter Username (must start with a letter): ");
            scanner.nextLine();
            String userName = scanner.next();
            if (isValidUsername(userName)) {
                return userName;
            } else {
                System.out.println("Invalid username.");
            }
        }
    }

    private static String getValidPassword(Scanner scanner) {
        while (true) {
            System.out.println("Enter Password (must be at least 4 characters): ");
            scanner.nextLine();
            String password = scanner.next();
            if (isValidPassword(password)) {
                return password;
            } else {
                System.out.println("Invalid password. Password must be at least 4 characters.");
            }
        }
    }

    private static String getValidRole(Scanner scanner) {
        while (true) {
            System.out.println("Enter Role ('Student' or 'Admin'): ");
            scanner.nextLine();
            String role = scanner.next();
            if (isValidRole(role)) {
                return role;
            } else {
                System.out.println("Invalid role. Role must be either 'Student' or 'Admin'.");
            }
        }
    }

    private static boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z][a-zA-Z0-9_]*$");
    }

    private static boolean isValidPassword(String password) {
        return password.length() >= 4 && password.matches("[a-zA-Z0-9]+");
    }

    private static boolean isValidRole(String role) {
        return role.equals("Student") || role.equals("Admin");
    }

    private static void validateUserExist(Connection connection, Scanner scanner, Admin admin) {
        System.out.println("Enter user ID: ");
        int id = scanner.nextInt();
        boolean checked = admin.CheckUserExist(connection, id);
        if (checked) {
            System.out.println("User with ID: " + id + " exists in the system.");
        } else {
            System.out.println("User with ID: " + id + " does not exist in the system.");
        }
    }

    private static void fetchStudentDataFromDB(Connection connection, Scanner scanner, Admin admin) {
        System.out.println("Enter user ID: ");
        int id = scanner.nextInt();
        Student student = admin.GetStudentByID(connection, id);
        if (student != null) {
            System.out.println(student);
        }
    }

    private static void addDataOfNewExam(Connection connection, Scanner scanner) {
        Exam exam = new Exam();
        System.out.println("Enter Exam title: ");
        scanner.nextLine();
        String title = scanner.nextLine();
        exam.createNewExam(connection, title);
    }

    private static void getAllAvailableExams(Connection connection) {
        Exam exam = new Exam();
        List<Exam> exams = exam.getAvailableExams(connection);
        exams.forEach(System.out::println);
    }

    private static void addNewQuestion(Connection connection, Scanner scanner, Exam exam) {
        System.out.println("Enter ID of exam to insert question in: ");
        int examId = scanner.nextInt();
        Exam returnedExam = exam.GetExamByID(connection, examId);
        if (returnedExam != null) {
            int typeOption = getQuestionType(scanner);
            String qTitle = getQuestionTitle(scanner);
            Question question = insertQuestionBasedOnType(connection, examId, typeOption, qTitle);
            if (typeOption == 1) {
                insertOptionsForMcqQuestion(connection, scanner, question);
            }
        } else {
            System.out.println("Exam with ID does not exist..");
        }
    }

    private static int getQuestionType(Scanner scanner) {
        int option;
        System.out.println("Enter your option based on question type:\n1)MCQ\t2)True/False");
        while (true) {
            option = scanner.nextInt();
            if (option == 1 || option == 2) {
                break;
            } else {
                System.out.println("Invalid option..");
            }
        }
        return option;
    }

    private static String getQuestionTitle(Scanner scanner) {
        System.out.println("Enter title of question: ");
        scanner.nextLine(); // Consume the leftover newline
        return scanner.nextLine();
    }

    private static Question insertQuestionBasedOnType(Connection connection, int examId, int type, String title) {
        if (type == 1) {
            MCQ mcq = new MCQ();
            return mcq.insertNewQuestion(connection, examId, title, "MCQ");
        } else if (type == 2) {
            TrueFalse trueFalse = new TrueFalse();
            return trueFalse.insertNewQuestion(connection, examId, title, "True/False");
        } else {
            System.out.println("Invalid question type..");
            return null;
        }
    }

    private static void insertOptionsForMcqQuestion(Connection connection, Scanner scanner, Question question) {
        MCQ mcq = (MCQ) question;
        System.out.println("Insert four options..");
        for (int i = 0; i < 4; i++) {
            System.out.print("Option " + (i + 1) + ": ");
            String optionData = scanner.next();
            boolean isCorrect = checkCorrectOption(scanner);
            mcq.addOption(connection, optionData, isCorrect);
        }
        System.out.println("Options are added successfully...");
    }

    private static boolean checkCorrectOption(Scanner scanner) {
        int option;
        System.out.println("Does this option represent the correct answer?\t1)True\t2)False");
        while (true) {
            option = scanner.nextInt();
            if (option == 1) {
                return true;
            } else if (option == 2) {
                return false;
            } else {
                System.out.println("Invalid option..");
            }
        }
    }

    private static void getStudentById(Connection connection, Scanner scanner) {
        Admin admin = new Admin();
        System.out.println("Enter Student Id: ");
        int id = scanner.nextInt();
        Student student = admin.GetStudentByID(connection, id);
        System.out.println(student.toString());
    }

    private static void studentTakeExam(Connection connection, Scanner scanner, Exam exam, ExamAttempts examAttempts) {
        Admin admin = new Admin();
        Student studentObj = admin.GetStudentByID(connection, currentId);

        System.out.println("Enter exam Id: ");
        int examId = scanner.nextInt();
        Exam examObj = exam.GetExamByID(connection, examId);

        examAttempts.takeExamUsingID(connection, studentObj, examObj);
    }

    private static void showGradeOfExam(Connection connection, Scanner scanner, ExamAttempts examAttempts) {
        System.out.println("Enter exam Id: ");
        int examId = scanner.nextInt();
        examAttempts.ShowStudentGradeByID(connection, examId, currentId);
    }

    private static int enterSystem(Scanner scanner) {
        int option;
        System.out.println("What is your role?\n1)Admin\t\t\t2)Student");
        while (true) {
            option = scanner.nextInt();
            if (option == 1 || option == 2) {
                break;
            }
            System.out.println("Enter valid option");
        }
        return option;
    }

    private static void insertBreakLine() {
        System.out.println("---------------------------------------------------------------------");
    }

    private static void retryLoginOrExit(Connection connection, Scanner scanner, int roleId) {
        int chosenOption;
        do {
            System.out.println("Invalid Username or password....");
            System.out.println("To exit the system, enter 0.");
            System.out.println("To try again, enter 1.");
            System.out.print("Your choice: ");

            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter 0 or 1.");
                scanner.next(); // Clear the invalid input
            }

            chosenOption = scanner.nextInt();

            if (chosenOption == Admin_Options.existSystemOption) {
                System.out.println("Exiting the system...");
                System.exit(0); // Exit the system
            } else if (chosenOption == 1) {
                if (loginFunctionality(connection, scanner, roleId)) {
                    System.out.println("Logged in successfully.......");
                    if (roleId == 1) {
                        displayAdminOptions(connection, scanner); // Show Admin options after successful login
                    } else if (roleId == 2) {
                        displayStudentOptions(connection, scanner); // Show Student options after successful login
                    }
                    return; // Exit the retry loop after successful login
                }
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        } while (true);
    }
}