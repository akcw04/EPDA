package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility.
 * Provides methods to establish connections to the database.
 * Note: In production, use connection pooling via GlassFish JDBC Resource.
 */
public class DatabaseConnection {

    // Database connection URL with embedded credentials for Apache Derby
    private static final String DB_URL = "jdbc:derby://localhost:1527/sample;user=app;password=app";
    private static final String DB_DRIVER = "org.apache.derby.jdbc.ClientDriver";

    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load database driver: " + DB_DRIVER);
            e.printStackTrace();
        }
    }

    /**
     * Get a connection to the database.
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Close a connection gracefully.
     *
     * @param connection the connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
