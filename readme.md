# TourGuide

## Description
**TourGuide** est une application qui expose ses fonctionnalités via une API REST.
Elle met à disposition de chaque voyageur les services suivants :

- Connaître leur position géographique actuelle (ou la plus récente).
- Obtenir la liste des attractions touristiques les plus proches.
- Consulter la liste des récompenses obtenues.
- Accéder à des offres de voyage.  

L’application actuelle est un prototype fonctionnel en environnement simulé, ne possédant aucune base de données, s’appuyant sur des librairies externes fictives (gpsUtil, RewardCentral, TripPricer).

## But du projet
Les objectifs principaux du projet sont les suivants :
- Corriger des anomalies : des tests unitaires instables, l’absence de recommandations d’attractions.
- Optimiser les performances afin de permettre à l’application de supporter jusqu’à 100 000 utilisateurs quotidiens.
- Mettre en place une chaine d’intégration continue (CI) pour garantir la stabilité, la fiabilité et la maintenabilité du projet sur le long terme.
- Réaliser la documentation fonctionnelle et technique manquante en y intégrant également les recherches techniques réalisées pour améliorer : l’architecture de l’application, ses performances, sa robustesse.

## Technologies

- Java 17  
- Spring Boot 3.X 
- Maven 3.9
- REST
- GitHub Actions 
- JUnit 5  

## Installation

### Prérequis
Avant d'installer l'application, assurez-vous d'avoir installé :
- Java 17
- Maven 3.9

### Comment faire pour que les dépendances des librairies gpsUtil, rewardCentral et tripPricer soient disponibles ?

> Run : 
- mvn install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar
