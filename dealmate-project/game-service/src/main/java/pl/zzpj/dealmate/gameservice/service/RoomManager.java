package pl.zzpj.dealmate.gameservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.model.GameRoom;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RoomManager {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final ChatServiceClient chatServiceClient;
    private final DeckServiceClient deckServiceClient;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomManager(ChatServiceClient chatServiceClient, DeckServiceClient deckServiceClient, SimpMessagingTemplate messagingTemplate) {
        this.chatServiceClient = chatServiceClient;
        this.deckServiceClient = deckServiceClient;
        this.messagingTemplate = messagingTemplate;
    }

    public GameRoom createRoom(CreateRoomRequest request) {
        GameRoom room = new GameRoom(request, this, chatServiceClient, deckServiceClient, messagingTemplate);
        rooms.put(room.getRoomId(), room);
        Thread.ofVirtual().start(room); // Start the virtual thread for the room
        log.info("RoomManager created room with ID: {} and started its virtual thread.", room.getRoomId());
        return room;
    }

    public Optional<GameRoom> getRoomById(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public Optional<GameRoom> getRoomByJoinCode(String code) {
        return rooms.values().stream()
                .filter(room -> room.getJoinCode().equals(code))
                .findFirst();
    }

    public Collection<GameRoom> getAllRooms() {
        return rooms.values();
    }

    public void removeRoom(String roomId) {
        GameRoom removedRoom = rooms.remove(roomId);
        if (removedRoom != null) {
            log.info("Room {} removed from RoomManager.", roomId);
        } else {
            log.warn("Attempted to remove non-existent room: {}", roomId);
        }
    }
}