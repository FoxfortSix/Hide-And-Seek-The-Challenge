package model;

/**
 * Filename: Alien.java
 * Updated: Added Loadout capability (Shotgun, Rifle)
 */
public class Alien extends GameObject {

    public enum Type {
        CHASER,
        ZIGZAG
    }

    // --- TAMBAHAN BARU: LOADOUT ---
    public enum Loadout {
        DEFAULT,        // Pistol biasa (1 peluru)
        ASSAULT_RIFLE,  // Tembakan cepat, cooldown rendah
        SHOTGUN         // 3 Peluru menyebar, cooldown lama
    }

    private Type type;
    private Loadout loadout; // Senjata yang dibawa

    private double velX, velY;
    private long lastShotTime;
    private boolean isBursting = false;
    private int burstShotsFired = 0;
    private long lastBurstTime = 0;

    // Update Constructor untuk menerima Loadout (Opsional, atau set default)
    public Alien(double x, double y, Type type, Loadout loadout) {
        super(x, y, 30, 30);
        this.type = type;
        this.loadout = loadout;
        this.lastShotTime = System.currentTimeMillis();
    }

    // --- GETTERS & SETTERS ---
    public Type getType() { return type; }

    public Loadout getLoadout() { return loadout; }
    public void setLoadout(Loadout loadout) { this.loadout = loadout; }

    public double getVelX() { return velX; }
    public void setVelX(double velX) { this.velX = velX; }

    public double getVelY() { return velY; }
    public void setVelY(double velY) { this.velY = velY; }

    public long getLastShotTime() { return lastShotTime; }
    public void setLastShotTime(long lastShotTime) { this.lastShotTime = lastShotTime; }

    public boolean isBursting() { return isBursting; }
    public void setBursting(boolean bursting) { isBursting = bursting; }

    public int getBurstShotsFired() { return burstShotsFired; }
    public void setBurstShotsFired(int count) { this.burstShotsFired = count; }

    public long getLastBurstTime() { return lastBurstTime; }
    public void setLastBurstTime(long time) { this.lastBurstTime = time; }
}