package pl.zzpj.dealmate.gameservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.model.GameRoom;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class RoomManagerTest {

    @Test
    void testCreateRoom() {
        // Given
         SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
         RoomManager roomManager = new RoomManager(messagingTemplate);

        // When
         GameRoom room = roomManager.createRoom();

         // Then
         assertThat(room).isNotNull();
         assertThat(room.getRoomId()).isNotEmpty();
         assertThat(room.getJoinCode()).isNotEmpty();
         assertThat(room.getPlayers().isEmpty());

         // Verify that the room is added to the manager's collection
        assertThat(roomManager.getAllRooms().contains(room));
    }

    @Test
    void testGetRoomById() {
        // Given
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RoomManager roomManager = new RoomManager(messagingTemplate);
        GameRoom room = roomManager.createRoom();

        // When
        var retrievedRoom = roomManager.getRoomById(room.getRoomId());

        // Then
        assertThat(retrievedRoom).isPresent();
        assertThat(retrievedRoom.get()).isEqualTo(room);
    }

    @Test
    void testGetRoomByJoinCode() {
        // Given
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RoomManager roomManager = new RoomManager(messagingTemplate);
        GameRoom room = roomManager.createRoom();

        // When
        var retrievedRoom = roomManager.getRoomByJoinCode(room.getJoinCode());

        // Then
        assertThat(retrievedRoom).isPresent();
        assertThat(retrievedRoom.get()).isEqualTo(room);
    }

    @Test
    void testGetAllRooms() {
        // Given
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        RoomManager roomManager = new RoomManager(messagingTemplate);
        GameRoom room1 = roomManager.createRoom();
        GameRoom room2 = roomManager.createRoom();

        // When
        var allRooms = roomManager.getAllRooms();

        // Then
        assertThat(allRooms.size()).isEqualTo(2);
        assertThat(allRooms.contains(room1));
        assertThat(allRooms.contains(room2));
    }
}
