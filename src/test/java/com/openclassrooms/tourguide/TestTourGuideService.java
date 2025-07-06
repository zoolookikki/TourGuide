package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    /*
     * vérifie que la méthode trackUserLocation (qui localise l’utilisateur, met à jour son historique et calcule ses récompenses) retourne bien la position correspondant à l'utilisateur concerné. 
     */
	@Test
	public void getUserLocation() {
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
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
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
	 *  idem getUserLocation ???
	 */
    /*
    public void getUserLocation() {
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
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }
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

	// ce test n'est pas appellé et ne fonctionne pas pour le moment.
	/*
	 * vérifie que le service getTripDeals retourne bien 10 offres pour un utilisateur donné.
	 */
//    @Test
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
		assertEquals(10, providers.size());
	}

}
