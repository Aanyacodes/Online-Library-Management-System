package com.library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class createDBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/LMSAdmin";

    private static final String USER =
            "LMSAdmin";

    private static final String PASSWORD =
            "Lm$Adm!n@2026";

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}
