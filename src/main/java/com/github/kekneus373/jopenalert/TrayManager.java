package com.github.kekneus373.jopenalert;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TrayManager {
    private static final Logger LOG = Logger.getLogger(TrayManager.class.getName());
    private final Config config;
    private final AutoStart autoStart;
    private final Poller poller;
    private final MapWindow mapWindow;
    private final Consumer<Boolean> shutdown;
    private final SystemTray tray;
    private final TrayIcon icon;
    private CheckboxMenuItem autoStartItem;
    private CheckboxMenuItem suspendItem;

    public TrayManager(Config config, AutoStart autoStart, Poller poller, MapWindow mapWindow, Consumer<Boolean> shutdown) {
        this.config = config;
        this.autoStart = autoStart;
        this.poller = poller;
        this.mapWindow = mapWindow;
        this.shutdown = shutdown;
        try {
            tray = SystemTray.getSystemTray();
            icon = new TrayIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), Config.APP_TITLE);
            icon.setImageAutoSize(true);
        } catch (Exception exception) {
            throw new IllegalStateException(Config.TRAY_UNAVAILABLE_MESSAGE, exception);
        }
    }

    public void install() throws java.awt.AWTException {
        var menu = new PopupMenu();
        var regions = new Menu(Config.REGION_LABEL);
        for (String name : Main.regionNames()) {
            var item = new MenuItem(name);
            item.addActionListener(event -> {
                config.region(name);
                saveConfig();
                poller.regionChanged();
            });
            regions.add(item);
        }
        menu.add(regions);
        autoStartItem = new CheckboxMenuItem(Config.AUTOSTART_LABEL, config.autoStart());
        autoStartItem.addItemListener(event -> {
            boolean enabled = autoStartItem.getState();
            try {
                autoStart.setEnabled(enabled);
                config.autoStart(enabled);
                saveConfig();
            } catch (IOException exception) {
                LOG.log(Level.WARNING, "Unable to update auto-start", exception);
                autoStartItem.setState(!enabled);
            }
        });
        menu.add(autoStartItem);
        suspendItem = new CheckboxMenuItem(Config.SUSPEND_LABEL, config.suspended());
        suspendItem.addItemListener(event -> {
            config.suspended(suspendItem.getState());
            saveConfig();
        });
        menu.add(suspendItem);
        var showMap = new MenuItem(Config.SHOW_MAP_LABEL);
        showMap.addActionListener(event -> mapWindow.showWindow());
        menu.add(showMap);
        var exit = new MenuItem(Config.EXIT_LABEL);
        exit.addActionListener(event -> shutdown.accept(true));
        menu.add(exit);
        icon.setPopupMenu(menu);
        tray.add(icon);
    }

    public void remove() { tray.remove(icon); }

    private void saveConfig() {
        try { config.save(); }
        catch (IOException exception) { LOG.log(Level.WARNING, "Unable to save configuration", exception); }
    }
}