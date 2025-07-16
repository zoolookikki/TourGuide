package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.util.InternalTestHelper;

public class TestPerformance {

    /*
     * A note on performance improvements:
     * 
     * The number of users generated for the high volume tests can be easily
     * adjusted via this method:
     * 
     * InternalTestHelper.setInternalUserNumber(100000);
     * 
     * 
     * These tests can be modified to suit new solutions, just as long as the
     * performance metrics at the end of the tests remains consistent.
     * 
     * These are performance metrics that we are trying to hit:
     * 
     * highVolumeTrackLocation: 100,000 users within 15 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     * highVolumeGetRewards: 100,000 users within 20 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    // ce test mesure le temps nécessaire pour localiser 100 000 utilisateurs, enregistrer leur position, et calculer leurs récompenses,le tout devant être exécuté en moins de 15 minutes.
    @Test
    public void highVolumeTrackLocation() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        // Users should be incremented up to 100,000, and test finishes within 15
        // minutes
        InternalTestHelper.setInternalUserNumber(100000);
//InternalTestHelper.setInternalUserNumber(1000);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        // A ARRETER TOUT DE SUITE SINON DOUBLE TRAITEMENT.
        tourGuideService.tracker.stopTracking();

        // récupération de l'ensemble des utilisateurs.
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();

        // début chronomètre.
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        // optimisation.
        tourGuideService.trackUserLocationByUsers(allUsers);

        // fin chronomètre.
        stopWatch.stop();

        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    // ce test vérifie que le calcul de récompenses pour 100 000 utilisateurs se fait en moins de 20 minutes.
    @Test
    public void highVolumeGetRewards() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

        // Users should be incremented up to 100,000, and test finishes within 20
        // minutes
      InternalTestHelper.setInternalUserNumber(100000);
//InternalTestHelper.setInternalUserNumber(1000);

        // début chronomètre.
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        // A ARRETER TOUT DE SUITE SINON DOUBLE TRAITEMENT.
        tourGuideService.tracker.stopTracking();

        // on prend la première attraction existante dans la liste fournie par gpsUtil.
        Attraction attraction = gpsUtil.getAttractions().get(0);
        
        // récupération de l'ensemble des utilisateurs.
        List<User> allUsers = new ArrayList<>();
        allUsers = tourGuideService.getAllUsers();
        
        // pour chaque utilisateur, on simule une visite à la même attraction
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
        
        // optimisation.
        rewardsService.calculateRewardsByUsers(allUsers);

        // vérifie que chaque utilisateur a bien au moins une récompense.
        for (User user : allUsers) {
            assertTrue(user.getUserRewards().size() > 0);
        }
        // fin chronomètre.
        stopWatch.stop();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
                + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
    
}
