package com.github.kekneus373.jopenalert;

import java.awt.GraphicsEnvironment;
import java.awt.SystemTray;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static List<String> regionNames = List.of();
    private static FileChannel lockChannel;
    private static FileLock lock;
    private static TrayManager tray;
    private static Poller poller;
    private static SoundPlayer sounds;
    private static ScheduledExecutorService scheduler;

    public static void main(String[] args) throws Exception {
        if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) {
            throw new IllegalStateException(Config.TRAY_UNAVAILABLE_MESSAGE);
        }
        Path configFile = Config.configFile();
        Files.createDirectories(configFile.getParent());
        lockChannel = FileChannel.open(configFile.resolveSibling("jOpenAlert.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        lock = lockChannel.tryLock();
        if (lock == null) throw new IllegalStateException("Another jOpenAlert instance is already running");
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            var thread = new Thread(runnable, "jOpenAlert-network");
            thread.setDaemon(true);
            return thread;
        });
        var api = new AlertApi();
        Config config;
        if (Files.exists(configFile)) {
            config = Config.load(configFile);
            regionNames = new ArrayList<>(api.fetchRegions().keySet());
        } else {
            Map<String, Boolean> regions = scheduler.submit(api::fetchRegions).get();
            regionNames = new ArrayList<>(regions.keySet());
            if (regionNames.isEmpty()) throw new IOException(Config.NO_REGIONS_MESSAGE);
            String selected = chooseRegion();
            if (selected == null) return;
            config = Config.newConfig(configFile, selected);
            config.save();
        }
        sounds = new SoundPlayer();
        poller = new Poller(api, config, sounds, scheduler);
        var map = new MapWindow(api, scheduler);
        tray = new TrayManager(config, new AutoStart(), poller, map, Main::shutdown);
        SwingUtilities.invokeAndWait(() -> {
            try { tray.install(); }
            catch (java.awt.AWTException exception) { throw new IllegalStateException(exception); }
        });
        poller.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(false), "jOpenAlert-shutdown"));
    }

    static List<String> regionNames() { return regionNames; }

    private static String chooseRegion() {
        var result = JOptionPane.showInputDialog(null, Config.PICK_REGION_MESSAGE, Config.PICK_REGION_TITLE,
                JOptionPane.QUESTION_MESSAGE, null, regionNames.toArray(), regionNames.get(0));
        return result == null ? null : result.toString();
    }

    private static synchronized void shutdown(boolean exit) {
        if (poller != null) poller.stop();
        if (tray != null) tray.remove();
        if (sounds != null) sounds.close();
        if (scheduler != null) scheduler.shutdownNow();
        try { if (lock != null) lock.release(); } catch (IOException exception) { LOG.log(Level.FINE, "Unable to release lock", exception); }
        try { if (lockChannel != null) lockChannel.close(); } catch (IOException exception) { LOG.log(Level.FINE, "Unable to close lock", exception); }
        if (exit) System.exit(0);
    }
}