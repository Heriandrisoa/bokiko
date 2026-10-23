package com.example.auth.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Minimal connection provider. Swap for a pooled DataSource (JNDI / HikariCP) in production. */
public final class Db {
    private static final String URL = Config.get("url", "DB_URL", null);
    private static final String USER = Config.get("user", "DB_USER", null);
    private static final String PASSWORD = Config.get("password", "DB_PASSWORD", null);

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Db() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
