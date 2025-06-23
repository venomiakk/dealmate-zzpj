package pl.zzpj.dealmate.gameservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.PlayerDto;
import pl.zzpj.dealmate.gameservice.dto.RoomInfo;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomManager roomManager;
    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private Authentication authentication;
    @Mock
    private GameRoom gameRoom;

    @InjectMocks
    private RoomController roomController;

    private UserDetailsDto userDetails;
    private PlayerDto playerDto;

    @BeforeEach
    void setup() {
        lenient().when(authentication.getName()).thenReturn("player1");
        userDetails = new UserDetailsDto(1L, "player1", "test@test.com", "First", "Last", "PL", 1000L, LocalDate.now());
        playerDto = new PlayerDto("player1", 1000L);
    }

    @Test
    void createRoom_shouldSucceed_whenUserHasEnoughCredits() {
        // Given
        CreateRoomRequest request = new CreateRoomRequest("player1", "TestRoom", EGameType.BLACKJACK, 4, true, 100L);
        when(userServiceClient.getUserByUsername("player1")).thenReturn(userDetails);
        when(roomManager.createRoom(any(CreateRoomRequest.class))).thenReturn(gameRoom);
        when(gameRoom.getPlayers()).thenReturn(Map.of("player1", playerDto));

        // When
        ResponseEntity<?> response = roomController.createRoom(authentication, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(RoomInfo.class);
        verify(roomManager).createRoom(request);
        verify(gameRoom).join("player1");
    }

    @Test
    void createRoom_shouldFail_whenUserHasNotEnoughCredits() {
        // Given
        CreateRoomRequest request = new CreateRoomRequest("player1", "TestRoom", EGameType.BLACKJACK, 4, true, 100L);
        UserDetailsDto poorUser = new UserDetailsDto(1L, "player1", "test@test.com", "First", "Last", "PL", 50L, LocalDate.now());
        when(userServiceClient.getUserByUsername("player1")).thenReturn(poorUser);

        // When
        ResponseEntity<?> response = roomController.createRoom(authentication, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Not enough credits to create a room");
    }

    @Test
    void joinRoom_shouldSucceed_whenRoomExistsAndPlayerCanJoin() {
        // Given
        when(roomManager.getRoomById("room1")).thenReturn(Optional.of(gameRoom));
        when(userServiceClient.getUserByUsername("player1")).thenReturn(userDetails);
        when(gameRoom.getEntryFee()).thenReturn(100.0);
        when(gameRoom.join("player1")).thenReturn(true);

        // When
        ResponseEntity<?> response = roomController.joinRoom("room1", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Player player1 joined room room1");
    }

    @Test
    void joinRoom_shouldFail_whenRoomIsFull() {
        // Given
        when(roomManager.getRoomById("room1")).thenReturn(Optional.of(gameRoom));
        when(userServiceClient.getUserByUsername("player1")).thenReturn(userDetails);
        when(gameRoom.getEntryFee()).thenReturn(0.0);
        when(gameRoom.join("player1")).thenReturn(false);

        // When
        ResponseEntity<?> response = roomController.joinRoom("room1", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).asString().contains("Failed to join room");
    }

    @Test
    void joinRoom_shouldReturnNotFound_whenRoomDoesNotExist() {
        // Given
        when(roomManager.getRoomById("non-existing-room")).thenReturn(Optional.empty());

        // When
        ResponseEntity<?> response = roomController.joinRoom("non-existing-room", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void startGame_shouldSucceed_whenPlayerIsOwner() {
        // Given
        // POPRAWKA: getPlayers() zwraca mapę, a nie set. Sprawdzamy, czy nie jest pusta.
        when(gameRoom.getPlayers()).thenReturn(Map.of("player1", playerDto));
        when(gameRoom.getOwnerLogin()).thenReturn("player1");
        when(roomManager.getRoomById("room1")).thenReturn(Optional.of(gameRoom));

        // When
        ResponseEntity<?> response = roomController.startGame("room1", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(gameRoom).signalGameStart();
    }

    @Test
    void startGame_shouldReturnForbidden_whenPlayerIsNotOwner() {
        // Given
        when(gameRoom.getOwnerLogin()).thenReturn("another_owner");
        when(roomManager.getRoomById("room1")).thenReturn(Optional.of(gameRoom));

        // When
        ResponseEntity<?> response = roomController.startGame("room1", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(gameRoom, never()).signalGameStart();
    }

    @Test
    void startGame_shouldFail_whenRoomIsEmpty() {
        // Given
        when(gameRoom.getPlayers()).thenReturn(Collections.emptyMap());
        when(gameRoom.getOwnerLogin()).thenReturn("player1");
        when(roomManager.getRoomById("room1")).thenReturn(Optional.of(gameRoom));

        // When
        ResponseEntity<?> response = roomController.startGame("room1", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Cannot start an empty game.");
    }

    @Test
    void leaveRoom_shouldSucceed_whenPlayerIsInRoom() {
        // Given
        when(gameRoom.leave("player1")).thenReturn(true);
        when(roomManager.getRoomById("room1")).thenReturn(Optional.of(gameRoom));

        // When
        ResponseEntity<?> response = roomController.leaveRoom("room1", authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void joinByCode_shouldSucceed_whenCodeIsValid() {
        // Given
        String joinCode = "ABCDEF";
        when(roomManager.getRoomByJoinCode(joinCode)).thenReturn(Optional.of(gameRoom));
        when(userServiceClient.getUserByUsername("player1")).thenReturn(userDetails);
        when(gameRoom.getEntryFee()).thenReturn(0.0);
        when(gameRoom.join("player1")).thenReturn(true);

        // When
        ResponseEntity<?> response = roomController.joinByCode(joinCode, authentication);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void listAllRooms_shouldReturnAllRooms() {
        // Given
        GameRoom anotherRoom = mock(GameRoom.class);
        when(gameRoom.getPlayers()).thenReturn(Map.of("player1", playerDto));
        when(anotherRoom.getPlayers()).thenReturn(Map.of("player2", new PlayerDto("player2", 500L)));
        when(roomManager.getAllRooms()).thenReturn(List.of(gameRoom, anotherRoom));

        // When
        ResponseEntity<List<RoomInfo>> response = roomController.listAllRooms();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getRoomById_shouldReturnRoomInfo_whenRoomExists() {
        // Given
        when(gameRoom.getRoomId()).thenReturn("room1");
        when(gameRoom.getPlayers()).thenReturn(Map.of("player1", playerDto));
        when(roomManager.getRoomById("room1")).thenReturn(Optional.of(gameRoom));

        // When
        ResponseEntity<RoomInfo> response = roomController.getRoomById("room1");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().roomId()).isEqualTo("room1");
    }

    @Test
    void getRoomById_shouldReturnNotFound_whenRoomDoesNotExist() {
        // Given
        when(roomManager.getRoomById("non-existing-room")).thenReturn(Optional.empty());

        // When
        ResponseEntity<RoomInfo> response = roomController.getRoomById("non-existing-room");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
