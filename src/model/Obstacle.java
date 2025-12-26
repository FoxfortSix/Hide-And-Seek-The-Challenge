package model;

/**
 * Filename  : Obstacle.java
 * Package   : model
 * Description:
 * Represents a static obstacle (e.g., a rock) in the game world.
 * Updated to support variable sizes.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class Obstacle extends GameObject {

    /**
     * Constructor Default (Ukuran standar 50x50)
     */
    public Obstacle(double x, double y) {
        super(x, y, 50, 50);
    }

    /**
     * Constructor Baru (Ukuran Custom)
     * Digunakan untuk variasi ukuran batu.
     * @param width Lebar batu
     * @param height Tinggi batu
     */
    public Obstacle(double x, double y, int width, int height) {
        super(x, y, width, height);
    }
}