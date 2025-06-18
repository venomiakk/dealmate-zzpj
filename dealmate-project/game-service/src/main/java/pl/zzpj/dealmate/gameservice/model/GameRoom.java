package pl.zzpj.dealmate.gameservice.model;

import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class GameRoom implements Runnable {

    @Getter
    private final String roomId;
    @Getter
    private final String joinCode;
    @Getter
    private final String name;
    @Getter
    private final EGameType gameType;
    @Getter
    private final int maxPlayers;
    @Getter
    private final boolean isPublic;
    private final Set<String> players = ConcurrentHashMap.newKeySet();
    @Getter
    private final BlockingQueue<String> events = new LinkedBlockingQueue<>();
    private final SimpMessagingTemplate messagingTemplate;

    public GameRoom(SimpMessagingTemplate messagingTemplate, CreateRoomRequest request) {
        this.roomId = UUID.randomUUID().toString();
        this.joinCode = new BigInteger(30, new SecureRandom()).toString(32);
        if (request.name() == null || request.name().isBlank()) {
            this.name = "Room " + roomId.substring(0, 8); // Default name if not provided
        } else {
            this.name = request.name();
        }
        this.gameType = request.gameType();
        this.maxPlayers = request.maxPlayers();
        this.isPublic = request.isPublic();
        this.messagingTemplate = messagingTemplate;
        Executors.newVirtualThreadPerTaskExecutor().submit(this);
    }

    /**
     *  Test constructor.
     *  Allows for creating a GameRoom instance without auto-starting the event loop.
     */
    GameRoom(SimpMessagingTemplate messagingTemplate, CreateRoomRequest request, boolean autoStart) {
        this.roomId = UUID.randomUUID().toString();
        this.joinCode = new BigInteger(30, new SecureRandom()).toString(32);
        if (request.name() == null || request.name().isBlank()) {
            this.name = "Room " + roomId.substring(0, 8); // Default name if not provided
        } else {
            this.name = request.name();
        }
        this.gameType = request.gameType();
        this.maxPlayers = request.maxPlayers();
        this.isPublic = request.isPublic();
        this.messagingTemplate = messagingTemplate;
        if (autoStart) {
            Executors.newVirtualThreadPerTaskExecutor().submit(this);
        }
    }

    public void join(String playerId) {
        players.add(playerId);
        events.add("JOIN:" + playerId);
    }

    public void leave(String playerId) {
        players.remove(playerId);
        events.add("LEAVE:" + playerId);
    }

    public Set<String> getPlayers() {
        return Set.copyOf(players); // unmodifiable snapshot
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                String event = events.take();
                messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}