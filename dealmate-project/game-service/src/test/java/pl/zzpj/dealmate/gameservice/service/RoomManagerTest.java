package pl.zzpj.dealmate.gameservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameRoom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class RoomManagerTest {

    @Test
    void testCreateRoom() {
        // Given
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RoomManager roomManager = new RoomManager(messagingTemplate);
         CreateRoomRequest request = new CreateRoomRequest(
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                2,
                true);
        // When
        GameRoom room = roomManager.createRoom(request);

        // Then
        assertThat(room).isNotNull();
        assertThat(room.getRoomId()).isNotEmpty();
        assertThat(room.getJoinCode()).isNotEmpty();
        assertThat(room.getName()).isEqualTo("Test Room");
        assertThat(room.getGameType()).isEqualTo(EGameType.TEXAS_HOLDEM);
        assertThat(room.getMaxPlayers()).isEqualTo(2);
        assertThat(room.isPublic()).isTrue();
        assertThat(room.getPlayers().size()).isEqualTo(0); // Initially no players in the room
        // Verify that the room is added to the manager's collection
        assertThat(roomManager.getRoomById(room.getRoomId())).isPresent();
    }

    @Test
    void testGetRoomById() {
        // Given
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RoomManager roomManager = new RoomManager(messagingTemplate);
        CreateRoomRequest request = new CreateRoomRequest(
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                2,
                true);
        GameRoom room = roomManager.createRoom(request);

        // When
        var retrievedRoom = roomManager.getRoomById(room.getRoomId());

        // Then
        assertThat(retrievedRoom).isPresent();
        assertThat(retrievedRoom).contains(room);
    }

    @Test
    void testGetRoomByJoinCode() {
        // Given
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RoomManager roomManager = new RoomManager(messagingTemplate);
        CreateRoomRequest request = new CreateRoomRequest(
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                2,
                true);
        GameRoom room = roomManager.createRoom(request);

        // When
        var retrievedRoom = roomManager.getRoomByJoinCode(room.getJoinCode());

        // Then
        assertThat(retrievedRoom).isPresent();
        assertThat(retrievedRoom).contains(room);
    }

    @Test
    void testGetAllRooms() {
        // Given
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RoomManager roomManager = new RoomManager(messagingTemplate);
        CreateRoomRequest request = new CreateRoomRequest(
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                2,
                true);
        GameRoom room1 = roomManager.createRoom(request);
        GameRoom room2 = roomManager.createRoom(request);

        // When
        var allRooms = roomManager.getAllRooms();

        // Then
        assertThat(allRooms.size()).isEqualTo(2);
        assertThat(allRooms.contains(room1)).isTrue();
        assertThat(allRooms.contains(room2)).isTrue();
    }
}
