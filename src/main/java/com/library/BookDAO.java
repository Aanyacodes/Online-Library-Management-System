package com.library;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookDAO {

    // =========================
    // ADD BOOK
    // =========================
    public boolean addBook(
            String title,
            String author,
            String isbn,
            String publisher,
            String genre,
            int publishedYear,
            int totalCopies,
            int availableCopies) {

        String sql =
                "INSERT INTO book_details " +
                "(book_id, title, author, isbn, publisher, genre, published_year, total_copies, available_copies) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String bookId =
                "BOOK-" +
                java.util.UUID.randomUUID()
                        .toString()
                        .substring(0, 5)
                        .toUpperCase();

        try (Connection conn =
                     createDBConnection.getConnection();

             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setString(1, bookId);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setString(4, isbn);
            pstmt.setString(5, publisher);
            pstmt.setString(6, genre);
            pstmt.setInt(7, publishedYear);
            pstmt.setInt(8, totalCopies);
            pstmt.setInt(9, availableCopies);

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
    // VIEW ALL BOOKS
    // =========================
    public List<Map<String, Object>> viewAllBooks() {

        List<Map<String, Object>> bookList =
                new ArrayList<>();

        String sql =
                "SELECT * FROM book_details";

        try (Connection conn =
                     createDBConnection.getConnection();

             PreparedStatement pstmt =
                     conn.prepareStatement(sql);

             ResultSet rs =
                     pstmt.executeQuery()) {

            while (rs.next()) {

                Map<String, Object> book =
                        new HashMap<>();

                book.put(
                        "id",
                        rs.getString("book_id"));

                book.put(
                        "title",
                        rs.getString("title"));

                book.put(
                        "author",
                        rs.getString("author"));

                book.put(
                        "isbn",
                        rs.getString("isbn"));

                book.put(
                        "publisher",
                        rs.getString("publisher"));

                book.put(
                        "genre",
                        rs.getString("genre"));

                book.put(
                        "year",
                        rs.getInt("published_year"));

                book.put(
                        "total",
                        rs.getInt("total_copies"));

                book.put(
                        "available",
                        rs.getInt("available_copies"));

                bookList.add(book);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Database Error: "
                            + e.getMessage());
        }

        return bookList;
    }
}