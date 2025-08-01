package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.dto.NearByAttractionDTO;
import com.openclassrooms.tourguide.model.user.User;
import com.openclassrooms.tourguide.model.user.UserReward;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.util.InternalTestHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.log4j.Log4j2;
import tripPricer.Provider;
import tripPricer.TripPricer;

import org.apache.commons.lang3.time.StopWatch;

/**
 * The main service of the TourGuide application, responsible for managing users, their geolocation, calculating rewards and recommending attractions or travel offers.
 *
 * <p>It centralizes access to functionality by orchestrating calls to simulated external libraries: GpsUtil, TripPricer, and RewardCentral via the RewardsService service.</p>
 *
 * <p>It also initializes the Tracker, which periodically updates the position and rewards of all registered users.</p>
 *
 */
@Log4j2
@Service
public class TourGuideService {

    // nombre maximum d'attractions proches à retourner.
    public static final int MAX_NEARBY_ATTRACTIONS = 5;

    // pour trouver la géolocalisation d'un utilisateur + la liste des attractions touristiques associées.
    private final GpsUtil gpsUtil;

    // service qui calcule les récompenses.
    private final RewardsService rewardsService;

    // pour générer des offres de voyage.
    private final TripPricer tripPricer = new TripPricer();

    // thread qui toutes les 5 minutes (scheduler), pour tous les utilisateurs enregistrés, met à jour de leur position GPS et effectue le recalcul de leurs récompenses.
    public final Tracker tracker;

    // pour créer des utilisateurs pour les tests.
    boolean testMode = true;

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        // pour tester l'endpoint getRewards sinon vide.
        //rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        
        // Locale.US comme locale par défaut afin d’uniformiser les conversions nombre/chaîne (coordonnées GPS, distances)
        Locale.setDefault(Locale.US);

        // crée des utilisateurs “internalUserX”
        if (testMode) {
            log.info("TestMode enabled");
            log.debug("Initializing users");
            initializeInternalUsers();
            log.debug("Finished initializing users");
        }

        // initialise et lance le scheduler
        tracker = new Tracker(this);

