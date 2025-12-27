package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {

    // Map untuk menyimpan gambar yang sudah diload (Cache)
    private Map<String, BufferedImage> images = new HashMap<>();

    public void loadImage(String key, String path) {
        try {
            // Cek apakah file ada
            File f = new File(path);
            if (!f.exists()) {
                System.err.println("File gambar tidak ditemukan: " + path);
                return;
            }
            // Baca gambar
            BufferedImage img = ImageIO.read(f);
            images.put(key, img);
            System.out.println("Berhasil memuat gambar: " + key + " dari " + path);
        } catch (IOException e) {
            System.err.println("Gagal memuat gambar: " + path);
            e.printStackTrace();
        }
    }

    public BufferedImage getImage(String key) {
        return images.get(key);
    }
}