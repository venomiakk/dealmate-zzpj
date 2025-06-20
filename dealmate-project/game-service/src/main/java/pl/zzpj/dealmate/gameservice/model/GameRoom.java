package pl.zzpj.dealmate.gameservice.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.RoomStateUpdateDto;
import pl.zzpj.dealmate.gameservice.game.blackjack.BlackjackGame;
import pl.zzpj.dealmate.gameservice.game.dto.PlayerAction;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final Set<String> players;
    private final RoomManager roomManager;
    private final ChatServiceClient chatServiceClient;
    private final DeckServiceClient deckServiceClient;
    private final SimpMessagingTemplate messagingTemplate;

    private final Object gameStartLock = new Object();
    private final AtomicBoolean gameHasStarted = new AtomicBoolean(false);
    private volatile BlackjackGame game;

    public GameRoom(CreateRoomRequest request, RoomManager roomManager, ChatServiceClient chatServiceClient, DeckServiceClient deckServiceClient, SimpMessagingTemplate messagingTemplate) {
        this.roomId = UUID.randomUUID().toString();
        this.name = request.name();
        this.gameType = request.gameType().name();
        this.maxPlayers = request.maxPlayers();
        this.isPublic = request.isPublic();
        this.joinCode = generateJoinCode();
        this.ownerLogin = request.ownerLogin();
        this.entryFee = request.entryFee() != null ? request.entryFee() : 0;
        this.players = new CopyOnWriteArraySet<>();
        this.roomManager = roomManager;
        this.chatServiceClient = chatServiceClient;
        this.deckServiceClient = deckServiceClient;
        this.messagingTemplate = messagingTemplate;
        log.info("GameRoom created: {}", this.roomId);
    }

    @Override
    public void run() {
        log.info("Virtual thread for room {} started. Waiting for start signal...", roomId);
        synchronized (gameStartLock) {
            try {
                gameStartLock.wait(); // Wait until signalGameStart() is called
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Game room {} thread interrupted while waiting to start.", roomId);
                return;
            }
        }

        log.info("Game is starting in room {}", roomId);
        if (EGameType.valueOf(this.gameType) == EGameType.BLACKJACK) {
            this.game = new BlackjackGame(this, deckServiceClient, messagingTemplate);
            this.game.play(); // This method blocks until the game is over
        } else {
            log.warn("Game type {} is not yet implemented.", this.gameType);
        }
        log.info("Game has finished in room {}. Thread is ending.", roomId);
    }

    public void signalGameStart() {
        if (gameHasStarted.compareAndSet(false, true)) {
            synchronized (gameStartLock) {
                gameStartLock.notify();
            }
        }
    }

    public void handlePlayerAction(String playerId, PlayerAction action) {
        if(game != null) {
            game.handlePlayerAction(playerId, action);
        } else {
            log.warn("Action received but no game is running in room {}", roomId);
        }
    }

    private String generateJoinCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public boolean join(String playerId) {
        if (gameHasStarted.get()) {
            log.warn("Player {} cannot join room {}: game has already started.", roomId, playerId);
            return false;
        }
        if (players.size() >= maxPlayers) {
            log.warn("Room {} is full. Player {} cannot join.", roomId, playerId);
            return false;
        }
        if (players.add(playerId)) {
            log.info("Player {} joined room {}", playerId, roomId);
            notifyChatService(playerId + " has joined the room.");
            broadcastPlayersUpdate(); // Ważna linia
            return true;
        }
        return false;
    }

    public boolean leave(String playerId) {
        if (players.remove(playerId)) {
            log.info("Player {} left room {}", playerId, roomId);
            notifyChatService(playerId + " has left the room.");
            broadcastPlayersUpdate(); // Ważna linia
            if (players.isEmpty()) {
                log.info("Room {} is empty. Removing room.", roomId);
                roomManager.removeRoom(roomId);
            }
            return true;
        }
        return false;
    }

    private void broadcastPlayersUpdate() {
        String topic = "/topic/room/" + this.roomId + "/players";
        Map<String, Set<String>> payload = Map.of("players", this.getPlayers());
        messagingTemplate.convertAndSend(topic, payload);
        log.info("Broadcasted player list update to topic {}: {}", topic, payload);
    }

    private void notifyChatService(String systemMessage) {
        try {
            RoomStateUpdateDto update = new RoomStateUpdateDto(this.roomId, this.getPlayers(), systemMessage);
            chatServiceClient.notifyRoomStateChange(update);
        } catch (Exception e) {
            log.error("Failed to notify ChatService for room {}: {}", this.roomId, e.getMessage());
        }
    }

    public Set<String> getPlayers() {
        return Collections.unmodifiableSet(players);
    }
}