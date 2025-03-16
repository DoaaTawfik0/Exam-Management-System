package User_Management;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class User {
    private String userName;
    private String userPassword;
    private int userId;

    public User(int userId, String userName, String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
        this.userId = userId;
    }

    public User() {
    }

    public int getUserId() {
        return userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getUserName() {
        return userName;
    }



    public abstract void printUser();
}
