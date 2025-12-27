package model;

/**
 * Filename: Player.java
 * Updated: Added Weapon System (AR/Shotgun) with Timer.
 */
public class Player extends GameObject {

    public enum WeaponType {
        DEFAULT,        // Pistol Biasa
        ASSAULT_RIFLE,  // Rapid Fire (Spray)
        SHOTGUN         // Spread Fire
    }

    // Properti Player
    private double speed = 3.5;
    private int score = 0;
    private int ammo = 0; // Ammo awal
    private double rotation;

    // --- VARIABEL UNTUK FITUR SENJATA ---
    private WeaponType currentWeapon = WeaponType.DEFAULT;
    private long weaponPowerUpEndTime = 0; // Waktu kapan senjata spesial habis

    public Player(double x, double y) {
        super(x, y, 30, 30); // Ukuran player 30x30
    }

    // --- LOGIKA POWER UP (Baru) ---

    /**
     * Mengubah senjata player untuk durasi tertentu.
     * @param type Tipe senjata (ASSAULT_RIFLE / SHOTGUN)
     * @param durationSeconds Durasi dalam detik (misal 30)
     */
    public void setWeapon(WeaponType type, int durationSeconds) {
        this.currentWeapon = type;
        // Set waktu habis: Waktu sekarang + durasi (dalam milidetik)
        this.weaponPowerUpEndTime = System.currentTimeMillis() + (durationSeconds * 1000L);
    }

    /**
     * Dipanggil di GameLoop untuk mengecek apakah durasi senjata sudah habis.
     * Jika habis, senjata kembali ke DEFAULT.
     */
    public void checkWeaponTimer() {
        if (currentWeapon != WeaponType.DEFAULT) {
            if (System.currentTimeMillis() > weaponPowerUpEndTime) {
                currentWeapon = WeaponType.DEFAULT; // Reset ke pistol biasa
            }
        }
    }

    /**
     * Mendapatkan sisa waktu senjata spesial dalam detik.
     * Berguna untuk menampilkan timer di layar (HUD).
     */
    public int getWeaponTimeLeft() {
        if (currentWeapon == WeaponType.DEFAULT) return 0;

        long timeLeftMillis = weaponPowerUpEndTime - System.currentTimeMillis();
        if (timeLeftMillis < 0) return 0;

        return (int) (timeLeftMillis / 1000); // Konversi ke detik
    }

    public WeaponType getCurrentWeapon() {
        return currentWeapon;
    }

    // --- GETTER & SETTER STANDAR ---

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getAmmo() {
        return ammo;
    }

    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
}