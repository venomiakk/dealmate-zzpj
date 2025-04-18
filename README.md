# dealmate-zzpj

## Dealmate - Card Game Platform

The aim of the project is to create a platform for playing various card games online, allowing users to set up tables,
join multiplayer games and compete in real time.

## Prerequisites

- (Oracle) [jdk-24](https://www.oracle.com/pl/java/technologies/downloads/) >=
- Maven 3.9.5 >=
- PostgreSQL 17 >=

## Selected topics

- [x] Centralized configuration via Spring Cloud Config Server
- [x] Service discovery via Spring Cloud Eureka
- [ ] Authorization management via Keycloak/Spring Authorization Server
- [ ] Implementation of static code analysis (SonarQube)
- [ ] Syntactic news from the latest JDK
    - [ ] Virtual Threads
    - [ ] Gatherers
- [ ] Using Cucumber/AssertJ in Testing
- [ ] Concurrency (multiple games at the same time)
- [ ] Use of AI

## Key functionalities

- Registration and login
- Creation of game rooms (public and private)
- Possibility to play different types of card games
- Chat during the game
- Integration with AI to analyze the game (suggest moves)

## Servers & Services

- *Eureka Discovery Server* (**discoveryserver**) - port 8761
- *Cloud Config Server* (**configserver**) - port 8888
- Deck Service (**deckservice**) - port 8101
- Game Service (**gameservice**) - port ?
- User Service (**userservice**) - port ?
- Chat Service (**chatservice**) - port ?
- AIService (**aiservice**) - port ?

## Startup order

1. Start the (**discoveryserver**) service
1. Start the (**configserver**) service
1. Start the rest of the services

## Contributors

- [Adrian Michałek](https://github.com/venomiakk)
- [Natalia Nykiel](https://github.com/natalianykiel)
- [Maksymilian Paluśkiewicz](https://github.com/FdotP)
- [Patryk Sałyga](https://github.com/patryksalyga)