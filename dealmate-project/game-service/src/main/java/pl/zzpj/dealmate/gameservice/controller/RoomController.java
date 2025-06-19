package pl.zzpj.dealmate.gameservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.RoomInfo;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.util.List;
import java.util.Set; // Import Set as it's used in RoomInfo

@Slf4j
@RestController
@RequestMapping("/game")
public class RoomController {

    private final RoomManager roomManager;
    private final UserServiceClient userServiceClient;

    public RoomController(RoomManager roomManager, UserServiceClient userServiceClient) {
        this.roomManager = roomManager;
        this.userServiceClient = userServiceClient;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(Authentication auth, @RequestBody CreateRoomRequest request) {
        String playerId = auth.getName();

        UserDetailsDto user = userServiceClient.getUserByUsername(playerId);
        if (user == null) {
            log.error("User not found while creating room: {}", playerId);
            return ResponseEntity.badRequest().body("User not found");
        }
// Simplified credit check for demonstration.
// In a real app, you'd manage transactions carefully.
        if (request.entryFee() > 0) {
            if (user.credits() == null || user.credits() < request.entryFee()) {
                log.error("User {} does not have enough credits ({}) to create room with entry fee {}",
                        playerId, user.credits(), request.entryFee());
                return ResponseEntity.badRequest().body("Not enough credits to create a room");
            }
// Deduction of credits would happen here or after game completion
        }

        log.info("Creating room: {}", request);
        GameRoom room = roomManager.createRoom(request);
        room.join(playerId); // Player joins immediately after creating

        return ResponseEntity.ok()
                .body(new RoomInfo(room.getRoomId(), room.getJoinCode(), room.getPlayers(),
                        room.getName(), room.getGameType(), room.getMaxPlayers(), room.isPublic(), room.getOwnerLogin(),
                        room.getEntryFee()));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId, Authentication auth) {
        String playerId = auth.getName();
        GameRoom room = roomManager.getRoomById(roomId)
                .orElse(null);
        if (room == null) {
            log.error("Room not found: {}", roomId);
            return ResponseEntity.notFound().build();
        }
        UserDetailsDto user = userServiceClient.getUserByUsername(playerId);
        if (user == null) {
            log.error("User not found while joining room: {}", playerId);
            return ResponseEntity.badRequest().body("User not found");
        }
        if (room.getEntryFee() > 0) {
            if (user.credits() == null || user.credits() < room.getEntryFee()) {
                log.error("User {} does not have enough credits ({}) to join room {} with entry fee {}",
                        playerId, user.credits(), roomId, room.getEntryFee());
                return ResponseEntity.badRequest().body("Not enough credits to join a room");
            }
// Deduction of credits would happen here or after game completion
        }

        if (room.join(playerId)) {
            return ResponseEntity.ok("Player " + playerId + " joined room " + roomId);
        } else {
            return ResponseEntity.badRequest().body("Failed to join room, it might be full.");
        }
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomId, Authentication auth) {
        String playerId = auth.getName();
        return roomManager.getRoomById(roomId)
                .map(room -> {
                    if (room.leave(playerId)) {
                        return ResponseEntity.ok("Player " + playerId + " left room " + roomId);
                    } else {
                        return ResponseEntity.badRequest().body("Player " + playerId + " was not in room " + roomId);
                    }
                }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/join-code/{code}")
    public ResponseEntity<?> joinByCode(@PathVariable String code, Authentication auth) {
        String playerId = auth.getName();
        GameRoom room = roomManager.getRoomByJoinCode(code)
                .orElse(null);
        if (room == null) {
            log.error("Room not found with join code: {}", code);
            return ResponseEntity.notFound().build();
        }
        UserDetailsDto user = userServiceClient.getUserByUsername(playerId);
        if (user == null) {
            log.error("User not found while joining room by code: {}", playerId);
            return ResponseEntity.badRequest().body("User not found");
        }
        if (room.getEntryFee() > 0) {
            if (user.credits() == null || user.credits() < room.getEntryFee()) {
                log.error("User {} does not have enough credits ({}) to join room by code {} with entry fee {}",
                        playerId, user.credits(), code, room.getEntryFee());
                return ResponseEntity.badRequest().body("Not enough credits to join a room by code");
            }
// Deduction of credits would happen here or after game completion
        }
        if (room.join(playerId)) {
            return ResponseEntity.ok("Player " + playerId + " joined room by code " + code);
        } else {
            return ResponseEntity.badRequest().body("Failed to join room by code, it might be full.");
        }
    }

    @GetMapping
    public ResponseEntity<List<RoomInfo>> listAllRooms() {
        List<RoomInfo> result = roomManager.getAllRooms().stream()
                .map(room -> new RoomInfo(room.getRoomId(), room.getJoinCode(), room.getPlayers(),
                        room.getName(), room.getGameType(), room.getMaxPlayers(), room.isPublic(), room.getOwnerLogin(),
                        room.getEntryFee()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get/{roomId}")
    public ResponseEntity<RoomInfo> getRoomById(@PathVariable String roomId) {
        log.info("Request to get room by ID: {}", roomId);
        return roomManager.getRoomById(roomId)
                .map(room -> new RoomInfo(room.getRoomId(), room.getJoinCode(), room.getPlayers(),
                        room.getName(), room.getGameType(), room.getMaxPlayers(), room.isPublic(), room.getOwnerLogin(),
                        room.getEntryFee()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
