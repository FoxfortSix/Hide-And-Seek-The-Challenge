package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filename  : ImageManager.java
 * Package   : view
 * Description:
 * Utility class for managing image resources.
 * <p>
 * Handles loading images from the disk and storing them in a cache (Map)
 * to prevent redundant usage of I/O operations during runtime.
 * </p>
 *
 * @author Mochammad Azka Basria
 * @version 1.0
 */
public class ImageManager {

    /** Cache for storing loaded images by key. */
    private Map<String, BufferedImage> images = new HashMap<>();

    /**
     * Loads an image from the specified path and stores it with a unique key.
     *
     * @param key  The unique identifier for the image.
     * @param path The relative or absolute path to the image file.
     */
    public void loadImage(String key, String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                System.err.println("Image file not found: " + path);
                return;
            }
            BufferedImage img = ImageIO.read(f);
            images.put(key, img);
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a stored image by its key.
     *
     * @param key The unique identifier of the image.
     * @return The {@link BufferedImage} associated with the key, or null if not found.
     */
    public BufferedImage getImage(String key) {
        return images.get(key);
    }
}