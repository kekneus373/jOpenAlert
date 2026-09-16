package com.github.kekneus373.jopenalert;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public final class Config {
    public static final String ALERTS_STATUS_URL = "https://jaam.net.ua/alerts_statuses_v3.json";
    public static final String MAP_URL = "https://ubilling.net.ua/aerialalerts/?map=rednight";
    public static final String CONFIG_DIRECTORY_NAME = "jOpenAlert";
    public static final String CONFIG_FILE_NAME = "config.properties";
    public static final String REGION_KEY = "regionFriendlyName";
    public static final String AUTOSTART_KEY = "autoStart";
    public static final String SUSPENDED_KEY = "suspended";
    public static final String LAST_STATE_KEY = "lastState";
    public static final String APP_TITLE = "Ukraine Air Alerts";
    public static final String REGION_LABEL = "Region";
    public static final String AUTOSTART_LABEL = "Auto-start";
    public static final String SUSPEND_LABEL = "Suspend";
    public static final String SHOW_MAP_LABEL = "Show map";
    public static final String EXIT_LABEL = "Exit";
    public static final String REFRESH_LABEL = "Refresh";
    public static final String UPDATED_LABEL = "Last updated: %s";
    public static final String PICK_REGION_MESSAGE = "Choose the region to monitor:";
    public static final String PICK_REGION_TITLE = "Choose region";
    public static final String NO_REGIONS_MESSAGE = "No regions were returned by the alerts service.";
    public static final String TRAY_UNAVAILABLE_MESSAGE = "System tray is not available in this desktop session.";

    private final Path file;
    private String region;
    private boolean autoStart;
    private boolean suspended;
    private boolean lastState;

    private Config(Path file) {
        this.file = file;
    }

    public static Path configFile() {
        String appData = System.getenv("APPDATA");
        Path root = appData == null || appData.isBlank()
                ? Path.of(System.getProperty("user.home"))
                : Path.of(appData);
        return root.resolve(CONFIG_DIRECTORY_NAME).resolve(CONFIG_FILE_NAME);
    }

    public static Config load(Path file) throws IOException {
        var config = new Config(file);
        var properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        }
        config.region = properties.getProperty(REGION_KEY, "");
        config.autoStart = Boolean.parseBoolean(properties.getProperty(AUTOSTART_KEY, "false"));
        config.suspended = Boolean.parseBoolean(properties.getProperty(SUSPENDED_KEY, "false"));
        config.lastState = Boolean.parseBoolean(properties.getProperty(LAST_STATE_KEY, "false"));
        return config;
    }

    public static Config newConfig(Path file, String region) {
        var config = new Config(file);
        config.region = region;
        return config;
    }

    public synchronized void save() throws IOException {
        Files.createDirectories(file.getParent());
        var properties = new Properties();
        properties.setProperty(REGION_KEY, region);
        properties.setProperty(AUTOSTART_KEY, Boolean.toString(autoStart));
        properties.setProperty(SUSPENDED_KEY, Boolean.toString(suspended));
        properties.setProperty(LAST_STATE_KEY, Boolean.toString(lastState));
        try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            properties.store(output, APP_TITLE);
        }
    }

    public synchronized String region() { return region; }
    public synchronized void region(String value) { region = value; }
    public synchronized boolean autoStart() { return autoStart; }
    public synchronized void autoStart(boolean value) { autoStart = value; }
    public synchronized boolean suspended() { return suspended; }
    public synchronized void suspended(boolean value) { suspended = value; }
    public synchronized boolean lastState() { return lastState; }
    public synchronized void lastState(boolean value) { lastState = value; }
}