package com.openclassrooms.tourguide.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * DTO representing an attraction close to the user (for the JSON response from one of the endpoints).
 * <p>
 * @Value better than @Data for DTOs because it generates all fields final, meaning they cannot be changed after initialization.
 * To avoid problems with modifying DTOs due to passing by reference in argument and function return.
 * </p>
 */
@Value
@AllArgsConstructor
public class NearByAttractionDTO {
    
    // Name of Tourist attraction, 
    private String attractionName;
    // Tourist attractions lat/long, 
    private double attractionLatitude;
    private double attractionLongitude;
    // The user's location lat/long, 
    private double userLatitude;
    private double userLongitude;    
    // The distance in miles between the user's location and each of the attractions.
    private double distanceToAttraction;
    // The reward points for visiting each Attraction.
    private int rewardPoints;

}


