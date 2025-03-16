package User_Management;

import java.util.logging.Logger;

public class Student extends User {
    private static final Logger logger = Logger.getLogger(User.class.getName());

    public static String role = "Student";



    public Student(int userId, String userName, String userPassword) {
        super(userId, userName, userPassword);
    }

    public Student() {
    }

    @Override
    public void printUser() {
        System.out.println("Student ID: " + this.getUserId());
        System.out.println("Name: " + this.getUserName());
        System.out.println("Role: " + Student.role);
        System.out.println("-----------------------------");
    }

    @Override
    public String toString() {
        return  "Student { " +
                "studentID = " + getUserId() +
                ", Username = '" + getUserName() + '\'' +
                " }";
    }
}
