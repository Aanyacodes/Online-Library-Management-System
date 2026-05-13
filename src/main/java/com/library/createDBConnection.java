package com.library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class createDBConnection {

    private static final String URL =
            System.getenv("DB_URL");

    private static final String USER =
            System.getenv("DB_USER");

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    static {

        try {

            Connection conn = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

            Statement stmt = conn.createStatement();

            // =========================
            // LIBRARIANS TABLE
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS librarians (
                    librarian_id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100),
                    email VARCHAR(100) UNIQUE,
                    password VARCHAR(255)
                )
            """);

            // =========================
            // MEMBERS TABLE
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    member_id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100),
                    email VARCHAR(100) UNIQUE,
                    password VARCHAR(255)
                )
            """);

            // =========================
            // BOOKS TABLE
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    book_id INT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255),
                    author VARCHAR(255),
                    quantity INT
                )
            """);

            // =========================
            // TRANSACTIONS TABLE
            // =========================
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
                    member_id INT,
                    book_id INT,
                    issue_date DATE,
                    return_date DATE,
                    FOREIGN KEY (member_id)
                    REFERENCES members(member_id),

                    FOREIGN KEY (book_id)
                    REFERENCES books(book_id)
                )
            """);

            // =========================
            // DEFAULT LIBRARIAN
            // =========================
            stmt.execute("""
                INSERT IGNORE INTO librarians
                (librarian_id, name, email, password)
                VALUES
                (
                    1,
                    'Admin',
                    'admin@gmail.com',
                    '$2a$12$7qJ9gGQ8Q7H7V0xJmJvK3eF0xH2dK1Y7V9eY6Qw0g6QxP5QmY8r7K'
                )
            """);

            // =========================
            // DEFAULT MEMBER
            // =========================
            stmt.execute("""
                INSERT IGNORE INTO members
                (member_id, name, email, password)
                VALUES
                (
                    1,
                    'Member User',
                    'member@gmail.com',
                    '$2a$12$7qJ9gGQ8Q7H7V0xJmJvK3eF0xH2dK1Y7V9eY6Qw0g6QxP5QmY8r7K'
                )
            """);

            System.out.println("✅ Database tables created successfully!");

            stmt.close();
            conn.close();

        } catch (Exception e) {

            System.out.println("Database setup failed!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}