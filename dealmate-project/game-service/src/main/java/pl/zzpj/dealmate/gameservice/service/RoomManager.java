package pl.zzpj.dealmate.gameservice.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.model.GameRoom;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomManager {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public RoomManager(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public GameRoom createRoom(CreateRoomRequest request) {
        GameRoom room = new GameRoom(messagingTemplate, request);
        rooms.put(room.getRoomId(), room);
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
}

