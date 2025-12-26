package model;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

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

    public void registerPlayer(String username) {
        try {
            // 1. Cek apakah user sudah ada
            String checkQuery = "SELECT * FROM tbenefit WHERE username = '" + username + "'";
            createQuery(checkQuery);

            if (!getResult().next()) {
                // 2. Jika TIDAK ADA, Insert data baru dengan nilai 0
                // Penting: Tutup result set sebelumnya sebelum query baru
                closeResult();

                String insertQuery = "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (" +
                        "'" + username + "', 0, 0, 0)";
                createUpdate(insertQuery);
                System.out.println("New player registered: " + username);
            } else {
                // Jika sudah ada, tutup result set saja
                closeResult();
                System.out.println("Welcome back, " + username);
            }
        } catch (Exception e) {
            System.err.println("Failed to register player: " + e.toString());
        }
    }

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

    public int getAmmoByUsername(String username) {
        int ammo = 0;
        try {
            String query = "SELECT sisa_peluru FROM tbenefit WHERE username = '" + username + "'";
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
            String checkQuery = "SELECT * FROM tbenefit WHERE username = '" + username + "'";
            createQuery(checkQuery);

            if (getResult().next()) {
                // UPDATE (Menambahkan skor ke yang sudah ada)
                String updateQuery = "UPDATE tbenefit SET " +
                        "skor = skor + " + scoreGained + ", " +
                        "peluru_meleset = peluru_meleset + " + missedGained + ", " +
                        "sisa_peluru = " + currentBullets + " " +
                        "WHERE username = '" + username + "'";

                closeResult();
                createUpdate(updateQuery);
                System.out.println("Data updated for: " + username);
            } else {
                // Fallback jika entah kenapa data belum ada (misal error saat register)
                closeResult();
                String insertQuery = "INSERT INTO tbenefit (username, skor, peluru_meleset, sisa_peluru) VALUES (" +
                        "'" + username + "', " + scoreGained + ", " + missedGained + ", " + currentBullets + ")";
                createUpdate(insertQuery);
            }
        } catch (Exception e) {
            System.err.println("Failed to save game data: " + e.toString());
        }
    }
}