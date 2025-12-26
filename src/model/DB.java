package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Filename  : DB.java
 * Package   : model
 * Description:
 * Handles basic MySQL database connections and raw query execution.
 * This class serves as the parent class for specific table Data Access Objects (DAOs).
 *
 * Programmer: [Your Name]
 * Date      : 2025-12-24
 */
public class DB {

    // Database Configuration
    private String conAddress = "jdbc:mysql://localhost:3306/db_hide_seek?user=root&password=&useSSL=false&allowPublicKeyRetrieval=true";
    // JDBC Components
    protected Statement stmt = null;
    protected ResultSet rs = null;
    protected Connection conn = null;

    /**
     * Constructor: DB
     * Establishes a connection to the database immediately upon instantiation.
     * * @throws Exception If the JDBC driver is not found.
     * @throws SQLException If the connection to the database fails.
     */
    public DB() throws Exception, SQLException {
        try {
            // Load MySQL Driver (Ensure mysql-connector-j is in your library path)
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            // Establish Connection
            conn = DriverManager.getConnection(conAddress);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

        } catch (SQLException es) {
            // Throw exception to be handled by the caller
            throw es;
        }
    }

    /**
     * Method: createQuery
     * Executes a SELECT query that returns a ResultSet.
     * * @param query The SQL query string (e.g., "SELECT * FROM table").
     * @throws Exception If a general error occurs.
     * @throws SQLException If the SQL syntax is invalid or execution fails.
     */
    public void createQuery(String query) throws Exception, SQLException {
        try {
            stmt = conn.createStatement();
            // Execute the query
            if (stmt.execute(query)) {
                // Retrieve the result set and store it in the 'rs' variable
                rs = stmt.getResultSet();
            }
        } catch (SQLException es) {
            throw es;
        }
    }

    /**
     * Method: createUpdate
     * Executes data manipulation queries (INSERT, UPDATE, DELETE).
     * Does not return a ResultSet.
     * * @param query The SQL query string.
     * @throws Exception If a general error occurs.
     * @throws SQLException If the execution fails.
     */
    public void createUpdate(String query) throws Exception, SQLException {
        try {
            stmt = conn.createStatement();
            int result = stmt.executeUpdate(query);
        } catch (SQLException es) {
            throw es;
        }
    }

    /**
     * Method: getResult
     * Retrieves the ResultSet from the last executed query.
     * * @return The current ResultSet, or null if retrieval fails.
     * @throws Exception If an error occurs during retrieval.
     */
    public ResultSet getResult() throws Exception {
        ResultSet temp = null;
        try {
            return rs;
        } catch (Exception ex) {
            return temp;
        }
    }

    /**
     * Method: closeResult
     * Closes the ResultSet and Statement to free up resources.
     * Must be called after data retrieval is complete.
     * * @throws Exception If an error occurs during closure.
     */
    public void closeResult() throws Exception {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqlEx) {
                rs = null;
                throw sqlEx;
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException sqlEx) {
                stmt = null;
                throw sqlEx;
            }
        }
    }

    /**
     * Method: closeConnection
     * Closes the physical connection to the database.
     * * @throws Exception If closing the connection fails.
     */
    public void closeConnection() throws Exception {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqlEx) {
                conn = null;
            }
        }
    }
}