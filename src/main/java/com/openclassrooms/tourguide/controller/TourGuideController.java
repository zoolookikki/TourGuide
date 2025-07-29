package com.openclassrooms.tourguide.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.dto.NearByAttractionDTO;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;
import com.openclassrooms.tourguide.service.TourGuideService;

import tripPricer.Provider;

/**
 * Main REST controller of the TourGuide application.
 */
@RestController
public class TourGuideController {

    @Autowired
    private TourGuideService tourGuideService;

    /**
     * Home endpoint
     *
     * @return a welcome message.
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    /**
     * Finds the user's last known location.
     *
     * @param userName the user's name.
     * @return the location.
     */
    @RequestMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
        return tourGuideService.getUserLocation(getUser(userName));
    }
    
    /**
     * Retrieves a user's location and searches for the nearest attractions.
     * 
     * @param userName the user's name.
     * @return a list of NearByAttractionDTO objects representing nearby attractions. 
     */
    @RequestMapping("/getNearbyAttractions")
    public List<NearByAttractionDTO> getNearbyAttractions(@RequestParam String userName) {
        User user = getUser(userName);
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(user);
        return tourGuideService.getNearByAttractions(visitedLocation, user);
    }

    /**
     * Search for a user's rewards.
     * 
     * @param userName the user's name.
     * @return a list of rewards.
     */
    @RequestMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return tourGuideService.getUserRewards(getUser(userName));
    }

    /**
     * Provides travel offers available to the user
     * 
     * @param userName the user's name.
     * @return a list of offers
     */
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        return tourGuideService.getTripDeals(getUser(userName));
    }

    /**
     * Internal utility method to retrieve a User object from name.
     * 
     * @param userName the user's name.
     * @return User object.
     */
    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

}