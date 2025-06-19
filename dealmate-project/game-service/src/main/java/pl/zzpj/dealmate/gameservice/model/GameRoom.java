package pl.zzpj.dealmate.gameservice.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.RoomInfo; // Import RoomInfo DTO
import pl.zzpj.dealmate.gameservice.dto.ChatMessage; // New ChatMessage DTO
import pl.zzpj.dealmate.gameservice.service.RoomManager; // To notify RoomManager for removal
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.dto.RoomStateUpdateDto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet; // For thread-safe player set

@Slf4j
@Getter
public class GameRoom {
    private final String roomId;
    private final String name;
    private final String gameType; // This is a String
    private final int maxPlayers;
    private final boolean isPublic;
    private final String joinCode;
    private final String ownerLogin;
    private final double entryFee;

    private final Set<String> players;
    private RoomManager roomManager; // To allow GameRoom to notify RoomManager about its state
    private final ChatServiceClient chatServiceClient; // <-- Nowe pole

    public GameRoom(CreateRoomRequest request, RoomManager roomManager, ChatServiceClient chatServiceClient) {
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
        this.chatServiceClient = chatServiceClient; // <-- Inicjalizacja
        log.info("GameRoom created: {}", this.roomId);
    }

    // Setter for RoomManager, to be called after creation in RoomManager
    public void setRoomManager(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    private String generateJoinCode() {
// Generate a simple 6-character alphanumeric code
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public boolean join(String playerId) {
        if (players.size() >= maxPlayers) {
            log.warn("Room {} is full. Player {} cannot join.", roomId, playerId);
            return false;
        }
        if (players.add(playerId)) {
            log.info("Player {} joined room {}", playerId, roomId);
            // Wyślij powiadomienie do ChatService
            notifyChatService(playerId + " has joined the room.");
            return true;
        }
        return false;
    }

    public boolean leave(String playerId) {
        if (players.remove(playerId)) {
            log.info("Player {} left room {}", playerId, roomId);

            // Sprawdź, czy pokój jest pusty i usuń go
            if (players.isEmpty() && roomManager != null) {
                log.info("Room {} is empty. Removing room.", roomId);
                // Najpierw wyślij ostatnią aktualizację, że gracz wyszedł
                notifyChatService(playerId + " has left the room. The room is now empty.");
                roomManager.removeRoom(roomId);
            } else {
                // Jeśli pokój nie jest pusty, po prostu wyślij aktualizację
                notifyChatService(playerId + " has left the room.");
            }
            return true;
        }
        return false;
    }

    private void notifyChatService(String systemMessage) {
        try {
            RoomStateUpdateDto update = new RoomStateUpdateDto(this.roomId, this.getPlayers(), systemMessage);
            chatServiceClient.notifyRoomStateChange(update);
            log.info("Notified ChatService about update in room {}", this.roomId);
        } catch (Exception e) {
            log.error("Failed to notify ChatService for room {}: {}", this.roomId, e.getMessage());
            // Tutaj można dodać logikę ponawiania lub obsługi błędów
        }
    }

    public Set<String> getPlayers() {
        return Collections.unmodifiableSet(players);
    }
}