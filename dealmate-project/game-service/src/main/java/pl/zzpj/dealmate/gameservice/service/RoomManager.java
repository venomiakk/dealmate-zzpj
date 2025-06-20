package pl.zzpj.dealmate.gameservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient; 
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.model.GameRoom;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomManager {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final ChatServiceClient chatServiceClient;
    private final DeckServiceClient deckServiceClient;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserServiceClient userServiceClient;
    private final GameHistoryService gameHistoryService;

    public GameRoom createRoom(CreateRoomRequest request) {
        GameRoom room = new GameRoom(request, this, userServiceClient, chatServiceClient, deckServiceClient, gameHistoryService, messagingTemplate);
        rooms.put(room.getRoomId(), room);

        Thread gameThread = Thread.ofVirtual().start(room);
        room.setGameThread(gameThread);

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