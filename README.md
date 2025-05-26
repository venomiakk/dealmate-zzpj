# dealmate-zzpj

## Notes
- Consider creating external common-dto library for all services
- Consider creating configuration profiles for different environments (dev, test, prod)

## Dealmate - Card Game Platform

The aim of the project is to create a platform for playing various card games online, allowing users to set up tables,
join multiplayer games and compete in real time.

## Prerequisites

- (Oracle) [jdk-24](https://www.oracle.com/pl/java/technologies/downloads/) >=
- Maven 3.9.5 >=
- PostgreSQL 17 >=

## Servers & Services

- *Eureka Discovery Server* (**discoveryserver**) - port 8761
- *Cloud Config Server* (**configserver**) - port 8888
- *Authorization Server* (**authserver**) - port 9000
- *API Gateway* (**apigateway**) - port 8100
- Deck Service (**deckservice**) - port 8101
- User Service (**userservice**) - port 8102
- AI Service (**aiservice**) - port 8103
- Game Service (**gameservice**) - port 8104
- Chat Service (**chatservice**) - port 8105

## Startup order

1. Start the (**discoveryserver**) server
1. Start the (**configserver**) server
1. Start the (**authserver**) server
1. Start the (**apigateway**) server
1. Start the rest of the services

## Selected topics

- [x] Centralized configuration via Spring Cloud Config Server
- [x] Service discovery via Spring Cloud Eureka
- [x] Authorization management via Spring Authorization Server
- [x] Implementation of static code analysis (SonarQube)
- [ ] Syntactic news from the latest JDK
    - [ ] Virtual Threads
    - [ ] Gatherers
- [x] Using AssertJ in Testing
- [ ] Concurrency (multiple games at the same time)
- [ ] Use of AI

## Key functionalities

- Registration and login
- Creation of game rooms (public and private)
- Possibility to play different types of card games
- Chat during the game
- Integration with AI to analyze the game (suggest moves)


## Contributors

- [Adrian Michałek](https://github.com/venomiakk)
- [Natalia Nykiel](https://github.com/natalianykiel)
- [Maksymilian Paluśkiewicz](https://github.com/FdotP)
- [Patryk Sałyga](https://github.com/patryksalyga)
