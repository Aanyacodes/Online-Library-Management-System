package com.library;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        // ==========================================
        // DATABASE OBJECTS
        // ==========================================

        UserDAO userDAO = new UserDAO();
        LibrarianDAO librarianDAO = new LibrarianDAO();
        BookDAO bookDAO = new BookDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        // ==========================================
        // PORT CONFIGURATION
        // ==========================================

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "7070")
        );

        // ==========================================
        // CREATE JAVALIN APP
        // ==========================================

        Javalin app = Javalin.create(config -> {

            // Serve frontend files
            config.staticFiles.add("/public", Location.CLASSPATH);

            // Enable CORS
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> it.anyHost());
            });

        });

        // ==========================================
        // HEALTH CHECK API
        // ==========================================

        app.get("/api/test", ctx -> {
            ctx.result("Backend is working successfully!");
        });

        // ==========================================
        // LOGIN API
        // ==========================================

        app.post("/api/login", ctx -> {

            Map<String, String> loginData = ctx.bodyAsClass(Map.class);

            String email = loginData.get("email");
            String password = loginData.get("password");
            String role = loginData.get("role");

            if ("librarian".equals(role)) {

                String libId = librarianDAO.loginLibrarian(email, password);

                if (libId != null) {

                    ctx.json(Map.of(
                            "status", "success",
                            "role", "librarian",
                            "userId", libId
                    ));

                } else {

                    ctx.status(401).json(Map.of(
                            "status", "error",
                            "message", "Invalid Librarian credentials"
                    ));
                }

            } else {

                boolean success = userDAO.loginUser(email, password);

                if (success) {

                    String userId = userDAO.getUserIdByEmail(email);

                    ctx.json(Map.of(
                            "status", "success",
                            "role", "member",
                            "userId", userId
                    ));

                } else {

                    ctx.status(401).json(Map.of(
                            "status", "error",
                            "message", "Invalid Member credentials"
                    ));
                }
            }
        });

        // ==========================================
        // ADD BOOK API
        // ==========================================

        app.post("/api/books", ctx -> {

            Map<String, String> bookData = ctx.bodyAsClass(Map.class);

            String title = bookData.get("title");
            String author = bookData.get("author");
            String isbn = bookData.get("isbn");
            String publisher = bookData.get("publisher");
            String genre = bookData.get("genre");

            int year = Integer.parseInt(bookData.get("year"));
            int copies = Integer.parseInt(bookData.get("copies"));
            int availableCopies = Integer.parseInt(bookData.get("available_copies"));

            boolean success = bookDAO.addBook(
                    title,
                    author,
                    isbn,
                    publisher,
                    genre,
                    year,
                    copies,
                    availableCopies
            );

            if (success) {

                ctx.json(Map.of(
                        "status", "success",
                        "message", "Book added successfully"
                ));

            } else {

                ctx.status(500).json(Map.of(
                        "status", "error",
                        "message", "Could not add book"
                ));
            }
        });

        // ==========================================
        // VIEW ALL BOOKS API
        // ==========================================

        app.get("/api/books", ctx -> {

            List<Map<String, Object>> books = bookDAO.viewAllBooks();

            ctx.json(books);
        });

        // ==========================================
        // BORROW BOOK API
        // ==========================================

        app.post("/api/borrow", ctx -> {

            Map<String, String> requestData = ctx.bodyAsClass(Map.class);

            String userId = requestData.get("userId");
            String bookId = requestData.get("bookId");

            boolean success = transactionDAO.issueBook(userId, bookId);

            if (success) {

                ctx.json(Map.of(
                        "status", "success",
                        "message", "Book borrowed successfully"
                ));

            } else {

                ctx.status(400).json(Map.of(
                        "status", "error",
                        "message", "Book unavailable"
                ));
            }
        });

        // ==========================================
        // VIEW BORROWED BOOKS API
        // ==========================================

        app.get("/api/borrowed/{userId}", ctx -> {

            String userId = ctx.pathParam("userId");

            List<Map<String, Object>> borrowedBooks =
                    transactionDAO.getBorrowedBooks(userId);

            ctx.json(borrowedBooks);
        });

        // ==========================================
        // RETURN BOOK API
        // ==========================================

        app.post("/api/return", ctx -> {

            Map<String, String> requestData = ctx.bodyAsClass(Map.class);

            String userId = requestData.get("userId");
            String bookId = requestData.get("bookId");

            double fine = transactionDAO.returnBook(userId, bookId);

            if (fine >= 0) {

                if (fine > 0) {

                    String msg =
                            "Book returned successfully. Fine: ₹"
                                    + String.format("%.2f", fine);

                    ctx.json(Map.of(
                            "status", "success",
                            "message", msg
                    ));

                } else {

                    ctx.json(Map.of(
                            "status", "success",
                            "message", "Book returned successfully"
                    ));
                }

            } else {

                ctx.status(400).json(Map.of(
                        "status", "error",
                        "message", "Return failed"
                ));
            }
        });

        // ==========================================
        // REGISTER MEMBER API
        // ==========================================

        app.post("/api/members", ctx -> {

            Map<String, String> data = ctx.bodyAsClass(Map.class);

            boolean success = userDAO.registerUser(
                    data.get("first_name"),
                    data.get("last_name"),
                    data.get("email"),
                    data.get("phone"),
                    data.get("password"),
                    data.get("address")
            );

            if (success) {

                ctx.json(Map.of(
                        "status", "success",
                        "message", "Member registered successfully"
                ));

            } else {

                ctx.status(400).json(Map.of(
                        "status", "error",
                        "message", "Registration failed"
                ));
            }
        });

        // ==========================================
        // REGISTER LIBRARIAN API
        // ==========================================

        app.post("/api/librarians", ctx -> {

            Map<String, String> data = ctx.bodyAsClass(Map.class);

            boolean success = librarianDAO.registerLibrarian(
                    data.get("first_name"),
                    data.get("last_name"),
                    data.get("email"),
                    data.get("phone"),
                    data.get("password"),
                    data.get("address")
            );

            if (success) {

                ctx.json(Map.of(
                        "status", "success",
                        "message", "Librarian registered successfully"
                ));

            } else {

                ctx.status(400).json(Map.of(
                        "status", "error",
                        "message", "Registration failed"
                ));
            }
        });

        // ==========================================
        // START SERVER
        // ==========================================

        app.get("/create-test-librarian", ctx -> {

    boolean success =
        librarianDAO.registerLibrarian(
            "Admin",
            "saanya@gmail.com",
            "123456"
        );

    if(success){

        ctx.result("Test librarian created");

    } else {

        ctx.result("Librarian already exists");
    }

});
        app.start(port);

        System.out.println(
                "🚀 LMS Web Server is running on port " + port
        );
    }
}