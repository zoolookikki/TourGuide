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
// thread qui toutes les 5 minutes (scheduler), pour tous les utilisateurs enregistrés, met à jour de leur position GPS  et effectue le recalcul de leurs récompenses.
public class Tracker extends Thread {
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	
	// création du thread
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final TourGuideService tourGuideService;
	private boolean stop = false;

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
			log.debug("Begin Tracker. Tracking " + users.size() + " users.");
	        log.debug("......................DEBUT SCHEDULER......................");
			stopWatch.start();
			/*
			 pour chaque utilisateur :
		     met à jour la position GPS courante de l’utilisateur, l’ajoute à son historique, déclenche le calcul de ses récompenses, et retourne la nouvelle position gps de l'utilisateur.
     		*/
			users.forEach(u -> tourGuideService.trackUserLocation(u));
			stopWatch.stop();
			log.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
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
