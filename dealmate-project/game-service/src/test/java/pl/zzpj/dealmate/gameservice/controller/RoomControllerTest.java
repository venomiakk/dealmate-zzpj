package pl.zzpj.dealmate.gameservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RoomControllerTest {

    private RoomManager roomManager;
    private UserServiceClient userServiceClient;
    private RoomController roomController;

    @BeforeEach
    void setup() {
        roomManager = mock(RoomManager.class);
        userServiceClient = mock(UserServiceClient.class);
        roomController = new RoomController(roomManager, userServiceClient);
    }

    @Test
    void shouldCreateRoomSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("player1");

        CreateRoomRequest request = new CreateRoomRequest("player1", "TestRoom", EGameType.BLACKJACK, 4, true, 100L);
        UserDetailsDto user = new UserDetailsDto(1L, "player1", "test@test.com", "First", "Last", "PL", 1000L, LocalDate.now());
        when(userServiceClient.getUserByUsername("player1")).thenReturn(user);

        GameRoom gameRoom = mock(GameRoom.class);
        when(gameRoom.getRoomId()).thenReturn("room1");
        when(gameRoom.getJoinCode()).thenReturn("ABC123");
        when(gameRoom.getPlayers()).thenReturn(Map.of());
        when(gameRoom.getName()).thenReturn("TestRoom");
        when(gameRoom.getGameType()).thenReturn("BLACKJACK");
        when(gameRoom.getMaxPlayers()).thenReturn(4);
        when(gameRoom.isPublic()).thenReturn(true);
        when(gameRoom.getOwnerLogin()).thenReturn("player1");
        when(gameRoom.getEntryFee()).thenReturn(100.0);

        when(roomManager.createRoom(request)).thenReturn(gameRoom);

        ResponseEntity<?> response = roomController.createRoom(authentication, request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void shouldReturnNotFoundForJoiningNonExistingRoom() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("player1");
        when(roomManager.getRoomById("room1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = roomController.joinRoom("room1", auth);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
