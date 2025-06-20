package pl.zzpj.dealmate.gameservice.game.blackjack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import pl.zzpj.dealmate.gameservice.client.DeckServiceClient;
import pl.zzpj.dealmate.gameservice.dto.PlayerDto;
import pl.zzpj.dealmate.gameservice.game.dto.CardDto;
import pl.zzpj.dealmate.gameservice.game.dto.DeckDto;
import pl.zzpj.dealmate.gameservice.game.dto.GameStateDto;
import pl.zzpj.dealmate.gameservice.game.dto.PlayerAction;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.model.GameResult;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlackjackGameTest {

    @Mock private GameRoom mockRoom;
    @Mock private DeckServiceClient mockDeckService;
    @Mock private SimpMessagingTemplate mockMessagingTemplate;
    @Mock private GameHistoryService mockGameHistoryService;

    private BlackjackGame blackjackGame;

    // Helper do tworzenia kart
    private CardDto card(String value, String suit) { return new CardDto(value.substring(0, 1) + suit, value, suit, null); }
    private CardDto card(String value) { return card(value, "S"); }
    private CardDto ace() { return card("ACE", "H"); }
    private CardDto king() { return card("KING", "S"); }


    @BeforeEach
    void setUp() {
        lenient().when(mockRoom.getRoomId()).thenReturn("test-room-123");
        lenient().when(mockRoom.getEntryFee()).thenReturn(10.0);
        lenient().when(mockDeckService.createDeck(anyInt())).thenReturn(new DeckDto("string-deck-id", true, 52, 1L));
    }

    // --- Metody pomocnicze do refleksji ---
    private void setField(String fieldName, Object value) throws Exception {
        Field field = BlackjackGame.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(blackjackGame, value);
    }

    // ZMIANA: Ta metoda została przepisana, aby poprawnie modyfikować pola atomowe
    private void setState(GameStatus status, int currentPlayerIndexValue) {
        try {
            Field gameStatusField = BlackjackGame.class.getDeclaredField("gameStatus");
            gameStatusField.setAccessible(true);
            AtomicReference<GameStatus> gameStatusRef = (AtomicReference<GameStatus>) gameStatusField.get(blackjackGame);
            gameStatusRef.set(status);

            Field playerIndexField = BlackjackGame.class.getDeclaredField("currentPlayerIndex");
            playerIndexField.setAccessible(true);
            AtomicInteger playerIndexRef = (AtomicInteger) playerIndexField.get(blackjackGame);
            playerIndexRef.set(currentPlayerIndexValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set state via reflection", e);
        }
    }

    private List<String> invokeDetermineWinners() throws Exception {
        Method method = BlackjackGame.class.getDeclaredMethod("determineWinners");
        method.setAccessible(true);
        return (List<String>) method.invoke(blackjackGame);
    }
    private int invokeCalculateHandValue(List<CardDto> hand) throws Exception {
        Method method = BlackjackGame.class.getDeclaredMethod("calculateHandValue", List.class);
        method.setAccessible(true);
        return (int) method.invoke(blackjackGame, hand);
    }

    private void invokePrivateMethod(String methodName) throws Exception {
        Method method = BlackjackGame.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(blackjackGame);
    }

    @Nested
    @DisplayName("Player Action Tests")
    class HandleActionTests {
        @BeforeEach
        void setupActionTests() throws Exception {
            blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of("player1", "player2"), mockGameHistoryService);
            setField("playerHands", new ConcurrentHashMap<>(Map.of("player1", new ArrayList<>(), "player2", new ArrayList<>())));
            setField("playerStatuses", new ConcurrentHashMap<>(Map.of("player1", PlayerStatus.PLAYING, "player2", PlayerStatus.PLAYING)));
            setState(GameStatus.PLAYER_TURN, 0);
        }

        @Test
        void handlePlayerAction_shouldCallHit_whenActionIsHitAndIsPlayerTurn() {
            when(mockDeckService.drawCards(anyLong(), eq(1))).thenReturn(List.of(card("5")));
            blackjackGame.handlePlayerAction("player1", new PlayerAction.Hit());

            verify(mockDeckService).drawCards(anyLong(), eq(1));
            verify(mockMessagingTemplate, atLeastOnce()).convertAndSend(anyString(), any(GameStateDto.class));
        }

        @Test
        void handlePlayerAction_shouldCallStand_whenActionIsStandAndIsPlayerTurn() {
            blackjackGame.handlePlayerAction("player1", new PlayerAction.Stand());
            verify(mockDeckService, never()).drawCards(anyLong(), anyInt());
            verify(mockMessagingTemplate).convertAndSend(anyString(), any(GameStateDto.class));
        }

        @Test
        void handlePlayerAction_shouldDoNothing_whenItIsNotPlayerTurn() {
            blackjackGame.handlePlayerAction("player2", new PlayerAction.Hit());
            verify(mockDeckService, never()).drawCards(anyLong(), anyInt());
            verify(mockMessagingTemplate, never()).convertAndSend(anyString(), (Object) any());
        }
    }


    @Nested
    @DisplayName("Determine Winners Logic Tests")
    class DetermineWinnersTests {
        private void setHands(List<CardDto> playerHand, List<CardDto> dealerHand) throws Exception {
            setField("playerHands", new ConcurrentHashMap<>(Map.of("player1", new ArrayList<>(playerHand))));
            setField("dealerHand", new ArrayList<>(dealerHand));

            int playerValue = invokeCalculateHandValue(playerHand);
            PlayerStatus status = playerValue > 21 ? PlayerStatus.BUSTED : PlayerStatus.PLAYING;
            if (playerValue == 21 && playerHand.size() == 2) status = PlayerStatus.BLACKJACK;
            setField("playerStatuses", new ConcurrentHashMap<>(Map.of("player1", status)));
        }

        @BeforeEach
        void setupWinnerTests() {
            blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of("player1"), mockGameHistoryService);
            lenient().when(mockRoom.getPlayers()).thenReturn(Map.of("player1", new PlayerDto("player1", 1000L)));
        }

        @Test
        void determineWinners_shouldResultInPlayerWin_whenScoreIsHigher() throws Exception {
            setHands(List.of(card("10"), card("9")), List.of(card("10"), card("8")));
            invokeDetermineWinners();
            ArgumentCaptor<GameResult> resultCaptor = ArgumentCaptor.forClass(GameResult.class);
            verify(mockGameHistoryService).recordGameResults(eq("player1"), resultCaptor.capture(), any(BigDecimal.class));
            assertThat(resultCaptor.getValue()).isEqualTo(GameResult.WIN);
        }

        @Test
        void determineWinners_shouldResultInPlayerLoss_whenScoreIsLower() throws Exception {
            setHands(List.of(card("10"), card("8")), List.of(card("10"), card("9")));
            invokeDetermineWinners();
            ArgumentCaptor<GameResult> resultCaptor = ArgumentCaptor.forClass(GameResult.class);
            verify(mockGameHistoryService).recordGameResults(eq("player1"), resultCaptor.capture(), any(BigDecimal.class));
            assertThat(resultCaptor.getValue()).isEqualTo(GameResult.LOSS);
        }

        @Test
        void determineWinners_shouldResultInPush_whenScoresAreEqual() throws Exception {
            setHands(List.of(card("10"), card("9")), List.of(card("10"), card("9")));
            invokeDetermineWinners();
            ArgumentCaptor<GameResult> resultCaptor = ArgumentCaptor.forClass(GameResult.class);
            verify(mockGameHistoryService).recordGameResults(eq("player1"), resultCaptor.capture(), any(BigDecimal.class));
            assertThat(resultCaptor.getValue()).isEqualTo(GameResult.PUSH);
        }
    }


    @Nested
    @DisplayName("Game Flow and State Tests")
    class GameFlowAndStateTests {
        @Test
        void initializeGame_shouldCreateDeckAndPreparePlayers() throws Exception {
            blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of("p1", "p2"), mockGameHistoryService);
            invokePrivateMethod("initializeGame");
            verify(mockDeckService).createDeck(1);

            Field playerHandsField = BlackjackGame.class.getDeclaredField("playerHands");
            playerHandsField.setAccessible(true);
            Map<String, List<CardDto>> playerHands = (Map<String, List<CardDto>>) playerHandsField.get(blackjackGame);

            assertThat(playerHands).hasSize(2).containsKey("p1").containsKey("p2");
            assertThat(playerHands.get("p1")).isEmpty();
        }

        @Test
        void dealInitialCards_shouldDealTwoCardsToEachPlayerAndDealer() throws Exception {
            blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of("p1", "p2"), mockGameHistoryService);
            invokePrivateMethod("initializeGame");

            when(mockDeckService.drawCards(anyLong(), eq(1)))
                    .thenReturn(List.of(card("2")))
                    .thenReturn(List.of(card("3")))
                    .thenReturn(List.of(card("4")))
                    .thenReturn(List.of(card("5")))
                    .thenReturn(List.of(card("6")))
                    .thenReturn(List.of(card("7")));

            invokePrivateMethod("dealInitialCards");

            verify(mockDeckService, times(6)).drawCards(anyLong(), eq(1));

            Field playerHandsField = BlackjackGame.class.getDeclaredField("playerHands");
            playerHandsField.setAccessible(true);
            Map<String, List<CardDto>> playerHands = (Map<String, List<CardDto>>) playerHandsField.get(blackjackGame);

            assertThat(playerHands.get("p1")).hasSize(2);
            assertThat(playerHands.get("p2")).hasSize(2);
        }

        @Test
        void dealInitialCards_shouldDetectPlayerBlackjack() throws Exception {
            blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of("p1"), mockGameHistoryService);
            invokePrivateMethod("initializeGame");

            when(mockDeckService.drawCards(anyLong(), eq(1)))
                    .thenReturn(List.of(ace()))
                    .thenReturn(List.of(card("5")))
                    .thenReturn(List.of(king()))
                    .thenReturn(List.of(card("6")));

            invokePrivateMethod("dealInitialCards");

            Field playerStatusesField = BlackjackGame.class.getDeclaredField("playerStatuses");
            playerStatusesField.setAccessible(true);
            Map<String, PlayerStatus> playerStatuses = (Map<String, PlayerStatus>) playerStatusesField.get(blackjackGame);

            assertThat(playerStatuses.get("p1")).isEqualTo(PlayerStatus.BLACKJACK);
        }

        @Test
        void hit_shouldSetPlayerToBusted_whenScoreExceeds21() throws Exception {
            blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of("p1"), mockGameHistoryService);
            setHands(List.of(card("10"), card("7")), List.of());
            when(mockDeckService.drawCards(anyLong(), eq(1))).thenReturn(List.of(card("5")));

            invokeHit("p1");

            Field playerStatusesField = BlackjackGame.class.getDeclaredField("playerStatuses");
            playerStatusesField.setAccessible(true);
            Map<String, PlayerStatus> playerStatuses = (Map<String, PlayerStatus>) playerStatusesField.get(blackjackGame);
            assertThat(playerStatuses.get("p1")).isEqualTo(PlayerStatus.BUSTED);
        }

        @Test
        void playDealerTurn_shouldDrawUntil17OrMore() throws Exception {
            blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of(), mockGameHistoryService);
            setField("dealerHand", new ArrayList<>(List.of(card("10"), card("6"))));

            when(mockDeckService.drawCards(anyLong(), eq(1))).thenReturn(List.of(card("8")));

            invokePrivateMethod("playDealerTurn");

            verify(mockDeckService, times(1)).drawCards(anyLong(), eq(1));

            Field dealerHandField = BlackjackGame.class.getDeclaredField("dealerHand");
            dealerHandField.setAccessible(true);
            List<CardDto> dealerHand = (List<CardDto>) dealerHandField.get(blackjackGame);

            assertThat(invokeCalculateHandValue(dealerHand)).isEqualTo(24);
        }

        @Test
        void play_shouldRunFullRoundAndReturnWinners() throws InterruptedException {
            String player1 = "player1";
            blackjackGame = new BlackjackGame(mockRoom, mockDeckService, mockMessagingTemplate, List.of(player1), mockGameHistoryService);
            when(mockRoom.getPlayers()).thenReturn(Map.of(player1, new PlayerDto(player1, 1000L)));

            when(mockDeckService.drawCards(anyLong(), eq(1)))
                    .thenReturn(List.of(card("10", "S")))
                    .thenReturn(List.of(card("7", "S")))
                    .thenReturn(List.of(king()))
                    .thenReturn(List.of(card("10", "H")));

            AtomicReference<List<String>> winners = new AtomicReference<>();

            Thread gameThread = new Thread(() -> winners.set(blackjackGame.play()));
            gameThread.start();

            Thread.sleep(500);

            blackjackGame.handlePlayerAction(player1, new PlayerAction.Stand());

            gameThread.join();

            assertThat(winners.get()).isNotNull().containsExactly(player1);

            ArgumentCaptor<GameResult> resultCaptor = ArgumentCaptor.forClass(GameResult.class);
            verify(mockGameHistoryService).recordGameResults(eq(player1), resultCaptor.capture(), any(BigDecimal.class));
            assertThat(resultCaptor.getValue()).isEqualTo(GameResult.WIN);
        }

        private void setHands(List<CardDto> playerHand, List<CardDto> dealerHand) throws Exception {
            setField("playerHands", new ConcurrentHashMap<>(Map.of("p1", new ArrayList<>(playerHand))));
            setField("dealerHand", new ArrayList<>(dealerHand));
            setField("playerStatuses", new ConcurrentHashMap<>(Map.of("p1", PlayerStatus.PLAYING)));
        }

        private void invokeHit(String playerId) throws Exception {
            Method method = BlackjackGame.class.getDeclaredMethod("hit", String.class);
            method.setAccessible(true);
            method.invoke(blackjackGame, playerId);
        }
    }
}
