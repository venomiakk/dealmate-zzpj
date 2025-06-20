package pl.zzpj.dealmate.gameservice.game.blackjack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.game.dto.*;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.model.GameResult;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class BlackjackGame {
    private final GameRoom room;
    private final DeckServiceClient deckService;
    private final SimpMessagingTemplate messagingTemplate;
    private final List<String> activePlayers;
    private final GameHistoryService gameHistoryService;
    private long deckId;

    // ZMIANA: Użycie klas atomowych dla bezpieczeństwa wątkowego
    private final AtomicReference<GameStatus> gameStatus = new AtomicReference<>();
    private final AtomicInteger currentPlayerIndex = new AtomicInteger(0);

    private final Map<String, List<CardDto>> playerHands = new ConcurrentHashMap<>();
    private final Map<String, PlayerStatus> playerStatuses = new ConcurrentHashMap<>();
    private List<CardDto> dealerHand = new ArrayList<>();

    private final Object actionLock = new Object();

    public BlackjackGame(GameRoom room, DeckServiceClient deckService, SimpMessagingTemplate messagingTemplate, List<String> activePlayers, GameHistoryService gameHistoryService) {
        this.room = room;
        this.deckService = deckService;
        this.messagingTemplate = messagingTemplate;
        this.activePlayers = activePlayers;
        this.gameHistoryService = gameHistoryService;
        this.gameStatus.set(GameStatus.STARTING); // ZMIANA: Użycie .set()
    }

    public List<String> play() {
        try {
            initializeGame();
            broadcastState("The round has started. Dealing cards.", null, null);
            dealInitialCards();

            this.gameStatus.set(GameStatus.PLAYER_TURN); // ZMIANA: Użycie .set()

            // ZMIANA: Pętla używa teraz AtomicInteger
            for (int i = 0; i < activePlayers.size(); i++) {
                currentPlayerIndex.set(i);
                String currentPlayerId = activePlayers.get(i);

                if (playerStatuses.get(currentPlayerId) == PlayerStatus.BLACKJACK) {
                    continue;
                }

                if (!room.getPlayers().containsKey(currentPlayerId)) {
                    log.info("Player {} left before their turn. Skipping.", currentPlayerId);
                    continue;
                }

                broadcastState(currentPlayerId + "'s turn.", null, null);

                synchronized (actionLock) {
                    try {
                        actionLock.wait(30000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Player {}'s turn was interrupted, possibly by leaving.", currentPlayerId);
                    }
                }
                if (playerStatuses.get(currentPlayerId) == PlayerStatus.PLAYING) {
                    log.info("Player {} timed out or left. Standing automatically.", currentPlayerId);
                    stand(currentPlayerId);
                }
            }

            this.gameStatus.set(GameStatus.DEALER_TURN); // ZMIANA: Użycie .set()
            broadcastState("Dealer's turn.", null, null);
            playDealerTurn();

            this.gameStatus.set(GameStatus.ROUND_FINISHED); // ZMIANA: Użycie .set()
            return determineWinners();

        } catch (Exception e) {
            log.error("An error occurred during the round in room {}: {}", room.getRoomId(), e.getMessage(), e);
            broadcastState("A critical error occurred in this round.", null, null);
            return new ArrayList<>();
        }
    }

    private List<String> determineWinners() {
        int dealerValue = calculateHandValue(dealerHand);
        boolean dealerBusted = dealerValue > 21;
        List<String> winners = new ArrayList<>();
        StringBuilder resultMessage = new StringBuilder("Round finished! ");
        if(dealerBusted) resultMessage.append("Dealer busted. ");

        BigDecimal entryFee = BigDecimal.valueOf(room.getEntryFee());

        for (String player : activePlayers) {
            if (!room.getPlayers().containsKey(player)) continue;

            int playerValue = calculateHandValue(playerHands.get(player));
            PlayerStatus status = playerStatuses.get(player);
            GameResult gameResult;

            if (status == PlayerStatus.BUSTED) {
                gameResult = GameResult.LOSS;
            } else if (status == PlayerStatus.BLACKJACK && dealerValue != 21) {
                winners.add(player);
                gameResult = GameResult.BLACKJACK_WIN;
            } else if (dealerBusted) {
                winners.add(player);
                gameResult = GameResult.WIN;
            } else if (playerValue > dealerValue) {
                winners.add(player);
                gameResult = GameResult.WIN;
            } else if (playerValue < dealerValue) {
                gameResult = GameResult.LOSS;
            } else {
                gameResult = GameResult.PUSH;
            }

            try {
                gameHistoryService.recordGameResults(player, gameResult, entryFee);
            } catch (Exception e) {
                log.error("Failed to record game result for player {}: {}", player, e.getMessage());
            }
        }

        if(winners.isEmpty()){
            resultMessage.append("Dealer wins.");
        } else {
            resultMessage.append("Winners: ").append(String.join(", ", winners));
        }

        broadcastState(resultMessage.toString(), winners, null);
        return winners;
    }

    private void initializeGame() {
        log.info("Initializing new round in room {} with players: {}", room.getRoomId(), activePlayers);
        DeckDto deck = deckService.createDeck(1);
        this.deckId = deck.id();
        activePlayers.forEach(p -> {
            playerHands.put(p, new ArrayList<>());
            playerStatuses.put(p, PlayerStatus.PLAYING);
        });
        dealerHand = new ArrayList<>();
    }

    private void dealInitialCards() {
        for (int i = 0; i < 2; i++) {
            for (String player : activePlayers) {
                deckService.drawCards(deckId, 1).forEach(card -> playerHands.get(player).add(card));
            }
            deckService.drawCards(deckId, 1).forEach(card -> dealerHand.add(card));
        }
        broadcastState(null, null, null);

        for (String player : activePlayers) {
            if (calculateHandValue(playerHands.get(player)) == 21) {
                playerStatuses.put(player, PlayerStatus.BLACKJACK);
                log.info("Player {} has Blackjack!", player);
            }
        }
    }

    public void handlePlayerAction(String playerId, PlayerAction action) {
        // ZMIANA: Użycie .get() do odczytu wartości
        if (!activePlayers.contains(playerId) || currentPlayerIndex.get() >= activePlayers.size() || !Objects.equals(activePlayers.get(currentPlayerIndex.get()), playerId) || gameStatus.get() != GameStatus.PLAYER_TURN) {
            log.warn("Player {} action out of turn or is not an active player.", playerId);
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
            broadcastState("Player " + playerId + " busted!", null, null);
            synchronized (actionLock) {
                actionLock.notify();
            }
        } else {
            broadcastState(null, null, null);
        }
    }

    private void stand(String playerId) {
        log.info("Player {} stands.", playerId);
        playerStatuses.put(playerId, PlayerStatus.STAND);
        broadcastState("Player " + playerId + " stands.", null, null);
        synchronized (actionLock) {
            actionLock.notify();
        }
    }

    private void playDealerTurn() {
        while (calculateHandValue(dealerHand) < 17) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Dealer's turn was interrupted during sleep.");
                break;
            }
            deckService.drawCards(deckId, 1).forEach(dealerHand::add);
            broadcastState("Dealer hits.", null, null);
        }
        broadcastState("Dealer stands.", null, null);
    }

    private int calculateHandValue(List<CardDto> hand) {
        int value = 0;
        int aceCount = 0;
        if (hand == null) return 0; // Dodatkowe zabezpieczenie
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
                case "HIDDEN" -> {}
                default -> value += Integer.parseInt(card.value());
            }
        }
        while (value > 21 && aceCount > 0) {
            value -= 10;
            aceCount--;
        }
        return value;
    }

    public void broadcastState(String message, List<String> winners, Integer countdown) {
        String topic = "/topic/game/" + room.getRoomId();

        Map<String, PlayerHandDto> playerHandsDto = activePlayers.stream()
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
        // ZMIANA: Użycie .get() do odczytu wartości
        if (gameStatus.get() == GameStatus.PLAYER_TURN || gameStatus.get() == GameStatus.STARTING) {
            if (!dealerHand.isEmpty()) {
                dealerCardsToSend.add(dealerHand.get(0));
                dealerCardsToSend.add(new CardDto("BK", "HIDDEN", null, null));
            }
        } else {
            dealerCardsToSend.addAll(dealerHand);
        }

        PlayerHandDto dealerHandDto = new PlayerHandDto("Dealer", dealerCardsToSend, calculateHandValue(dealerCardsToSend), "");

        String currentTurnPlayer = getCurrentPlayerId();

        GameStateDto state = new GameStateDto(
                gameStatus.get().name(), // ZMIANA: Użycie .get()
                playerHandsDto,
                dealerHandDto,
                currentTurnPlayer,
                BigDecimal.valueOf(room.getEntryFee() * activePlayers.size()),
                winners,
                message,
                countdown
        );

        messagingTemplate.convertAndSend(topic, state);
    }

    public String getCurrentPlayerId() {
        // ZMIANA: Użycie .get() do odczytu wartości
        if (gameStatus.get() == GameStatus.PLAYER_TURN && currentPlayerIndex.get() < activePlayers.size()) {
            return activePlayers.get(currentPlayerIndex.get());
        }
        return null;
    }

    public void skipCurrentPlayerTurn() {
        synchronized (actionLock) {
            actionLock.notifyAll();
        }
    }
}