        // ajoute un hook pour permettre au scheduler de s'arrêter correctement.
        addShutDownHook();
    }

    /**
     * Returns the list of rewards earned by a user.
     *
     * @param user the user concerned.
     * @return list of UserRewards associated with the user.
     */
    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    /**
     * If the user already has positions, we return the last one, otherwise we request one.
     *
     * @param user the user whose position we want.
     * @return the most recently visited position.
     */
    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
                : trackUserLocation(user);
        return visitedLocation;
    }

    /**
     * Search for a user by name.
     *
     * @param userName 
     * @return the corresponding User object or null if not found.
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * Returns the list of all users.
     *
     * @return a list of User.
     */    
    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Adds a user if it does not exist.
     *
     * @param user the user to add.
     */
    
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }
    
    /**
     * Returns the offers available for a user.
     *
     * @param user the user for whom we are looking offers.
     * @return list of suppliers.
     */
    public List<Provider> getTripDeals(User user) {
        // calcule le total de points de récompense.
        int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
        // récupère les offres.
        log.debug("getTripDeals/tripPricer.getPrice,user.getUserId()=" + user.getUserId()
                + ",user.getUserPreferences().getNumberOfAdults()=" + user.getUserPreferences().getNumberOfAdults()
                + ",user.getUserPreferences().getNumberOfChildren()=" + user.getUserPreferences().getNumberOfChildren()
                + ",user.getUserPreferences().getTripDuration()=" + user.getUserPreferences().getTripDuration()
                + ",cumulatativeRewardPoints=" + cumulatativeRewardPoints);
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
        log.debug("getTripDeals/providers=" + providers);
        // stocke ces offres dans l’objet User.
        user.setTripDeals(providers);
        return providers;
    }

    /**
     * Updates the user's current GPS position, saves this position in the history, triggers the calculation of its rewards, and returns the user's new GPS position.
     *
     * @param user the user concerned.
     * @return the new position visited.
     */
    public VisitedLocation trackUserLocation(User user) {
        StopWatch stopWatch = new StopWatch();
        
        log.debug("......................DEBUT trackUserLocation ......................" + user.getUserName());

        stopWatch.start();
        // appelle gpsUtil pour obtenir la position courante
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        stopWatch.stop();
        log.debug("getUserLocation : " + stopWatch.getTime() + " ms");
        
        stopWatch.reset();
        stopWatch.start();
        // ajoute cette position dans l’historique de l’utilisateur.
        user.addToVisitedLocations(visitedLocation);
        stopWatch.stop();
        log.debug("addToVisitedLocations : " + stopWatch.getTime() + " ms");
        
        stopWatch.reset();
        stopWatch.start();
        // calcule les récompenses.
        rewardsService.calculateRewards(user);
        stopWatch.stop();
        log.debug("calculateRewards : " + stopWatch.getTime() + " ms");
        
        log.debug("......................FIN trackUserLocation ......................" + user.getUserName());
        return visitedLocation;
    }
    
    
    /**
     * Same as trackUserLocation method, for all users passed as parameters, using optimized parallel processing.
     *
     * @param users the list of users to be processed.
     */
    public void trackUserLocationByUsers(List<User> users) {
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (User user : users) {
            futures.add(CompletableFuture.runAsync(() -> {
                trackUserLocation(user);
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();  
        executor.shutdown();
    }
    
    /**
     * Returns the list of attractions closest to the user, sorted by distance, with associated distances and reward points.
     *
     * @param visitedLocation the user's current position.
     * @param user            the user concerned.
     * @return NearByAttractionDTO list limited to MAX_NEARBY_ATTRACTIONS.
     */    
    // optimisation CompletableFuture car appel à getRewarPoints pénalisant (vu lors du test getNearbyAttractions qui était trop lent)
    public List<NearByAttractionDTO> getNearByAttractions(VisitedLocation visitedLocation, User user) {
        // Liste de tâches asynchrones
        List<CompletableFuture<NearByAttractionDTO>> futures = new ArrayList<>();
        // pas besoin de plus de threads pour ce traitement.
        ExecutorService executor = Executors.newFixedThreadPool(100);
        
        // construction de la liste de NearByAttractionDTO (1 ère partie).
        for (Attraction attraction : gpsUtil.getAttractions()) {
            // Lancement d’une tâche asynchrone pour chaque attraction ==> c'est getRewardPoints qui prend du temps. 
            CompletableFuture<NearByAttractionDTO> future = CompletableFuture.supplyAsync(() -> {
                return new NearByAttractionDTO(
                        attraction.attractionName,
                        attraction.latitude,
                        attraction.longitude,
                        visitedLocation.location.latitude,
                        visitedLocation.location.longitude,
                        // idem RewardsService.nearAttraction
                        rewardsService.getDistance(attraction, visitedLocation.location),
                        rewardsService.getRewardPoints(attraction, user)
                );
            }, executor);
            futures.add(future);
        }

        // Attente des résultats ==> construction de la liste de NearByAttractionDTO (2 ème partie).
        List<NearByAttractionDTO> dtos = new ArrayList<>();
        for (CompletableFuture<NearByAttractionDTO> future : futures) {
            dtos.add(future.join());
        }
        
        executor.shutdown();
        
        /*
         * Trier la liste des dtos, de la plus petite distance à la plus grande, en comparant les distances vers l’attraction. 
         * sources :
         * https://medium.com/@AlexanderObregon/javas-comparator-comparing-method-explained-342361288af6
         * https://docs.oracle.com/javase/10/docs/api/java/util/Comparator.html#comparingDouble(java.util.function.ToDoubleFunction)
         */
        dtos.sort(Comparator.comparingDouble(NearByAttractionDTO::getDistanceToAttraction));

        /*
         * Filtrage : on ne garde que les MAX_NEARBY_ATTRACTIONS premières attractions.
         * source :
         * https://codegym.cc/fr/groups/posts/fr.416.methode-sublist-en-java-arraylist-et-list
         */
        // Attention car la taille de la liste peut être inférieure à MAX_NEARBY_ATTRACTIONS => IndexOutOfBoundExecption.
        int maxIndex = Math.min(MAX_NEARBY_ATTRACTIONS, dtos.size());
        return dtos.subList(0, maxIndex);
    }
    
    /**
     * Registers a shutdown hook with the JVM to ensure proper shutdown of the scheduler when the application closes.
     */
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     * 
     * Methods Below: For Internal Testing
     * 
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    // création d'utilisateurs pour les tests.
    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            // génère des positions pour un utilisateur.
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        log.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    // ajoute 3 positions aléatoires par utilisateur (latitude/longitude + date).
    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                    new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
