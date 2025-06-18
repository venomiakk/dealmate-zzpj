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
import java.util.Set;

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

    //TODO: Move this kind of validation to a service layer
    // Then handle it with global exception handler
    // Remember to change in tests later
    // Later, when user service will be ready, we can set user credits to 0 if they are null
    //* Credits should be updated after game is finished, not when room is created or joined

    @PostMapping("/create")
    public ResponseEntity<?> createRoom(Authentication auth, @RequestBody CreateRoomRequest request) {
        String playerId = auth.getName();

        UserDetailsDto user = userServiceClient.getUserByUsername(playerId);
        if (user == null) {
            log.error("User not found while creating room: {}", playerId);
            return ResponseEntity.badRequest().body("User not found");
        }
        if (user.credits() == null && request.entryFee() > 0) {
            log.error("User {} has no credits to create room", playerId);
            return ResponseEntity.badRequest().body("User has no credits");
        }
        if (user.credits() != null && user.credits() < request.entryFee()) {
            log.error("User {} does not have enough credits to create a room", playerId);
            return ResponseEntity.badRequest().body("Not enough credits to create a room");
        }
        log.info("Creating room: {}", request);
        GameRoom room = roomManager.createRoom(request);
        room.join(playerId);

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
        if (user.credits() == null  && room.getEntryFee() > 0) {
            log.error("User {} has no credits to join the room", playerId);
            return ResponseEntity.badRequest().body("User has no credits");
        }
        if (user.credits() != null && user.credits() < room.getEntryFee()) {
            log.error("User {} does not have enough credits to join a room", playerId);
            return ResponseEntity.badRequest().body("Not enough credits to join a room");
        }
        return roomManager.getRoomById(roomId)
                .map(m -> {
                    m.join(playerId);
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
        if (user.credits() == null && room.getEntryFee() > 0) {
            log.error("User {} has no credits to join the room by code", playerId);
            return ResponseEntity.badRequest().body("User has no credits");
        }
        if (user.credits() != null && user.credits() < room.getEntryFee()) {
            log.error("User {} does not have enough credits to join a room by code", playerId);
            return ResponseEntity.badRequest().body("Not enough credits to join a room by code");
        }
        return roomManager.getRoomByJoinCode(code)
                .map(rm -> {
                    rm.join(playerId);
                    return ResponseEntity.ok("Player " + playerId + " joined room by code " + code);
                }).orElse(ResponseEntity.notFound().build());
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


