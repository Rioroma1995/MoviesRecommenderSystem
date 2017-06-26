package app.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DBConnect {

    Connection getConnection() {
        String username = "Romka";
        String password = "123456789";
        Connection conn;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/recommendation", username, password);
            System.out.println("Connected to database");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Incorrect driver", e);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot connect the database!", e);
        }
        return conn;
    }

}