package com.openclassrooms.tourguide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.service.RewardsService;

/**
 * Spring configuration of the TourGuide application.
 */
@Configuration
public class TourGuideModule {

    @Bean
    public GpsUtil getGpsUtil() {
        return new GpsUtil();
    }

    @Bean
    public RewardCentral getRewardCentral() {
        return new RewardCentral();
    }

    @Bean
    public RewardsService getRewardsService() {
        return new RewardsService(getGpsUtil(), getRewardCentral());
    }

}
