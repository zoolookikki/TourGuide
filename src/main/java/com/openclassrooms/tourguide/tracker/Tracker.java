package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.TourGuideService;

import lombok.extern.log4j.Log4j2;

@Log4j2
/**
 * Thread that every 5 minutes (scheduler), for all registered users, updates their GPS position and recalculates their rewards.
 */
public class Tracker extends Thread {
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);

    // création du thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final TourGuideService tourGuideService;
    private boolean stop = false;

    /**
     * Constructs a Tracker bound to a TourGuideService instance.
     * The thread is immediately started upon creation.
     *
     * @param tourGuideService the service used for tracking operations.
     */
    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;

        // lance le thread.
        executorService.submit(this);
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        stop = true;
        // interrompt le thread si bloqué en sleep.
        executorService.shutdownNow();
    }

    /**
     * Loops through user tracking operations: retrieving the user list, updating their GPS location, and recalculating associated rewards.
     * <p>
     * The thread stops when an interrupt is detected or stop is triggered.
     * </p>
     */
    @Override
    public void run() {
        // chronomètre.
        StopWatch stopWatch = new StopWatch();
        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                log.debug("Tracker stopping");
                break;
            }

            List<User> users = tourGuideService.getAllUsers();
            log.info("Begin Tracker. Tracking " + users.size() + " users.");
            log.debug("......................DEBUT SCHEDULER......................");
            stopWatch.start();
            /*
             * pour chaque utilisateur : met à jour la position GPS courante de l’utilisateur, l’ajoute à son historique, déclenche le calcul de ses récompenses, 
             * 
             */
            // appel de la méthode optimisée.
            tourGuideService.trackUserLocationByUsers(users);
            
            stopWatch.stop();
            log.info("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            stopWatch.reset();
            log.debug("......................FIN SCHEDULER......................");
            try {
                log.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {
                break;
            }
        }

    }
}
