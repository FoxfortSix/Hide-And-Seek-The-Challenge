package model;

import java.sql.SQLException;

/**
 * Filename  : TabelBenefit.java
 * Package   : model
 * Description:
 * Handles CRUD operations specific to the 'tbenefit' table.
 * Inherits connection capabilities from the DB class.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class TabelBenefit extends DB {

    /**
     * Constructor: TabelBenefit
     * Calls the superclass (DB) constructor to initialize the connection.
     * * @throws Exception If connection fails.
     * @throws SQLException If a SQL error occurs.
     */
    public TabelBenefit() throws Exception, SQLException {
        super();
    }



    /**
     * Method: getBenefit
     * Retrieves ALL data from the 'tbenefit' table, ordered by score descending.
     * The result is stored in the parent class's 'rs' (ResultSet) variable.
     */
    public void getBenefit() {
        try {
            String query = "SELECT * FROM tbenefit ORDER BY skor DESC";
            createQuery(query);
        } catch (Exception e) {
            System.err.println("Error in getBenefit: " + e.toString());
        }
    }

    /**
     * Method: saveGameData
     * Saves the latest game progress to the database.
     * Logic:
     * 1. Check if the username exists.
     * 2. If EXISTS: UPDATE (add score, add missed bullets, replace ammo).
     * 3. If NOT EXISTS: INSERT new record.
     * * @param username The player's username (Primary Key).
     * @param scoreGained Score gained in this session (cumulative).
     * @param missedGained Missed bullets count in this session (cumulative).
     * @param currentBullets Current ammo remaining (replaces old value).
     */
    public void saveGameData(String username, int scoreGained, int missedGained, int currentBullets) {
        try {
            // 1. Check if user exists
            String checkQuery = "SELECT * FROM tbenefit WHERE username = '" + username + "'";
            createQuery(checkQuery);

            if (getResult().next()) {
                // 2. Data found -> UPDATE
                // Score and Missed Bullets are CUMULATIVE (added to existing)
                // Ammo is REPLACED (updated to current state)
                String updateQuery = "UPDATE tbenefit SET " +
                        "skor = skor + " + scoreGained + ", " +
                        "peluru_meleset = peluru_meleset + " + missedGained + ", " +
                        "sisa_peluru = " + currentBullets + " " +
                        "WHERE username = '" + username + "'";

                // Important: Close the checkQuery ResultSet before executing update
                closeResult();
                createUpdate(updateQuery);
                System.out.println("Data successfully updated for: " + username);

            } else {
                // 3. Data not found -> INSERT
                String insertQuery = "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (" +
                        "'" + username + "', " +
                        scoreGained + ", " +
                        missedGained + ", " +
                        currentBullets + ")";

                closeResult();
                createUpdate(insertQuery);
                System.out.println("New user created: " + username);
            }
        } catch (Exception e) {
            System.err.println("Failed to save game data: " + e.toString());
        }
    }
}