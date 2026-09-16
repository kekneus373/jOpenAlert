package com.github.kekneus373.jopenalert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class AutoStart {
    private static final String SHORTCUT_NAME = "jOpenAlert.lnk";

    public void setEnabled(boolean enabled) throws IOException {
        Path startup = Path.of(System.getenv().getOrDefault("APPDATA", System.getProperty("user.home")))
                .resolve("Microsoft").resolve("Windows").resolve("Start Menu").resolve("Programs")
                .resolve("Startup");
        Path target = startup.resolve(SHORTCUT_NAME);
        if (!enabled) {
            Files.deleteIfExists(target);
            return;
        }
        Files.createDirectories(startup);
        try (InputStream input = AutoStart.class.getResourceAsStream("/assets/" + SHORTCUT_NAME)) {
            if (input == null) {
                throw new IOException("Shortcut resource is missing");
            }
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}