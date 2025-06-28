package com.openclassrooms.tourguide.model.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

public class User {
    // identifiants de l’utilisateur. Immuables (final), définis à la création.
	private final UUID userId;
	private final String userName;
	// coordonnées de l’utilisateur (modifiables).
	private String phoneNumber;
	private String emailAddress;
	// date de la dernière mise à jour de la position de l'utilisateur.
	private Date latestLocationTimestamp;
	// liste de toutes les localisations visitées par l’utilisateur (historique GPS).
	private List<VisitedLocation> visitedLocations = new ArrayList<>();
	// liste des récompenses obtenues pour avoir visité des attractions.
	private List<UserReward> userRewards = new ArrayList<>();
	// préférences de voyage (nb de personnes, durée du séjour, etc.).
	private UserPreferences userPreferences = new UserPreferences();
	// offres de voyages proposées à l’utilisateur.
	private List<Provider> tripDeals = new ArrayList<>();
	
	public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
		this.userId = userId;
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}
	
	public UUID getUserId() {
		return userId;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	
	public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
		this.latestLocationTimestamp = latestLocationTimestamp;
	}
	
	public Date getLatestLocationTimestamp() {
		return latestLocationTimestamp;
	}
	
	// ajoute une localisation à l’historique de l’utilisateur.
	public void addToVisitedLocations(VisitedLocation visitedLocation) {
		visitedLocations.add(visitedLocation);
	}
	
	public List<VisitedLocation> getVisitedLocations() {
		return visitedLocations;
	}

	// vide l’historique.
	public void clearVisitedLocations() {
		visitedLocations.clear();
	}
	
	// ajoute une récompense seulement si elle n’existe pas déjà pour la même attraction.
	public void addUserReward(UserReward userReward) {
		if(userRewards.stream().filter(r -> !r.attraction.attractionName.equals(userReward.attraction)).count() == 0) {
			userRewards.add(userReward);
		}
	}
	
	public List<UserReward> getUserRewards() {
		return userRewards;
	}
	
	public UserPreferences getUserPreferences() {
		return userPreferences;
	}
	
	public void setUserPreferences(UserPreferences userPreferences) {
		this.userPreferences = userPreferences;
	}

	public VisitedLocation getLastVisitedLocation() {
		return visitedLocations.get(visitedLocations.size() - 1);
	}
	
	public void setTripDeals(List<Provider> tripDeals) {
		this.tripDeals = tripDeals;
	}
	
	public List<Provider> getTripDeals() {
		return tripDeals;
	}

}
