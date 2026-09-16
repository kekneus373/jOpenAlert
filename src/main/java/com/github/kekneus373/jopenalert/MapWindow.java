package com.github.kekneus373.jopenalert;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class MapWindow {
    private static final Logger LOG = Logger.getLogger(MapWindow.class.getName());
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AlertApi api;
    private final ScheduledExecutorService scheduler;
    private JFrame frame;
    private MapPanel imagePanel;
    private JLabel updated;
    private ScheduledFuture<?> refreshTask;

    public MapWindow(AlertApi api, ScheduledExecutorService scheduler) {
        this.api = api;
        this.scheduler = scheduler;
    }

    public void showWindow() {
        if (frame == null || !frame.isDisplayable()) {
            createFrame();
        }
        frame.setVisible(true);
        frame.toFront();
        if (refreshTask == null || refreshTask.isCancelled()) {
            refreshTask = scheduler.scheduleAtFixedRate(this::refresh, 0, 20, TimeUnit.SECONDS);
        }
    }

    private void createFrame() {
        frame = new JFrame(Config.APP_TITLE);
        frame.setAlwaysOnTop(true);
        frame.setSize(new Dimension(900, 650));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(8, 8));
        imagePanel = new MapPanel();
        updated = new JLabel(String.format(Config.UPDATED_LABEL, "never"), SwingConstants.CENTER);
        JButton refresh = new JButton(Config.REFRESH_LABEL);
        refresh.addActionListener(event -> scheduler.execute(this::refresh));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(updated, BorderLayout.CENTER);
        bottom.add(refresh, BorderLayout.EAST);
        frame.add(imagePanel, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent event) {
                if (refreshTask != null) refreshTask.cancel(false);
                refreshTask = null;
            }
        });
    }

    private void refresh() {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(api.fetchMapPng()));
            if (image == null) throw new IllegalArgumentException("Map response was not an image");
            SwingUtilities.invokeLater(() -> {
                if (frame != null && frame.isDisplayable()) {
                    imagePanel.image = image;
                    imagePanel.revalidate();
                    imagePanel.repaint();
                    updated.setText(String.format(Config.UPDATED_LABEL, TIME_FORMAT.format(LocalDateTime.now())));
                }
            });
        } catch (Exception exception) {
            LOG.log(Level.WARNING, "Unable to refresh alert map", exception);
        }
    }

    private static final class MapPanel extends JPanel {
        private BufferedImage image;

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image == null) return;
            double scale = Math.min((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
            int width = Math.max(1, (int) (image.getWidth() * scale));
            int height = Math.max(1, (int) (image.getHeight() * scale));
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            graphics.drawImage(scaled, x, y, null);
        }
    }
}