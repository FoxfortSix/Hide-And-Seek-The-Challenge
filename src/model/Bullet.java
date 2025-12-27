package model;

import java.awt.Color;
import java.awt.Rectangle;

/**
 * Filename: Bullet.java
 * Description:
 * Merepresentasikan objek peluru.
 * Updated: Sekarang mendukung ImageKey untuk render sprite berbeda dan rotasi.
 */
public class Bullet extends GameObject {

    private double velX;
    private double velY;
    private Color color;    // Warna fallback jika gambar gagal dimuat
    private String imageKey; // Kunci untuk mengambil gambar dari ImageManager

    /**
     * Constructor Bullet
     * @param x Posisi awal X
     * @param y Posisi awal Y
     * @param velX Kecepatan X
     * @param velY Kecepatan Y
     * @param color Warna (untuk fallback)
     * @param imageKey Kunci gambar (misal: "BULLET_PLAYER_PISTOL")
     */
    public Bullet(double x, double y, double velX, double velY, Color color, String imageKey) {
        // Kita set ukuran hitbox peluru menjadi 20x10 (lebar x tinggi)
        // Bentuk persegi panjang ini cocok karena peluru akan dirotasi
        super(x, y, 20, 10);
        this.velX = velX;
        this.velY = velY;
        this.color = color;
        this.imageKey = imageKey;
    }

    public void update() {
        x += velX;
        y += velY;
    }

    // --- GETTERS ---

    public String getImageKey() {
        return imageKey;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Menghitung sudut rotasi peluru dalam radian berdasarkan vektor kecepatannya.
     * Digunakan oleh View agar moncong peluru selalu menghadap ke arah gerak.
     * @return Sudut rotasi dalam radian.
     */
    public double getRotation() {
        return Math.atan2(velY, velX);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
}