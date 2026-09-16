# jOpenAlert

Small Windows tray application for the current air-raid alert state in Ukraine.

## Build

Requires JDK 17 or newer and Maven:

```text
mvn package
java -jar target/jOpenAlert-1.0.0.jar
```

Put `begin.wav`, `end.wav`, and `jOpenAlert.lnk` in `src/main/res/assets/`. They are packaged into the runnable JAR. The WAV files should be PCM audio; unsupported or missing files are logged and do not stop the application.

The first launch fetches the region names and asks for one. Configuration is stored at `%APPDATA%\jOpenAlert\config.properties`. The Auto-start menu item copies the packaged `jOpenAlert.lnk` into the user's Windows Startup folder; disabling it removes that shortcut.

The endpoint URLs are the `ALERTS_STATUS_URL` and `MAP_URL` constants in `Config.java`, so alternate compatible services can be selected without changing application logic. All network requests use the single scheduled executor.

SystemTray requires a desktop session. The application intentionally has no startup window or taskbar button.