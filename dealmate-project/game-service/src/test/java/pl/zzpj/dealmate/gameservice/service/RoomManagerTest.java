package pl.zzpj.dealmate.gameservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameRoom;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RoomManagerTest {

    @Mock private ChatServiceClient chatServiceClient;
    @Mock private DeckServiceClient deckServiceClient;
    @Mock private UserServiceClient userServiceClient;
    @Mock private GameHistoryService gameHistoryService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RoomManager roomManager;

    private CreateRoomRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateRoomRequest(
                "ownerLogin", "Test Room", EGameType.BLACKJACK, 4, true, 0L);
    }

    @Test
    void shouldCreateAndStoreRoom() {
        // When
        GameRoom room = roomManager.createRoom(request);

        // Then
        assertThat(room).isNotNull();
        assertThat(room.getName()).isEqualTo("Test Room");
        assertThat(room.getOwnerLogin()).isEqualTo("ownerLogin");

        Optional<GameRoom> retrievedRoom = roomManager.getRoomById(room.getRoomId());
        assertThat(retrievedRoom).isPresent();
        assertThat(retrievedRoom.get()).isEqualTo(room);
    }

    @Test
    void shouldReturnRoomByIdWhenExists() {
        // Given
        GameRoom room = roomManager.createRoom(request);

        // When
        Optional<GameRoom> foundRoom = roomManager.getRoomById(room.getRoomId());

        // Then
        assertThat(foundRoom).isPresent().contains(room);
    }

    @Test
    void shouldReturnEmptyOptionalForNonExistentRoomId() {
        // When
        Optional<GameRoom> notFoundRoom = roomManager.getRoomById("non-existent-id");

        // Then
        assertThat(notFoundRoom).isNotPresent();
    }

    @Test
    void shouldReturnRoomByJoinCode() {
        // Given
        GameRoom room1 = roomManager.createRoom(request);
        // Tworzymy drugi pokój, aby upewnić się, że znajdujemy właściwy
        CreateRoomRequest request2 = new CreateRoomRequest(
                "owner2", "Room 2", EGameType.BLACKJACK, 2, true, 10L);
        roomManager.createRoom(request2);

        // When
        Optional<GameRoom> foundRoom = roomManager.getRoomByJoinCode(room1.getJoinCode());

        // Then
        assertThat(foundRoom).isPresent();
        assertThat(foundRoom.get().getRoomId()).isEqualTo(room1.getRoomId());
    }

    @Test
    void shouldRemoveRoomById() {
        // Given
        GameRoom room = roomManager.createRoom(request);
        String roomId = room.getRoomId();
        assertThat(roomManager.getRoomById(roomId)).isPresent();

        // When
        roomManager.removeRoom(roomId);

        // Then
        assertThat(roomManager.getRoomById(roomId)).isNotPresent();
    }

    @Test
    void shouldReturnAllCreatedRooms() {
        // Given
        GameRoom room1 = roomManager.createRoom(request);
        CreateRoomRequest request2 = new CreateRoomRequest(
                "owner2", "Room 2", EGameType.BLACKJACK, 2, true, 10L);
        GameRoom room2 = roomManager.createRoom(request2);

        // When
        var allRooms = roomManager.getAllRooms();

        // Then
        assertThat(allRooms).hasSize(2).containsExactlyInAnyOrder(room1, room2);
    }
}