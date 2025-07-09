package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;

import com.openclassrooms.tourguide.dto.NearByAttractionDTO;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.util.InternalTestHelper;

import tripPricer.Provider;

public class TestTourGuideService {

    public static final int MAX_TRIP_DEALS = 5;

    /*
     * vérifie que la méthode getUserLocation, si il n'y a pas d'historique : 
     * comme il n'y a pas de localisation déjà enregistrée, qu'elle doit appeller la méthode trackUserLocation afin de localiser l'utilisateur, mettre à jour l'historique (+ calcul des récompenses) 
     * et retourner la dernière localisation connue de l'utilisateur.
     */
    @Test
    public void getUserLocationWithNoHistory() {
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        // when
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        tourGuideService.tracker.stopTracking();

        // then
        assertNotNull(visitedLocation);
        // il ne doit y avoir dans l'historique qu'une seule position gps enregistrée.
        assertEquals(1, user.getVisitedLocations().size());
        // la localisation retournée doit être la dernière de l'historique.
        assertSame(visitedLocation, user.getLastVisitedLocation());
    }

    /*
     * vérifie que la méthode getUserLocation, si il y a un historique : 
     * retourne la dernière localisation connue de l'utilisateur sans avoir appeler la méthode trackUserLocation.
     */
    @Test
    public void getUserLocationWithHistory() {
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        // ajout dans l'historique 2 localisations : la dernière est la bonne.
        tourGuideService.trackUserLocation(user);
        VisitedLocation visitedLocationOK = tourGuideService.trackUserLocation(user);

        // when
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        tourGuideService.tracker.stopTracking();

        // then
        assertNotNull(visitedLocation);
        // il ne doit pas y avoir dans l'historique de nouvelles localisations.
        assertEquals(2, user.getVisitedLocations().size());
        // la localisation retournée doit être la dernière (soit la seconde qui a été crée).
        assertSame(visitedLocation, visitedLocationOK);
    }

    /*
     * vérifie qu'un utilisateur a bien été créé et donc que l'on peut le retrouver par son nom.
     */
    @Test
    public void addUser() {
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        // when
        User retrivedUser = tourGuideService.getUser(user.getUserName());
        User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        // then
        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    /*
     * vérifie que tous les utilisateurs que l'on a créé existent dans la liste retournée par getAllUsers.
     */
    @Test
    public void getAllUsers() {
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);

        // when
        List<User> allUsers = tourGuideService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        // then
        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    /*
     * vérifie que l'appel direct à trackUserLocation fonctionne (test minimum car une partie est déjà testée avec getUserLocation) : 
     * une localisation a été générée 
     * qu'elle retourne une localisation qui correspond à l'utilisateur concernée (ce qui était écrit à l'origine).
     */
    @Test
    public void trackUser() {
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        // when
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        tourGuideService.tracker.stopTracking();

        // then
        assertNotNull(visitedLocation);
        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    /*
     * Vérifie que le service retourne les 5 attractions les plus proches d’un utilisateur donné.
     */
    @Test
    public void getNearbyAttractions() {
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        // when
        List<NearByAttractionDTO> attractions = tourGuideService.getNearByAttractions(visitedLocation, user);

        tourGuideService.tracker.stopTracking();

        // then
        assertEquals(TourGuideService.MAX_NEARBY_ATTRACTIONS, attractions.size());
    }

    /*
     * vérifie que le service getTripDeals retourne le bon nombre d'offres pour un utilisateur donné.
     */
    @Test
    public void getTripDeals() {
        // given
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        // when
        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        // then
        assertEquals(MAX_TRIP_DEALS, providers.size());
    }

}
