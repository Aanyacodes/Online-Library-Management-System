package com.library;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // =========================
    // HASH PASSWORD
    // =========================
    public static String hashPassword(
            String plainTextPassword) {

        return BCrypt.hashpw(
                plainTextPassword,
                BCrypt.gensalt(12)
        );
    }

    // =========================
    // VERIFY PASSWORD
    // =========================
    public static boolean checkPassword(
            String plainTextPassword,
            String hashedPassword) {

        return BCrypt.checkpw(
                plainTextPassword,
                hashedPassword
        );
    }
}