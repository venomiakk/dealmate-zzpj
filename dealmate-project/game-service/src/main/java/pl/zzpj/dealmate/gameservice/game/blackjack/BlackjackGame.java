package pl.zzpj.dealmate.gameservice.game.blackjack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.game.dto.*;
import pl.zzpj.dealmate.gameservice.model.GameRoom;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class BlackjackGame {
    private final GameRoom room;
    private final DeckServiceClient deckService;
    private final SimpMessagingTemplate messagingTemplate;
    private final List<String> players;
    private long deckId;
    private volatile GameStatus gameStatus;

    private final Map<String, List<CardDto>> playerHands = new ConcurrentHashMap<>();
    private final Map<String, PlayerStatus> playerStatuses = new ConcurrentHashMap<>();
    private List<CardDto> dealerHand = new ArrayList<>();

    private volatile int currentPlayerIndex = 0;
    private final Object actionLock = new Object();


    public BlackjackGame(GameRoom room, DeckServiceClient deckService, SimpMessagingTemplate messagingTemplate) {
        this.room = room;
        this.deckService = deckService;
        this.messagingTemplate = messagingTemplate;
        this.players = new ArrayList<>(room.getPlayers());
        this.gameStatus = GameStatus.STARTING;
    }

    public void play() {
        try {
            initializeGame();
            broadcastState("The game has started. Dealing cards.", null);
            dealInitialCards();


            // Player turns
            this.gameStatus = GameStatus.PLAYER_TURN;
            for (currentPlayerIndex = 0; currentPlayerIndex < players.size(); currentPlayerIndex++) {
                String currentPlayerId = players.get(currentPlayerIndex);

                if(playerStatuses.get(currentPlayerId) == PlayerStatus.BLACKJACK){
                    continue; // Skip turn if player has blackjack
                }

                broadcastState(currentPlayerId + "'s turn.", null);

                // Wait for player action (Hit/Stand)
                synchronized (actionLock) {
                    try {
                        actionLock.wait(30000); // 30-second timeout
                    } catch (InterruptedException e) {
                        log.warn("Player {}'s turn was interrupted.", currentPlayerId);
                        Thread.currentThread().interrupt();
                    }
                }
                if (playerStatuses.get(currentPlayerId) == PlayerStatus.PLAYING) {
                    log.info("Player {} timed out. Standing automatically.", currentPlayerId);
                    stand(currentPlayerId);
                }
            }

            // Dealer's turn
            this.gameStatus = GameStatus.DEALER_TURN;
            broadcastState("Dealer's turn.", null);
            playDealerTurn();

            // End of round
            this.gameStatus = GameStatus.ROUND_FINISHED;
            determineWinners();

        } catch (Exception e) {
            log.error("An error occurred during the game in room {}: {}", room.getRoomId(), e.getMessage(), e);
            broadcastState("A critical error occurred. The game will be terminated.", null);
        } finally {
            log.info("Game finished in room {}", room.getRoomId());
            this.gameStatus = GameStatus.GAME_OVER;
            // The room will handle its own shutdown or restart logic
        }
    }

    private void initializeGame() {
        log.info("Initializing Blackjack game in room {}", room.getRoomId());
        DeckDto deck = deckService.createDeck(1);
        this.deckId = deck.id();
        players.forEach(p -> {
            playerHands.put(p, new ArrayList<>());
            playerStatuses.put(p, PlayerStatus.PLAYING);
        });
        dealerHand = new ArrayList<>();
    }

    private void dealInitialCards() {
        // Deal one card to each player, then one to dealer, repeat
        for (int i = 0; i < 2; i++) {
            for (String player : players) {
                deckService.drawCards(deckId, 1).forEach(card -> playerHands.get(player).add(card));
            }
            deckService.drawCards(deckId, 1).forEach(card -> dealerHand.add(card));
        }
        broadcastState(null, null);

        // Check for blackjacks
        for (String player : players) {
            if (calculateHandValue(playerHands.get(player)) == 21) {
                playerStatuses.put(player, PlayerStatus.BLACKJACK);
                log.info("Player {} has Blackjack!", player);
            }
        }
    }

    public void handlePlayerAction(String playerId, PlayerAction action) {
        if (!Objects.equals(players.get(currentPlayerIndex), playerId) || gameStatus != GameStatus.PLAYER_TURN) {
            log.warn("Player {} action out of turn.", playerId);
            return;
        }

        switch (action) {
            case PlayerAction.Hit hit -> this.hit(playerId);
            case PlayerAction.Stand stand -> this.stand(playerId);
        }
    }

    private void hit(String playerId) {
        log.info("Player {} hits.", playerId);
        deckService.drawCards(deckId, 1).forEach(card -> playerHands.get(playerId).add(card));
        int handValue = calculateHandValue(playerHands.get(playerId));
        if (handValue > 21) {
            playerStatuses.put(playerId, PlayerStatus.BUSTED);
            log.info("Player {} busted!", playerId);
            broadcastState("Player " + playerId + " busted!", null);
            // Notify the waiting thread to proceed to the next player
            synchronized (actionLock) {
                actionLock.notify();
            }
        } else {
            broadcastState(null, null);
        }
    }

    private void stand(String playerId) {
        log.info("Player {} stands.", playerId);
        playerStatuses.put(playerId, PlayerStatus.STAND);
        broadcastState("Player " + playerId + " stands.", null);
        // Notify the waiting thread to proceed to the next player
        synchronized (actionLock) {
            actionLock.notify();
        }
    }

    private void playDealerTurn() {
        while (calculateHandValue(dealerHand) < 17) {
            try {
                Thread.sleep(1500); // Pause for dramatic effect
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            deckService.drawCards(deckId, 1).forEach(dealerHand::add);
            broadcastState("Dealer hits.", null);
        }
        broadcastState("Dealer stands.", null);
    }

    private void determineWinners() {
        int dealerValue = calculateHandValue(dealerHand);
        boolean dealerBusted = dealerValue > 21;
        List<String> winners = new ArrayList<>();
        StringBuilder resultMessage = new StringBuilder("Game over! ");
        if(dealerBusted) resultMessage.append("Dealer busted. ");

        for (String player : players) {
            int playerValue = calculateHandValue(playerHands.get(player));
            PlayerStatus status = playerStatuses.get(player);

            if (status == PlayerStatus.BUSTED) {
                // Player loses
            } else if (status == PlayerStatus.BLACKJACK && dealerValue != 21) {
                winners.add(player);
            } else if (dealerBusted) {
                winners.add(player);
            } else {
                if (playerValue > dealerValue) {
                    winners.add(player);
                }
            }
        }
        if(winners.isEmpty()){
            resultMessage.append("Dealer wins.");
        } else {
            resultMessage.append("Winners: ").append(String.join(", ", winners));
        }

        broadcastState(resultMessage.toString(), winners);
    }

    private int calculateHandValue(List<CardDto> hand) {
        int value = 0;
        int aceCount = 0;
        for (CardDto card : hand) {
            if (card == null || card.value() == null) {
                continue;
            }
            switch (card.value()) {
                case "ACE" -> {
                    aceCount++;
                    value += 11;
                }
                case "KING", "QUEEN", "JACK" -> value += 10;
                case "HIDDEN" -> {
                    // Nic nie rób, wartość tej karty to 0
                }
                default -> value += Integer.parseInt(card.value());
            }
        }
        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }
        return value;
    }


    private void broadcastState(String message, List<String> winners) {
        String topic = "/topic/game/" + room.getRoomId();

        Map<String, PlayerHandDto> playerHandsDto = players.stream()
                .collect(Collectors.toMap(
                        p -> p,
                        p -> new PlayerHandDto(
                                p,
                                playerHands.get(p),
                                calculateHandValue(playerHands.get(p)),
                                playerStatuses.get(p).name()
                        )
                ));

        List<CardDto> dealerCardsToSend = new ArrayList<>();
        if (gameStatus == GameStatus.PLAYER_TURN || gameStatus == GameStatus.STARTING) {
            if (!dealerHand.isEmpty()) {
                dealerCardsToSend.add(dealerHand.get(0)); // Show only first card
                dealerCardsToSend.add(new CardDto("BK", "HIDDEN", null, null)); // Placeholder for the second
            }
        } else {
            dealerCardsToSend.addAll(dealerHand); // Show all cards
        }

        PlayerHandDto dealerHandDto = new PlayerHandDto("Dealer", dealerCardsToSend, calculateHandValue(dealerCardsToSend), "");

        String currentTurnPlayer = (gameStatus == GameStatus.PLAYER_TURN && currentPlayerIndex < players.size()) ? players.get(currentPlayerIndex) : null;

        GameStateDto state = new GameStateDto(
                gameStatus.name(),
                playerHandsDto,
                dealerHandDto,
                currentTurnPlayer,
                BigDecimal.valueOf(room.getEntryFee() * room.getPlayers().size()),
                winners,
                message
        );

        log.debug("Broadcasting state to {}: {}", topic, state);
        messagingTemplate.convertAndSend(topic, state);
    }
}