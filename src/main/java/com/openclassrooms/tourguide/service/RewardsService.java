package com.openclassrooms.tourguide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.log4j.Log4j2;
import rewardCentral.RewardCentral;

import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;

@Log4j2
// service permettant le calcul des récompenses en fonction des lieux visités et l'évaluation de la distance entre un utilisateur et une attraction.
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // petite distance par défaut en miles pour dire qu’une position est proche d’une attraction.
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;

    // + grande distance acceptable que la précédente par défaut en miles.
    private int attractionProximityRange = 200;

    // pour trouver la géolocalisation d'un utilisateur + la liste des attractions touristiques associées.
    private final GpsUtil gpsUtil;

    // pour calculer les récompenses.
    private final RewardCentral rewardsCentral;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }
    
    // calcule et attribue des récompenses à un utilisateur en fonction des lieux visités et de la proximité des attractions.
    public void calculateRewards(User user) {
        // récupère l'historique des lieux visités par l'utilisateur.
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        // récupère la liste des attractions.
        List<Attraction> attractions = gpsUtil.getAttractions();

        log.info("User: " + user.getUserName() + ", locations: " + userLocations.size() + ", attractions: " + attractions.size());
        
        // pour chaque lieu que l'utilisateur a visité.
        for (VisitedLocation visitedLocation : userLocations) {
            // pour chaque attractions que l'on connait.
            for (Attraction attraction : attractions) {
                // On vérifie que l'utilisateur n'a pas encore reçu de récompense pour cette attraction.
                if (user.getUserRewards().stream()
                        .filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
                    // On vérifie si la position visitée est proche de l’attraction
                    if (nearAttraction(visitedLocation, attraction)) {
                        // On calcule le nombre de points d'une récompense et on l’ajoute à la liste des récompenses de l’utilisateur.
                        user.addUserReward(
                                new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
    }
    
    public void calculateRewardsByUsers(List<User> users) {

        /*
         *  Interface qui gère un pool de threads.
         *  Jusqu'à ... tâches peuvent s'exécuter en parallèle.
         *  Les threads sont réutilisés : on ne recrée pas un thread pour chaque tâche.
         *  
         */

//        sources : https://www.infoq.com/fr/articles/Java-Thread-Pool-Performance-Tuning/
        
//        ExecutorService executor = Executors.newFixedThreadPool(32); // 162 s pour 10.000 =>  toujous hs pour 100.000 => 27mn
//        ExecutorService executor = Executors.newFixedThreadPool(100); // 51 s pour 10.000 ==> OK
        ExecutorService manyUersexecutor = Executors.newFixedThreadPool(1000); // 10 s pour 10.000 ==> 100.000 = 100 s => - de 2 mn !!!
//        ExecutorService executor = Executors.newFixedThreadPool(10000); // + ne change rien

        // liste pour suivre les tâches.
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (User user : users) {
            // création d'une tâche asynchrone pour un utilisateur
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                calculateRewards(user);
            }, manyUersexecutor);
            // ajoute le CompletableFuture à la liste pour pouvoir ensuite synchroniser tout à la fin.
            futures.add(future);
        }

        // crée un nouveau CompletableFuture combiné qui représente l'ensemble des tâches.
        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        // bloque jusqu’à ce que toutes les tâches soient terminées.
        combinedFuture.join();
        manyUersexecutor.shutdown();
     }

    // vérifie si le lieu visité et l'attraction est à proximité (rayon en miles petit).
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    // vérifie si l'attraction est proche de la localisation actuelle (rayon en miles + important que la précédente).
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    // calcule le nombre de points d'une récompense d'une attraction associée à un utilisateur.
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    // calcule la distance en miles entre deux positions.
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math
                .acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
