package pl.zzpj.dealmate.gameservice.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;

import java.util.concurrent.BlockingQueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRoomTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;


    @Test
    void shouldJoinRoomSuccessfully() {
        // Given
        CreateRoomRequest request = new CreateRoomRequest(
                "ownerLogin",
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                4,
                true);
        GameRoom gameRoom = new GameRoom(messagingTemplate, request);
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
                true);
        GameRoom gameRoom = new GameRoom(messagingTemplate, request);
        String playerId = "player1";
        gameRoom.join(playerId);

        // When
        gameRoom.leave(playerId);

        // Then
        assertThat(gameRoom.getPlayers()).doesNotContain(playerId);
    }

    @Test
    void shouldCreateRoomWithDefaultNameIfNotProvided() {
        // Given
        CreateRoomRequest request = new CreateRoomRequest(
                "ownerLogin",
                "",
                EGameType.TEXAS_HOLDEM,
                4,
                true);
        GameRoom gameRoom = new GameRoom(messagingTemplate, request);

        // Then
        assertThat(gameRoom.getName()).startsWith("Room ");
        assertThat(gameRoom.getName()).contains(gameRoom.getRoomId().substring(0, 8));
    }

    @Test
    void shouldSendEventOverWebSocketWhenEventIsAdded() throws Exception {
        // given
        CreateRoomRequest request = new CreateRoomRequest(
                "ownerLogin",
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                4,
                true);

        // subclass GameRoom to override thread startup behavior
        class TestableGameRoom extends GameRoom {
            public TestableGameRoom(SimpMessagingTemplate messagingTemplate, CreateRoomRequest request) {
                super(messagingTemplate, request, false); // pass "false" to not start thread
            }

            public BlockingQueue<String> getEventQueue() {
                return super.getEvents(); // access the protected events queue
            }
        }

        // modified GameRoom with manual thread start
        TestableGameRoom testGameRoom = new TestableGameRoom(messagingTemplate, request);

        // create thread manually
        Thread thread = new Thread(testGameRoom);
        thread.start();

        // when
        testGameRoom.getEventQueue().put("JOIN:player1");

        // allow time to process
        Thread.sleep(100);

        // stop thread
        thread.interrupt();
        thread.join();

        // then
        verify(messagingTemplate).convertAndSend(startsWith("/topic/room/"), eq("JOIN:player1"));
    }
}