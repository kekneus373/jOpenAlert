package com.github.kekneus373.jopenalert;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class SoundPlayer implements AutoCloseable {
    public static final String BEGIN = "begin";
    public static final String END = "end";
    private static final Logger LOG = Logger.getLogger(SoundPlayer.class.getName());
    private final ExecutorService playback = Executors.newSingleThreadExecutor(runnable -> {
        var thread = new Thread(runnable, "jOpenAlert-sound");
        thread.setDaemon(true);
        return thread;
    });
    private final Clip beginClip;
    private final Clip endClip;

    public SoundPlayer() {
        beginClip = load("/assets/begin.wav");
        endClip = load("/assets/end.wav");
    }

    public void play(String sound) {
        Clip clip = BEGIN.equals(sound) ? beginClip : endClip;
        if (clip == null) {
            return;
        }
        playback.execute(() -> {
            synchronized (clip) {
                clip.stop();
                clip.setFramePosition(0);
                clip.start();
            }
        });
    }

    @Override
    public void close() {
        playback.shutdownNow();
        if (beginClip != null) beginClip.close();
        if (endClip != null) endClip.close();
    }

    private Clip load(String resource) {
        try (InputStream input = SoundPlayer.class.getResourceAsStream(resource)) {
            if (input == null) {
                LOG.warning("Sound resource is missing: " + resource);
                return null;
            }
            InputStream bufferedStream = new BufferedInputStream(input);
            try (AudioInputStream audio = AudioSystem.getAudioInputStream(bufferedStream)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audio);
                return clip;
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exception) {
            LOG.log(Level.WARNING, "Unable to load sound resource: " + resource, exception);
            return null;
        }
    }
}