package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {

    private Map<String, BufferedImage> images = new HashMap<>();

    public void loadImage(String key, String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                System.err.println("File gambar tidak ditemukan: " + path);
                return;
            }
            BufferedImage img = ImageIO.read(f);
            images.put(key, img);
        } catch (IOException e) {
            System.err.println("Gagal memuat gambar: " + path);
            e.printStackTrace();
        }
    }

    public BufferedImage getImage(String key) {
        return images.get(key);
    }
}