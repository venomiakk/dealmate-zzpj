package pl.zzpj.dealmate.gameservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.ChatServiceClient;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.PlayerDto;
import pl.zzpj.dealmate.gameservice.dto.RoomStateUpdateDto;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;
import pl.zzpj.dealmate.gameservice.game.blackjack.BlackjackGame;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRoomTest {

    @Mock private RoomManager roomManager;
    @Mock private UserServiceClient userServiceClient;
    @Mock private ChatServiceClient chatServiceClient;
    @Mock private DeckServiceClient deckServiceClient;
    @Mock private GameHistoryService gameHistoryService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private Thread mockGameThread;
    @Mock private BlackjackGame mockBlackjackGame;

    private GameRoom gameRoom;
    private CreateRoomRequest createRoomRequest;

    @BeforeEach
    void setUp() {
        createRoomRequest = new CreateRoomRequest(
                "owner1", "Test Room", EGameType.BLACKJACK, 4, true, 50L);

        gameRoom = new GameRoom(createRoomRequest, roomManager, userServiceClient, chatServiceClient,
                deckServiceClient, gameHistoryService, messagingTemplate);
    }

    private UserDetailsDto createSampleUser(String username, long credits) {
        return new UserDetailsDto(1L, username, "email@test.com", "First", "Last", "PL", credits, LocalDate.now());
    }

    @Test
    void shouldJoinRoomSuccessfully() {
        // Given
        String playerId = "player1";
        UserDetailsDto userDetails = createSampleUser(playerId, 1000L);
        when(userServiceClient.getUserByUsername(playerId)).thenReturn(userDetails);

        // When
        boolean result = gameRoom.join(playerId);

        // Then
        assertThat(result).isTrue();
        assertThat(gameRoom.getPlayers()).containsKey(playerId);
        assertThat(gameRoom.getPlayers().get(playerId).getCredits()).isEqualTo(1000L);

        // Weryfikacja wysłania aktualizacji o graczach przez WebSocket
        ArgumentCaptor<Map> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/room/" + gameRoom.getRoomId() + "/players"), payloadCaptor.capture());

        Map capturedPayload = payloadCaptor.getValue();
        Collection<PlayerDto> playersInPayload = (Collection<PlayerDto>) capturedPayload.get("players");
        assertThat(playersInPayload).hasSize(1);
        assertThat(playersInPayload.iterator().next().getLogin()).isEqualTo(playerId);

        // Weryfikacja wysłania wiadomości na czat
        verify(chatServiceClient).notifyRoomStateChange(any(RoomStateUpdateDto.class));
    }

    @Test
    void shouldNotJoinWhenRoomIsFull() {
        // Given
        gameRoom = new GameRoom(createRoomRequest, roomManager, userServiceClient, chatServiceClient,
                deckServiceClient, gameHistoryService, messagingTemplate); // Pokój na 4 graczy

        when(userServiceClient.getUserByUsername(anyString())).thenAnswer(inv -> createSampleUser(inv.getArgument(0), 1000L));
        gameRoom.join("p1");
        gameRoom.join("p2");
        gameRoom.join("p3");
        gameRoom.join("p4");

        // When
        boolean result = gameRoom.join("p5");

        // Then
        assertThat(result).isFalse();
        assertThat(gameRoom.getPlayers()).hasSize(4);
    }


    @Test
    void shouldLeaveRoomSuccessfully() {
        // Given
        String playerId = "player1";
        UserDetailsDto userDetails = createSampleUser(playerId, 1000L);
        when(userServiceClient.getUserByUsername(playerId)).thenReturn(userDetails);
        gameRoom.join(playerId);

        // When
        boolean result = gameRoom.leave(playerId);

        // Then
        assertThat(result).isTrue();
        assertThat(gameRoom.getPlayers()).doesNotContainKey(playerId);
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/room/" + gameRoom.getRoomId() + "/players"), any(Map.class)); // Raz przy join, raz przy leave
        verify(chatServiceClient, times(2)).notifyRoomStateChange(any(RoomStateUpdateDto.class));
    }


    @Test
    void shouldAllowJoiningWhenGameIsInProgress() {
        // Given
        // Symulujemy grę w toku, ustawiając 'currentGame'
        gameRoom.setCurrentGame(mockBlackjackGame);

        String newPlayerId = "spectator1";
        UserDetailsDto userDetails = createSampleUser(newPlayerId, 500L);
        when(userServiceClient.getUserByUsername(newPlayerId)).thenReturn(userDetails);

        // When
        boolean result = gameRoom.join(newPlayerId);

        // Then
        assertThat(result).isTrue();
        assertThat(gameRoom.getPlayers()).containsKey(newPlayerId);

        // Sprawdzamy, czy nowy gracz został poinformowany o trybie obserwatora
        verify(mockBlackjackGame).broadcastState(contains("spectating"), any(), any());
    }

    @Test
    void shouldNotifyGameToSkipTurnWhenCurrentPlayerLeaves() {
        // Given
        String currentPlayerId = "player1";
        UserDetailsDto user1 = createSampleUser(currentPlayerId, 1000L);
        UserDetailsDto user2 = createSampleUser("player2", 1000L);
        when(userServiceClient.getUserByUsername(currentPlayerId)).thenReturn(user1);
        when(userServiceClient.getUserByUsername("player2")).thenReturn(user2);

        gameRoom.join(currentPlayerId);
        gameRoom.join("player2");

        gameRoom.setCurrentGame(mockBlackjackGame);
        when(mockBlackjackGame.getCurrentPlayerId()).thenReturn(currentPlayerId);

        // When
        gameRoom.leave(currentPlayerId);

        // Then
        verify(mockBlackjackGame).skipCurrentPlayerTurn();
    }

    @Test
    void shouldNotNotifyGameToSkipTurnWhenAnotherPlayerLeaves() {
        // Given
        String currentPlayerId = "player1";
        String otherPlayerId = "player2";
        UserDetailsDto user1 = createSampleUser(currentPlayerId, 1000L);
        UserDetailsDto user2 = createSampleUser(otherPlayerId, 1000L);
        when(userServiceClient.getUserByUsername(currentPlayerId)).thenReturn(user1);
        when(userServiceClient.getUserByUsername(otherPlayerId)).thenReturn(user2);

        gameRoom.join(currentPlayerId);
        gameRoom.join(otherPlayerId);

        gameRoom.setCurrentGame(mockBlackjackGame);
        when(mockBlackjackGame.getCurrentPlayerId()).thenReturn(currentPlayerId);

        // When
        gameRoom.leave(otherPlayerId);

        // Then
        verify(mockBlackjackGame, never()).skipCurrentPlayerTurn();
    }

    @Test
    void shouldInterruptThreadWhenLastPlayerLeaves() {
        // Given
        String lastPlayerId = "player1";
        UserDetailsDto userDetails = createSampleUser(lastPlayerId, 1000L);
        when(userServiceClient.getUserByUsername(lastPlayerId)).thenReturn(userDetails);

        gameRoom.join(lastPlayerId);
        gameRoom.setGameThread(mockGameThread); // Wstrzykujemy mock wątku

        // When
        gameRoom.leave(lastPlayerId);

        // Then
        assertThat(gameRoom.getPlayers()).isEmpty();
        verify(mockGameThread).interrupt();
    }
}