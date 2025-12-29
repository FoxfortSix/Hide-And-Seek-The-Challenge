package view;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filename  : SoundManager.java
 * Package   : view
 * Description:
 * A utility class to manage loading and playing sound effects (.wav).
 * <p>
 * UPGRADED VERSION: Supports "Audio Pooling" (Polyphony).
 * Allows rapid-fire sounds to overlap without cutting each other off or delaying.
 * Instead of a single clip, it maintains a pool of identical clips for each sound.
 * </p>
 *
 * @author Mochammad Azka Basria
 * @version 1.0
 */
public class SoundManager {

    /**
     * Map storing a LIST of Clips for each sound key.
     * Key: String ID (e.g., "SHOOT")
     * Value: A list of Clip objects (e.g., [Clip1, Clip2, Clip3...])
     */
    private Map<String, List<Clip>> soundPool;

    /** Tracks the current index of the clip to play for each sound key (Round Robin). */
    private Map<String, Integer> currentIndices;

    /**
     * Constructs a new SoundManager.
     */
    public SoundManager() {
        soundPool = new HashMap<>();
        currentIndices = new HashMap<>();
    }

    /**
     * Loads a sound file and creates multiple copies (pool) for rapid playback.
     * <p>
     * Reads the raw audio data into memory first to create independent streams
     * for each Clip instance in the pool.
     * </p>
     *
     * @param key      The unique identifier (e.g., "SHOOT").
     * @param filePath Path to the .wav file.
     */
    public void loadSound(String key, String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + filePath);
                return;
            }

            // 1. Read raw audio data into memory (byte array)
            // We must read it first because an AudioInputStream can only be read once.
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = audioIn.getFormat();

            // Trick: Convert to PCM_SIGNED if needed for compatibility (optional but safe)
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            // Read stream to byte array
            byte[] audioData = readStream(audioIn);

            // 2. Determine pool size
            // If BGM, only 1 copy is needed. If SFX (e.g., shooting), create 20 copies for rapid fire.
            int poolSize = key.equals("BGM") ? 1 : 20;

            List<Clip> clipList = new ArrayList<>();

            for (int i = 0; i < poolSize; i++) {
                // Create a new Stream from the same byte array
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                AudioInputStream poolStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());

                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(poolStream);
                clipList.add(clip);
            }

            soundPool.put(key, clipList);
            currentIndices.put(key, 0); // Start from index 0

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound '" + key + "': " + e.getMessage());
        }
    }

    /**
     * Helper method to read an AudioInputStream into a byte array.
     *
     * @param stream The source audio stream.
     * @return A byte array containing the audio data.
     * @throws IOException If an I/O error occurs.
     */
    private byte[] readStream(AudioInputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    /**
     * Plays a sound effect. Automatically rotates through the pool (Round Robin)
     * to allow overlapping sounds.
     *
     * @param key The identifier of the sound to play.
     */
    public void play(String key) {
        List<Clip> clips = soundPool.get(key);
        if (clips == null || clips.isEmpty()) return;

        // Get the current turn index
        int index = currentIndices.get(key);
        Clip clip = clips.get(index);

        // Play sound
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();

        // Update turn to next clip (Round Robin)
        // Example: 0 -> 1 -> 2 -> 3 -> 4 -> 0 -> 1 ...
        index++;
        if (index >= clips.size()) {
            index = 0;
        }
        currentIndices.put(key, index);
    }

    /**
     * Loops a sound continuously (Primary use: BGM).
     *
     * @param key The identifier of the sound to loop.
     */
    public void loop(String key) {
        List<Clip> clips = soundPool.get(key);
        if (clips != null && !clips.isEmpty()) {
            // For BGM, always use index 0
            Clip clip = clips.get(0);
            if (!clip.isRunning()) {
                clip.setFramePosition(0);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }
}