package com.appointmentsystem.util;

import appointmentsystem.db.DatabaseConnection;
import java.sql.Connection;

/**
 * Database connection wrapper that delegates to the real DatabaseConnection
 * in the appointmentsystem.db package.
 */
public class DBConnection {

    public static Connection getConnection() throws Exception {
        return DatabaseConnection.getConnection();
    }
}
