package pl.zzpj.dealmate.chatservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.zzpj.dealmate.chatservice.dto.ChatMessageDto;
import pl.zzpj.dealmate.chatservice.dto.RoomInfoUpdateDto;
import pl.zzpj.dealmate.chatservice.dto.RoomStateUpdateDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Adnotacja do testowania warstwy web, skupiająca się tylko na SystemController
@WebMvcTest(SystemController.class)
class SystemControllerTest {

    // Wstrzykuje w pełni skonfigurowany MockMvc do wykonywania żądań HTTP
    @Autowired
    private MockMvc mockMvc;

    // Służy do konwersji obiektów Java na JSON
    @Autowired
    private ObjectMapper objectMapper;

    // Tworzy mocka dla SimpMessagingTemplate. Zastępuje prawdziwy bean w kontekście testowym.
    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @WithMockUser // Symuluje uwierzytelnionego użytkownika, aby przejść przez zabezpieczenia
    void handleRoomStateUpdate_whenDtoIsFull_shouldSendTwoWebSocketMessages() throws Exception {
        // GIVEN
        String roomId = "test-room-1";
        Set<String> players = Set.of("player1", "player2");
        String systemMessage = "Player 2 has joined";
        RoomStateUpdateDto updateDto = new RoomStateUpdateDto(roomId, players, systemMessage);

        // Używamy ArgumentCaptor, aby "złapać" obiekty wysyłane do mocka i sprawdzić ich zawartość
        ArgumentCaptor<ChatMessageDto> chatMessageCaptor = ArgumentCaptor.forClass(ChatMessageDto.class);
        ArgumentCaptor<RoomInfoUpdateDto> roomInfoCaptor = ArgumentCaptor.forClass(RoomInfoUpdateDto.class);

        // WHEN
        mockMvc.perform(post("/api/chat/system/update")
                        .with(csrf()) // Dodajemy token CSRF (wymagany domyślnie w testach MockMvc)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                // THEN - HTTP Response
                .andExpect(status().isOk());

        // THEN - Mock Verification
        // Weryfikujemy, czy metoda convertAndSend została wywołana dokładnie 1 raz dla tematu czatu
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/" + roomId + "/chat"), chatMessageCaptor.capture());
        // Weryfikujemy, czy metoda convertAndSend została wywołana dokładnie 1 raz dla tematu graczy
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/" + roomId + "/players"), roomInfoCaptor.capture());

        // Sprawdzamy zawartość przechwyconych obiektów
        ChatMessageDto sentChatMessage = chatMessageCaptor.getValue();
        assertThat(sentChatMessage.getSender()).isEqualTo("System");
        assertThat(sentChatMessage.getContent()).isEqualTo(systemMessage);

        RoomInfoUpdateDto sentRoomInfo = roomInfoCaptor.getValue();
        assertThat(sentRoomInfo.players()).isEqualTo(players);
    }

    @Test
    @WithMockUser
    void handleRoomStateUpdate_whenOnlyMessage_shouldSendOneChatMessage() throws Exception {
        // GIVEN
        String roomId = "test-room-2";
        String systemMessage = "Game started";
        RoomStateUpdateDto updateDto = new RoomStateUpdateDto(roomId, null, systemMessage); // brak listy graczy

        // WHEN
        mockMvc.perform(post("/api/chat/system/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                // THEN - HTTP Response
                .andExpect(status().isOk());

        // THEN - Mock Verification
        // Sprawdzamy, czy wywołano wysyłkę na temat czatu
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/room/" + roomId + "/chat"), any(ChatMessageDto.class));
        // Sprawdzamy, czy NIE wywołano wysyłki na temat graczy
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/" + roomId + "/players"), any(RoomInfoUpdateDto.class));
    }

    @Test
    @WithMockUser
    void handleRoomStateUpdate_whenDtoIsEmpty_shouldNotSendAnyMessages() throws Exception {
        // GIVEN
        String roomId = "test-room-3";
        RoomStateUpdateDto updateDto = new RoomStateUpdateDto(roomId, null, "  "); // Pusta wiadomość

        // WHEN
        mockMvc.perform(post("/api/chat/system/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                // THEN - HTTP Response
                .andExpect(status().isOk());

        // THEN - Mock Verification
        // Sprawdzamy, czy metoda convertAndSend nie została wywołana ANI RAZU
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    @WithMockUser
    void testEndpoint_shouldReturnSuccessMessage() throws Exception {
        // GIVEN
        String expectedMessage = "Chat service is running!";

        // WHEN & THEN
        mockMvc.perform(get("/api/chat/test")
                        .with(csrf())) // GET też może wymagać CSRF w testach
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));
    }
}