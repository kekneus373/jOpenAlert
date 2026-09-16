package com.github.kekneus373.jopenalert;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Poller {
    private static final Logger LOG = Logger.getLogger(Poller.class.getName());
    private static final long NORMAL_DELAY_SECONDS = 20;
    private static final long BACKOFF_DELAY_SECONDS = 60;
    private final AlertApi api;
    private final Config config;
    private final SoundPlayer sounds;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> future;
    private boolean firstSuccessfulPoll = true;
    private int failures;

    public Poller(AlertApi api, Config config, SoundPlayer sounds, ScheduledExecutorService scheduler) {
        this.api = api;
        this.config = config;
        this.sounds = sounds;
        this.scheduler = scheduler;
    }

    public synchronized void start() {
        schedule(0);
    }

    public synchronized void stop() {
        if (future != null) {
            future.cancel(false);
        }
    }

    public synchronized void regionChanged() {
        failures = 0;
    }

    private synchronized void schedule(long delaySeconds) {
        future = scheduler.schedule(this::pollSafely, delaySeconds, TimeUnit.SECONDS);
    }

    private void pollSafely() {
        long nextDelay = NORMAL_DELAY_SECONDS;
        try {
            if (!config.suspended()) {
                Map<String, Boolean> states = api.fetchStates();
                Boolean current = states.get(config.region());
                if (current == null) {
                    throw new IOException("Region was not present in the alerts response: " + config.region());
                }
                boolean previous = config.lastState();
                config.lastState(current);
                config.save();
                if (!firstSuccessfulPoll && previous != current) {
                    sounds.play(current ? SoundPlayer.BEGIN : SoundPlayer.END);
                }
                firstSuccessfulPoll = false;
                failures = 0;
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Polling interrupted", exception);
        } catch (Exception exception) {
            failures++;
            LOG.log(Level.WARNING, "Unable to update alert status; retrying", exception);
            if (failures >= 3) {
                nextDelay = BACKOFF_DELAY_SECONDS;
            }
        }
        synchronized (this) {
            if (!scheduler.isShutdown()) {
                schedule(nextDelay);
            }
        }
    }
}