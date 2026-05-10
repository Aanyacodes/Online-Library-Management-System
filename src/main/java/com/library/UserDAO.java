package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // =========================
    // REGISTER USER
    // =========================
    public boolean registerUser(
            String firstName,
            String lastName,
            String email,
            String phone,
            String password,
            String address) {

        String sql =
                "INSERT INTO user_details " +
                "(userId, first_name, last_name, email, phone_number, password, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String userId =
                "USR-" +
                java.util.UUID.randomUUID()
                        .toString()
                        .substring(0, 5)
                        .toUpperCase();

        try (Connection conn =
                     createDBConnection.getConnection();

             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            pstmt.setString(6, password);
            pstmt.setString(7, address);

            int rowsAffected =
                    pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {

            System.err.println(
                    "Database Error: "
                            + e.getMessage());

            return false;
        }
    }

    // =========================
    // LOGIN USER
    // =========================
    public boolean loginUser(
            String email,
            String rawPassword) {

        String sql =
                "SELECT password " +
                "FROM user_details " +
                "WHERE email = ?";

        try (Connection conn =
                     createDBConnection.getConnection();

             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs =
                         pstmt.executeQuery()) {

                if (rs.next()) {

                    String storedHash =
                            rs.getString("password");

                    return PasswordUtil.checkPassword(
                            rawPassword,
                            storedHash);

                } else {

                    System.out.println(
                            "Invalid email or password.");

                    return false;
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Database Error: "
                            + e.getMessage());

            return false;
        }
    }

    // =========================
    // GET USER ID
    // =========================
    public String getUserIdByEmail(String email) {

        String sql =
                "SELECT userId " +
                "FROM user_details " +
                "WHERE email = ?";

        try (Connection conn =
                     createDBConnection.getConnection();

             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs =
                         pstmt.executeQuery()) {

                if (rs.next()) {

                    return rs.getString("userId");
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error fetching user ID: "
                            + e.getMessage());
        }

        return "Error";
    }
}
