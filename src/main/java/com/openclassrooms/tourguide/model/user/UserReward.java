package com.openclassrooms.tourguide.model.user;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

/*
Sert à stocker les informations sur le lieu visité, l'attraction et les points de récompense attribués.
est utilisé dans la liste userRewards d’un User.
Utilise les classes suivantes de gpsUtil :

    public class VisitedLocation {
        public final UUID userId;
        public final Location location;
        public final Date timeVisited;
    }
    public class Location {
        public final double longitude;
        public final double latitude;
    }
    public class Attraction extends Location {
        public final String attractionName;
        public final String city;
        public final String state;
        public final UUID attractionId;
    }
*/
public class UserReward {

    public final VisitedLocation visitedLocation;
    public final Attraction attraction;
    private int rewardPoints;

    public UserReward(VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
        this.visitedLocation = visitedLocation;
        this.attraction = attraction;
        this.rewardPoints = rewardPoints;
    }

    public UserReward(VisitedLocation visitedLocation, Attraction attraction) {
        this.visitedLocation = visitedLocation;
        this.attraction = attraction;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

}
