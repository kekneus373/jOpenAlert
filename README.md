# jOpenAlert

Small Windows tray application for the current air-raid alert state in Ukraine.

## Build

Requires JDK 17 or newer and Maven:

```text
mvn package
java -jar target/jOpenAlert-1.0.0.jar
```

Put `begin.wav`, `end.wav`, and `jOpenAlert.lnk` in `src/main/res/assets/`. They are packaged into the runnable JAR. The WAV files should be PCM audio; unsupported or missing files are logged and do not stop the application.

The first launch fetches the region names and asks for one. Configuration is stored at `%APPDATA%\jOpenAlert\config.properties`. The Auto-start menu item copies the packaged `Start-jOpenAlert.cmd` into the user's Windows Startup folder; disabling it removes that file.

The endpoint URLs are the `ALERTS_STATUS_URL` and `MAP_URL` constants in `Config.java`, so alternate compatible services can be selected without changing application logic. All network requests use the single scheduled executor.

SystemTray requires a desktop session. The application intentionally has no startup window or taskbar button.

## Install

1. Get the latest OpenJDK at [jdk.java.net](https://jdk.java.net/) (Tested with JDK 27).

2. Extract contents into `%USERPROFILE%\AppData\Local\Programs\Common\JavaJDK`

3. Create user-wide `JAVA_HOME` variable pointing to that folder (e.g. `JAVA_HOME=C:\Users\resu\AppData\Local\Programs\Common\JavaJDK\jdk-27`)

4. Add `%JAVA_HOME%\bin` to the system Path (e.g. `Path=C:\Users\resu\AppData\Local\Programs\Common\JavaJDK\jdk-27\bin`)

5. Download latest ZIP Release

6. Extract the contents into `%USERPROFILE\AppData\Local\Programs\Common\jOpenAlert` directory, then execute `Start-jOpenAlert.cmd` (as it was launched at bootup), set region, right click the tray icon, tick `Auto-start`.

7. Done!

> If you would like to choose another location for the program, change `INSTALL_PATH` variable in the batch script and move it to where you need to. As simple as that!

## License

This program is distributed under MIT License. Check the terms in `LICENSE` file.
<p></p>
<sub>
Credits to the author of the generic Java icon:
<a target="_blank" href="https://icons8.com/icon/13679/java">Java</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
</sub>