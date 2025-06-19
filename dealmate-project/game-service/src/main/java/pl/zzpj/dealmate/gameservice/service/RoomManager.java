package pl.zzpj.dealmate.gameservice.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.model.GameRoom;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j; // Import Slf4j for logging

@Slf4j // Add Slf4j annotation
@Service
public class RoomManager {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final ChatServiceClient chatServiceClient; // <-- Nowe pole

    public RoomManager(ChatServiceClient chatServiceClient) { // <-- Zaktualizowany konstruktor
        this.chatServiceClient = chatServiceClient;
    }

    public GameRoom createRoom(CreateRoomRequest request) {
        // Przekaż klienta do konstruktora GameRoom
        GameRoom room = new GameRoom(request, this, chatServiceClient);
        rooms.put(room.getRoomId(), room);
        log.info("RoomManager created room with ID: {}", room.getRoomId());
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

    // New method to remove a room
    public void removeRoom(String roomId) {
        GameRoom removedRoom = rooms.remove(roomId);
        if (removedRoom != null) {
            log.info("Room {} removed from RoomManager.", roomId);
// Optionally, send a message to a general topic about room deletion
// This is already handled by GameRoom.leave() for `sendRoomUpdateToAllRoomsTopic`
        } else {
            log.warn("Attempted to remove non-existent room: {}", roomId);
        }
    }
}