package model;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * Provides data access operations for the {@code tbenefit} database table.
 * <p>
 * The {@code TabelBenefit} class extends {@link DB} and encapsulates
 * all SQL logic related to player benefit data, including score,
 * ammunition, and shooting accuracy.
 * </p>
 *
 * <p>
 * Responsibilities of this class include:
 * <ul>
 *     <li>Registering new players</li>
 *     <li>Retrieving leaderboard data</li>
 *     <li>Loading player-specific statistics</li>
 *     <li>Persisting game progress</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class functions as a table-specific Data Access Object (DAO)
 * and isolates SQL queries from higher application layers.
 * </p>
 *
 * @author Mochammad Azka Basria
 */
public class TabelBenefit extends DB {

    /**
     * Constructs a new {@code TabelBenefit} instance.
     * <p>
     * This constructor initializes the database connection
     * by invoking the superclass constructor.
     * </p>
     *
     * @throws Exception   if database connection initialization fails
     * @throws SQLException if a SQL-related error occurs
     */
    public TabelBenefit() throws Exception, SQLException {
        super();
    }

    /**
     * Retrieves all player benefit records from the database.
     * <p>
     * Data is ordered by score in descending order and stored
     * in the inherited {@link #rs} result set.
     * </p>
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
     * Registers a player if the username does not already exist.
     * <p>
     * The registration process follows these steps:
     * <ol>
     *     <li>Check whether the username exists in the database</li>
     *     <li>If not found, insert a new record with default values</li>
     *     <li>If found, skip insertion and reuse existing data</li>
     * </ol>
     * </p>
     *
     * @param username the unique username identifying the player
     */
    public void registerPlayer(String username) {
        try {
            String checkQuery =
                    "SELECT * FROM tbenefit WHERE username = '" + username + "'";
            createQuery(checkQuery);

            if (!getResult().next()) {
                closeResult();

                String insertQuery =
                        "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (" +
                                "'" + username + "', 0, 0, 0)";
                createUpdate(insertQuery);
                System.out.println("New player registered: " + username);
            } else {
                closeResult();
                System.out.println("Welcome back, " + username);
            }
        } catch (Exception e) {
            System.err.println("Failed to register player: " + e.toString());
        }
    }

    /**
     * Retrieves all benefit data in a tabular format.
     * <p>
     * This method converts database rows into a two-dimensional
     * object array, making it suitable for UI components such
     * as tables or scoreboards.
     * </p>
     *
     * @return a two-dimensional array containing benefit data
     */
    public Object[][] getAllData() {
        List<Object[]> list = new ArrayList<>();

        try {
            String query = "SELECT * FROM tbenefit ORDER BY skor DESC";
            createQuery(query);

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("username"),
                        rs.getInt("skor"),
                        rs.getInt("peluru_meleset"),
                        rs.getInt("sisa_peluru")
                });
            }

            closeResult();
        } catch (Exception e) {
            System.err.println("Load data failed: " + e);
        }

        Object[][] data = new Object[list.size()][4];
        for (int i = 0; i < list.size(); i++) {
            data[i] = list.get(i);
        }
        return data;
    }

    /**
     * Retrieves the remaining ammunition for a specific player.
     *
     * @param username the player's username
     * @return the remaining ammunition count
     */
    public int getAmmoByUsername(String username) {
        int ammo = 0;

        try {
            String query =
                    "SELECT sisa_peluru FROM tbenefit WHERE username = '" + username + "'";
            createQuery(query);

            if (rs.next()) {
                ammo = rs.getInt("sisa_peluru");
            }

            closeResult();
        } catch (Exception e) {
            System.err.println("Failed to get ammo: " + e);
        }

        return ammo;
    }

    /**
     * Persists the player's latest game progress.
     * <p>
     * The save operation follows this logic:
     * <ol>
     *     <li>Check if the player record already exists</li>
     *     <li>If it exists, update cumulative values</li>
     *     <li>If it does not exist, insert a new record</li>
     * </ol>
     * </p>
     *
     * @param username       the player's username
     * @param scoreGained    score gained during the session
     * @param missedGained   missed bullets during the session
     * @param currentBullets current remaining ammunition
     */
    public void saveGameData(
            String username,
            int scoreGained,
            int missedGained,
            int currentBullets) {

        try {
            String checkQuery =
                    "SELECT * FROM tbenefit WHERE username = '" + username + "'";
            createQuery(checkQuery);

            if (getResult().next()) {
                String updateQuery =
                        "UPDATE tbenefit SET " +
                                "skor = skor + " + scoreGained + ", " +
                                "peluru_meleset = peluru_meleset + " + missedGained + ", " +
                                "sisa_peluru = " + currentBullets + " " +
                                "WHERE username = '" + username + "'";

                closeResult();
                createUpdate(updateQuery);
                System.out.println("Data updated for: " + username);
            } else {
                closeResult();
                String insertQuery =
                        "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (" +
                                "'" + username + "', " + scoreGained + ", " +
                                missedGained + ", " + currentBullets + ")";
                createUpdate(insertQuery);
            }
        } catch (Exception e) {
            System.err.println("Failed to save game data: " + e.toString());
        }
    }
}
