package com.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {

    // =========================
    // ISSUE BOOK
    // =========================
    public boolean issueBook(String userId, String bookId) {

        String checkAvailability =
                "SELECT available_copies FROM book_details WHERE book_id = ?";

        String updateBook =
                "UPDATE book_details SET available_copies = available_copies - 1 WHERE book_id = ?";

        String insertTransaction =
                "INSERT INTO transaction_details " +
                "(userId, book_id, issue_date, due_date) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = null;

        try {
            conn = createDBConnection.getConnection();
            conn.setAutoCommit(false);

            // STEP 1: CHECK BOOK AVAILABILITY
            try (PreparedStatement pstmtCheck =
                         conn.prepareStatement(checkAvailability)) {

                pstmtCheck.setString(1, bookId);

                ResultSet rs = pstmtCheck.executeQuery();

                if (rs.next()) {

                    int copies = rs.getInt("available_copies");

                    if (copies <= 0) {
                        System.out.println("Book is out of stock.");
                        return false;
                    }

                } else {
                    System.out.println("Book ID not found.");
                    return false;
                }
            }

            // STEP 2: UPDATE BOOK INVENTORY
            try (PreparedStatement pstmtUpdate =
                         conn.prepareStatement(updateBook)) {

                pstmtUpdate.setString(1, bookId);
                pstmtUpdate.executeUpdate();
            }

            // STEP 3: INSERT TRANSACTION
            try (PreparedStatement pstmtInsert =
                         conn.prepareStatement(insertTransaction)) {

                LocalDate today = LocalDate.now();
                LocalDate dueDate = today.plusDays(14);

                pstmtInsert.setString(1, userId);
                pstmtInsert.setString(2, bookId);

                pstmtInsert.setDate(3,
                        java.sql.Date.valueOf(today));

                pstmtInsert.setDate(4,
                        java.sql.Date.valueOf(dueDate));

                pstmtInsert.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {

            System.err.println(
                    "Database Error during issueBook(): "
                            + e.getMessage());

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            return false;

        } finally {

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    // =========================
    // GET BORROWED BOOKS
    // =========================
    public List<Map<String, Object>> getBorrowedBooks(String userId) {

        List<Map<String, Object>> borrowedList =
                new ArrayList<>();

        String sql =
                "SELECT b.book_id, b.title, b.author, t.due_date " +
                "FROM book_details b " +
                "JOIN transaction_details t " +
                "ON b.book_id = t.book_id " +
                "WHERE t.userId = ? " +
                "AND t.return_date IS NULL";

        try (Connection conn =
                     createDBConnection.getConnection();

             PreparedStatement pstmt =
                     conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                Map<String, Object> book =
                        new HashMap<>();

                String currentBookId =
                        rs.getString("book_id");

                String title =
                        rs.getString("title");

                String author =
                        rs.getString("author");

                LocalDate dueDate =
                        rs.getDate("due_date").toLocalDate();

                LocalDate today =
                        LocalDate.now();

                long daysLate =
                        ChronoUnit.DAYS.between(dueDate, today);

                double fineDue =
                        (daysLate > 0)
                                ? daysLate * 1.0
                                : 0.0;

                book.put("bookId", currentBookId);
                book.put("title", title);
                book.put("author", author);
                book.put("dueDate", dueDate.toString());
                book.put("fineDue", fineDue);

                borrowedList.add(book);
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error fetching borrowed books: "
                            + e.getMessage());
        }

        return borrowedList;
    }

    // =========================
    // RETURN BOOK
    // =========================
    public double returnBook(String userId, String bookId) {

        String getTransactionInfo =
                "SELECT transaction_id, due_date " +
                "FROM transaction_details " +
                "WHERE userId = ? " +
                "AND book_id = ? " +
                "AND return_date IS NULL " +
                "LIMIT 1";

        String markAsReturned =
                "UPDATE transaction_details " +
                "SET return_date = CURDATE() " +
                "WHERE transaction_id = ?";

        String updateBookInventory =
                "UPDATE book_details " +
                "SET available_copies = available_copies + 1 " +
                "WHERE book_id = ?";

        String insertFine =
                "INSERT INTO fine_details " +
                "(userId, transaction_id, amount, fine_collection_date, status) " +
                "VALUES (?, ?, ?, CURDATE(), 'UNPAID')";

        double fineAmount = 0.0;
        int transactionId = -1;

        Connection conn = null;

        try {

            conn = createDBConnection.getConnection();
            conn.setAutoCommit(false);

            // STEP 1: FIND TRANSACTION
            try (PreparedStatement pstmtGet =
                         conn.prepareStatement(getTransactionInfo)) {

                pstmtGet.setString(1, userId);
                pstmtGet.setString(2, bookId);

                ResultSet rs = pstmtGet.executeQuery();

                if (rs.next()) {

                    transactionId =
                            rs.getInt("transaction_id");

                    LocalDate dueDate =
                            rs.getDate("due_date")
                                    .toLocalDate();

                    LocalDate today =
                            LocalDate.now();

                    long daysLate =
                            ChronoUnit.DAYS.between(
                                    dueDate,
                                    today
                            );

                    if (daysLate > 0) {
                        fineAmount = daysLate * 1.0;
                    }

                } else {

                    System.out.println(
                            "No active transaction found.");

                    return -1.0;
                }
            }

            // STEP 2: MARK RETURNED
            try (PreparedStatement pstmtReturn =
                         conn.prepareStatement(markAsReturned)) {

                pstmtReturn.setInt(1, transactionId);
                pstmtReturn.executeUpdate();
            }

            // STEP 3: UPDATE INVENTORY
            try (PreparedStatement pstmtBook =
                         conn.prepareStatement(updateBookInventory)) {

                pstmtBook.setString(1, bookId);
                pstmtBook.executeUpdate();
            }

            // STEP 4: INSERT FINE
            if (fineAmount > 0) {

                try (PreparedStatement pstmtFine =
                             conn.prepareStatement(insertFine)) {

                    pstmtFine.setString(1, userId);
                    pstmtFine.setInt(2, transactionId);
                    pstmtFine.setDouble(3, fineAmount);

                    pstmtFine.executeUpdate();
                }
            }

            conn.commit();

            return fineAmount;

        } catch (SQLException e) {

            System.err.println(
                    "Error during returnBook(): "
                            + e.getMessage());

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            return -1.0;

        } finally {

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}