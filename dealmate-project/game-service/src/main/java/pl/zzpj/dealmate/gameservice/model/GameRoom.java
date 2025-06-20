package pl.zzpj.dealmate.gameservice.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.PlayerDto;
import pl.zzpj.dealmate.gameservice.dto.RoomStateUpdateDto;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;
import pl.zzpj.dealmate.gameservice.game.blackjack.BlackjackGame;
import pl.zzpj.dealmate.gameservice.game.dto.PlayerAction;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference; // ZMIANA: Nowy import

@Slf4j
@Getter
public class GameRoom implements Runnable {
    private final String roomId;
    private final String name;
    private final String gameType;
    private final int maxPlayers;
    private final boolean isPublic;
    private final String joinCode;
    private final String ownerLogin;
    private final double entryFee;

    private final Map<String, PlayerDto> players;
    private final RoomManager roomManager;
    private final UserServiceClient userServiceClient;
    private final ChatServiceClient chatServiceClient;
    private final DeckServiceClient deckServiceClient;
    private final GameHistoryService gameHistoryService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Object gameStartLock = new Object();
    private final AtomicBoolean gameHasStartedSignal = new AtomicBoolean(false);

    // ZMIANA: Użycie AtomicReference do bezpiecznego zarządzania referencją do obiektu gry
    private final AtomicReference<BlackjackGame> currentGame = new AtomicReference<>();

    @Setter
    private Thread gameThread;
    private List<String> lastRoundWinners = new ArrayList<>();

    public GameRoom(CreateRoomRequest request, RoomManager roomManager, UserServiceClient userServiceClient, ChatServiceClient chatServiceClient, DeckServiceClient deckServiceClient, GameHistoryService gameHistoryService, SimpMessagingTemplate messagingTemplate) {
        this.roomId = UUID.randomUUID().toString();
        this.name = request.name();
        this.gameType = request.gameType().name();
        this.maxPlayers = request.maxPlayers();
        this.isPublic = request.isPublic();
        this.joinCode = generateJoinCode();
        this.ownerLogin = request.ownerLogin();
        this.entryFee = request.entryFee() != null ? request.entryFee() : 0;
        this.players = new ConcurrentHashMap<>();
        this.roomManager = roomManager;
        this.userServiceClient = userServiceClient;
        this.chatServiceClient = chatServiceClient;
        this.deckServiceClient = deckServiceClient;
        this.gameHistoryService = gameHistoryService;
        this.messagingTemplate = messagingTemplate;
        log.info("GameRoom created: {}", this.roomId);
    }

    @Override
    public void run() {
        log.info("Virtual thread for room {} started. Waiting for start signal...", roomId);
        synchronized (gameStartLock) {
            try {
                if (!gameHasStartedSignal.get()) {
                    gameStartLock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Game room {} thread interrupted before start. Closing.", roomId);
                roomManager.removeRoom(this.roomId);
                return;
            }
        }
        log.info("Initial game start signal received for room {}", roomId);

        while (!Thread.currentThread().isInterrupted()) {
            if (players.isEmpty()) {
                log.info("Room {} is empty. Stopping game loop.", roomId);
                break;
            }

            updateAllPlayerCredits();
            List<String> activePlayers = new ArrayList<>(this.players.keySet());
            log.info("Starting new round in room {} with players: {}", roomId, activePlayers);

            // ZMIANA: Używamy .set() do aktualizacji referencji
            this.currentGame.set(new BlackjackGame(this, deckServiceClient, messagingTemplate, activePlayers, gameHistoryService));
            this.lastRoundWinners = this.currentGame.get().play();

            log.info("Round finished in room {}. Winners: {}. Starting 5 second countdown.", roomId, lastRoundWinners);

            for (int i = 5; i > 0; i--) {
                BlackjackGame game = this.currentGame.get(); // ZMIANA: Używamy .get() do odczytu
                if (game != null) {
                    game.broadcastState("New round starting soon...", this.lastRoundWinners, i);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (Thread.currentThread().isInterrupted()) break;
        }
        log.info("Game loop for room {} has ended. Removing room.", roomId);
        roomManager.removeRoom(this.roomId);
    }

    public boolean join(String playerId) {
        if (players.size() >= maxPlayers) {
            return false;
        }
        UserDetailsDto userDetails = userServiceClient.getUserByUsername(playerId);
        if (userDetails == null) {
            log.error("Cannot find user details for {}", playerId);
            return false;
        }

        PlayerDto player = new PlayerDto(playerId, userDetails.credits());
        players.put(playerId, player);

        log.info("Player {} joined room {}", playerId, roomId);
        notifyChatService(playerId + " has joined the room.");
        broadcastPlayersUpdate();

        BlackjackGame game = currentGame.get(); // ZMIANA: Używamy .get()
        if (game != null) {
            game.broadcastState("You are spectating. Waiting for the next round.", this.lastRoundWinners, null);
        }
        return true;
    }

    public boolean leave(String playerId) {
        if (players.remove(playerId) != null) {
            log.info("Player {} left room {}", playerId, roomId);
            BlackjackGame game = this.currentGame.get(); // ZMIANA: Używamy .get()
            if (game != null && playerId.equals(game.getCurrentPlayerId())) {
                game.skipCurrentPlayerTurn();
            }
            notifyChatService(playerId + " has left the room.");
            broadcastPlayersUpdate();
            if (players.isEmpty() && gameThread != null) {
                gameThread.interrupt();
            }
            return true;
        }
        return false;
    }

    private void updateAllPlayerCredits() {
        log.info("Updating credit info for all players in room {}", roomId);
        for(String playerId : players.keySet()) {
            try {
                UserDetailsDto userDetails = userServiceClient.getUserByUsername(playerId);
                if (userDetails != null) {
                    players.put(playerId, new PlayerDto(playerId, userDetails.credits()));
                }
            } catch (Exception e) {
                log.error("Could not update credits for player {}: {}", playerId, e.getMessage());
            }
        }
        broadcastPlayersUpdate();
    }

    private void broadcastPlayersUpdate() {
        String topic = "/topic/room/" + this.roomId + "/players";
        Map<String, Collection<PlayerDto>> payload = Map.of("players", this.players.values());
        messagingTemplate.convertAndSend(topic, payload);
        log.info("Broadcasted player list update to topic {}", topic);
    }

    public void signalGameStart() {
        if (gameHasStartedSignal.compareAndSet(false, true)) {
            synchronized (gameStartLock) {
                gameStartLock.notifyAll();
            }
        }
    }

    public void handlePlayerAction(String playerId, PlayerAction action) {
        BlackjackGame game = currentGame.get(); // ZMIANA: Używamy .get()
        if(game != null) {
            game.handlePlayerAction(playerId, action);
        } else {
            log.warn("Action received but no game is running in room {}", roomId);
        }
    }

    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void notifyChatService(String systemMessage) {
        try {
            Set<String> playerNames = players.keySet();
            RoomStateUpdateDto update = new RoomStateUpdateDto(this.roomId, playerNames, systemMessage);
            chatServiceClient.notifyRoomStateChange(update);
        } catch (Exception e) {
            log.error("Failed to notify ChatService for room {}: {}", this.roomId, e.getMessage());
        }
    }

    // Setter tylko na potrzeby testów jednostkowych
    public void setCurrentGame(BlackjackGame game) {
        this.currentGame.set(game);
    }
}
