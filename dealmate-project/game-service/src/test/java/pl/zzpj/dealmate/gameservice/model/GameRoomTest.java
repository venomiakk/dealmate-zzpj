package pl.zzpj.dealmate.gameservice.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRoomTest {

    @Mock
    private ChatServiceClient chatServiceClient;

    @Mock
    private RoomManager roomManager;


    @Test
    void shouldJoinRoomSuccessfully() {
        // Given
        CreateRoomRequest request = new CreateRoomRequest(
                "ownerLogin",
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                4,
                true,
                null);
        GameRoom gameRoom = new GameRoom(request, roomManager, chatServiceClient);
        String playerId = "player1";

        // When
        gameRoom.join(playerId);

        // Then
        assertThat(gameRoom.getPlayers()).contains(playerId);
    }

    @Test
    void shouldLeaveRoomSuccessfully() {
        // Given
        CreateRoomRequest request = new CreateRoomRequest(
                "ownerLogin",
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                4,
                true,
                null);
        GameRoom gameRoom = new GameRoom(request, roomManager, chatServiceClient);
        String playerId = "player1";
        gameRoom.join(playerId);

        // When
        gameRoom.leave(playerId);

        // Then
        assertThat(gameRoom.getPlayers()).doesNotContain(playerId);
    }

    //@Test
    //void shouldCreateRoomWithDefaultNameIfNotProvided() {
    //    // Given
    //    CreateRoomRequest request = new CreateRoomRequest(
    //            "ownerLogin",
    //            "",
    //            EGameType.TEXAS_HOLDEM,
    //            4,
    //            true,
    //            null);
    //    GameRoom gameRoom = new GameRoom(request, roomManager, chatServiceClient);
    //
    //    // Then
    //    assertThat(gameRoom.getName()).startsWith("Room ");
    //    assertThat(gameRoom.getName()).contains(gameRoom.getRoomId().substring(0, 8));
    //}

}