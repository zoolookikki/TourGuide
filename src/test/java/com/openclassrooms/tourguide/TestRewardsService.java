package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.log4j.Log4j2;
import rewardCentral.RewardCentral;

import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.util.InternalTestHelper;

@Log4j2
// mémo : RewardsService ==> service permettant le calcul des récompenses en fonction des lieux visités et l'évaluation de la distance entre un utilisateur et une attraction.
public class TestRewardsService {

    @Test
    // vérifie qu'un utilisateur reçoit une récompense lorsqu'il a visité un lieu.
    public void userGetRewards() {
        log.debug("......................DEBUT TEST OK userGetRewards......................");
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtil.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

        // when
        tourGuideService.trackUserLocation(user);
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();

        // then
        assertTrue(userRewards.size() == 1);
        log.debug("......................FIN TEST OK userGetRewards......................");
    }

    @Test
    // vérifie que c'est vrai si on lui passe la même attraction deux fois (distance 0).
    public void isWithinAttractionProximity() {
        log.debug("......................DEBUT TEST OK isWithinAttractionProximity......................");
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        Attraction attraction = gpsUtil.getAttractions().get(0);

        // when then
        // vérifie si l'attraction est proche de la localisation actuelle
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
        log.debug("......................DEBUT TEST FIN isWithinAttractionProximity......................");
    }

//	@Disabled // Needs fixed - can throw ConcurrentModificationException
    @Test
    // Avec une proximité règlée au maximum, vérifie que toutes les attractions génèrent une récompense.
    public void nearAllAttractions() {
        log.debug("......................DEBUT TEST HS nearAllAttractions......................");

        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        // distance de proximité au maximum ==> toutes les attractions possibles vont correspondres.
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        // un utilisateur simulé uniquement.
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        // when
        // calcule les récompenses pour l'utilisateur simulé.
        rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
        // récupère la liste des récompenses.
        List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
        tourGuideService.tracker.stopTracking();

        // then
        // il y a autant d'attractions que de récompenses (cqfd).
//        userRewards.forEach(r -> System.out.println(r.attraction.attractionName));
        assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
        log.debug("......................FIN TEST HS nearAllAttractions......................");
    }
    
}
