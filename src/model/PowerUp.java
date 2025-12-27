package model;

import java.awt.Color;

public class PowerUp extends GameObject {

    public enum Type {
        ASSAULT_RIFLE, // Icon 'A'
        SHOTGUN        // Icon 'S'
    }

    private Type type;
    private long creationTime;
    private final long LIFETIME = 10000; // 10 detik sebelum hilang

    public PowerUp(double x, double y, Type type) {
        super(x, y, 30, 30); // Ukuran kotak 30x30
        this.type = type;
        this.creationTime = System.currentTimeMillis();
    }

    public Type getType() { return type; }

    // Cek apakah sudah waktunya ikon ini menghilang (lebih dari 10 detik)
    public boolean isExpired() {
        return (System.currentTimeMillis() - creationTime) > LIFETIME;
    }

    public Color getColor() {
        return (type == Type.ASSAULT_RIFLE) ? Color.RED : new Color(255, 200, 0);
    }

    public String getLetter() {
        return (type == Type.ASSAULT_RIFLE) ? "A" : "S";
    }
}