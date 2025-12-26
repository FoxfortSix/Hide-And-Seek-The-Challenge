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
 * UPGRADED VERSION: Supports "Audio Pooling" (Polyphony).
 * Allows rapid-fire sounds to overlap without cutting each other off or delaying.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class SoundManager {

    // Map menyimpan LIST of Clips, bukan cuma 1 Clip
    // Key: "SHOOT", Value: [Clip1, Clip2, Clip3, Clip4, Clip5]
    private Map<String, List<Clip>> soundPool;
    private Map<String, Integer> currentIndices; // Untuk melacak giliran clip mana yang dipakai

    public SoundManager() {
        soundPool = new HashMap<>();
        currentIndices = new HashMap<>();
    }

    /**
     * Loads a sound file and creates multiple copies (pool) for rapid playback.
     * * @param key The name/ID (e.g., "SHOOT").
     * @param filePath Path to .wav file.
     */
    public void loadSound(String key, String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + filePath);
                return;
            }

            // 1. Baca data audio mentah ke memori (byte array)
            // Kita harus baca dulu karena Stream hanya bisa dibaca sekali.
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            AudioFormat format = audioIn.getFormat();

            // Trik: Convert ke PCM_SIGNED jika format tidak didukung langsung (opsional, tapi aman)
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            // Baca stream ke byte array
            byte[] audioData = readStream(audioIn);

            // 2. Tentukan jumlah pool
            // Jika BGM, cukup 1. Jika SFX (tembakan), buat 5 copy biar bisa rapid fire.
            int poolSize = key.equals("BGM") ? 1 : 20;

            List<Clip> clipList = new ArrayList<>();

            for (int i = 0; i < poolSize; i++) {
                // Bikin Stream baru dari byte array yang sama
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                AudioInputStream poolStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());

                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(poolStream);
                clipList.add(clip);
            }

            soundPool.put(key, clipList);
            currentIndices.put(key, 0); // Mulai dari index 0

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound '" + key + "': " + e.getMessage());
        }
    }

    // Helper untuk membaca InputStream ke byte[]
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
     * Plays a sound. Automatically rotates through the pool for overlapping effects.
     * * @param key The key of the sound.
     */
    public void play(String key) {
        List<Clip> clips = soundPool.get(key);
        if (clips == null || clips.isEmpty()) return;

        // Ambil index giliran sekarang
        int index = currentIndices.get(key);
        Clip clip = clips.get(index);

        // Putar suara
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();

        // Update giliran ke clip berikutnya (Round Robin)
        // Contoh: 0 -> 1 -> 2 -> 3 -> 4 -> 0 -> 1 ...
        index++;
        if (index >= clips.size()) {
            index = 0;
        }
        currentIndices.put(key, index);
    }

    /**
     * Loops a sound continuously (Only for BGM).
     */
    public void loop(String key) {
        List<Clip> clips = soundPool.get(key);
        if (clips != null && !clips.isEmpty()) {
            // Untuk BGM selalu ambil index 0
            Clip clip = clips.get(0);
            if (!clip.isRunning()) {
                clip.setFramePosition(0);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }
}