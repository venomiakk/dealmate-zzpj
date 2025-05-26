package pl.zzpj.dealmate.gameservice.model;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class GameRoom implements Runnable {

    private final String roomId;
    private final String joinCode;
    private final Set<String> players = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<String> events = new LinkedBlockingQueue<>();
    private final SimpMessagingTemplate messagingTemplate;

    public GameRoom(SimpMessagingTemplate messagingTemplate) {
        this.roomId = UUID.randomUUID().toString();
        this.joinCode = new BigInteger(30, new SecureRandom()).toString(32);
        this.messagingTemplate = messagingTemplate;
        Executors.newVirtualThreadPerTaskExecutor().submit(this);
    }

    public String getRoomId() {
        return roomId;
    }

    public String getJoinCode() {
        return joinCode;
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