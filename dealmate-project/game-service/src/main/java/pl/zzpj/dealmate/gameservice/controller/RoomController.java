package pl.zzpj.dealmate.gameservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.RoomInfo;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/game")
public class RoomController {

    private final RoomManager roomManager;

    public RoomController(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(Authentication auth, @RequestBody CreateRoomRequest request) {
        String playerId = auth.getName();
        log.info("Creating room: {}", request);
        GameRoom room = roomManager.createRoom(request);
        room.join(playerId);

        return ResponseEntity.ok()
                .body(new RoomInfo(room.getRoomId(), room.getJoinCode(), room.getPlayers(),
                        room.getName(), room.getGameType(), room.getMaxPlayers(), room.isPublic()));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId, Authentication auth) {
        String playerId = auth.getName();
        return roomManager.getRoomById(roomId)
                .map(room -> {
                    room.join(playerId);
                    return ResponseEntity.ok("Player " + playerId + " joined room " + roomId);
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomId, Authentication auth) {
        String playerId = auth.getName();
        return roomManager.getRoomById(roomId)
                .map(room -> {
                    room.leave(playerId);
                    return ResponseEntity.ok("Player " + playerId + " left room " + roomId);
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/join-code/{code}")
    public ResponseEntity<?> joinByCode(@PathVariable String code, Authentication auth) {
        String playerId = auth.getName();
        return roomManager.getRoomByJoinCode(code)
                .map(room -> {
                    room.join(playerId);
                    return ResponseEntity.ok("Player " + playerId + " joined room by code " + code);
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RoomInfo>> listAllRooms() {
        List<RoomInfo> result = roomManager.getAllRooms().stream()
                .map(room -> new RoomInfo(room.getRoomId(), room.getJoinCode(), room.getPlayers(),
                        room.getName(), room.getGameType(), room.getMaxPlayers(), room.isPublic()))
                .toList();
        return ResponseEntity.ok(result);
    }

}
