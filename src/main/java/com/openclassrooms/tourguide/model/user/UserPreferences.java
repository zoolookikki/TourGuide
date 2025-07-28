package com.openclassrooms.tourguide.model.user;

/**
 * Stores a user's travel preferences, used to find suitable offers.
 */
public class UserPreferences {

    /*
     * distance maximale des attractions (en miles) considérée comme acceptable.
     * initialisée à la valeur maximale d'un entier => pas de limite pas défaut.
     */
    private int attractionProximity = Integer.MAX_VALUE;
    // durée souhaitée du voyage en ???
    private int tripDuration = 1;
    // nombre total de billets à acheter.
    private int ticketQuantity = 1;
    // nombre d’adultes participant au voyage.
    private int numberOfAdults = 1;
    // nombre d’enfants participant au voyage.
    private int numberOfChildren = 0;

    public UserPreferences() {
    }

    public void setAttractionProximity(int attractionProximity) {
        this.attractionProximity = attractionProximity;
    }

    public int getAttractionProximity() {
        return attractionProximity;
    }

    public int getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(int tripDuration) {
        this.tripDuration = tripDuration;
    }

    public int getTicketQuantity() {
        return ticketQuantity;
    }

    public void setTicketQuantity(int ticketQuantity) {
        this.ticketQuantity = ticketQuantity;
    }

    public int getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(int numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(int numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

}
