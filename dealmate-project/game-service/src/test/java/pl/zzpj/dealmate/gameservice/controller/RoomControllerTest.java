package pl.zzpj.dealmate.gameservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomManager roomManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "player1")
    void shouldCreateRoom() throws Exception {
        // given
        CreateRoomRequest request = new CreateRoomRequest("Test Room", EGameType.TEXAS_HOLDEM, 4, true);
        GameRoom mockRoom = mock(GameRoom.class);
        when(mockRoom.getRoomId()).thenReturn("room123");
        when(mockRoom.getJoinCode()).thenReturn("abc123");
        when(mockRoom.getPlayers()).thenReturn(Set.of("player1"));
        when(mockRoom.getName()).thenReturn("Test Room");
        when(mockRoom.getGameType()).thenReturn(EGameType.TEXAS_HOLDEM);
        when(mockRoom.getMaxPlayers()).thenReturn(4);
        when(mockRoom.isPublic()).thenReturn(true);

        when(roomManager.createRoom(any(CreateRoomRequest.class))).thenReturn(mockRoom);

        // when/then
        mockMvc.perform(post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("room123"))
                .andExpect(jsonPath("$.players[0]").value("player1"));
    }

    @Test
    @WithMockUser(username = "playerX")
    void shouldJoinRoom() throws Exception {
        GameRoom mockRoom = mock(GameRoom.class);
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));

        mockMvc.perform(post("/game/room123/join")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Player playerX joined room room123"));

        verify(mockRoom).join("playerX");
    }

    @Test
    @WithMockUser(username = "playerX")
    void shouldReturnNotFoundWhenJoiningNonexistentRoom() throws Exception {
        when(roomManager.getRoomById("badId")).thenReturn(Optional.empty());

        mockMvc.perform(post("/game/badId/join")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "playerY")
    void shouldJoinByCode() throws Exception {
        GameRoom mockRoom = mock(GameRoom.class);
        when(roomManager.getRoomByJoinCode("code123")).thenReturn(Optional.of(mockRoom));

        mockMvc.perform(post("/game/join-code/code123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Player playerY joined room by code code123"));

        verify(mockRoom).join("playerY");
    }

    @Test
    @WithMockUser(username = "playerY")
    void shouldListAllRooms() throws Exception {
        GameRoom mockRoom = mock(GameRoom.class);
        when(mockRoom.getRoomId()).thenReturn("roomX");
        when(mockRoom.getJoinCode()).thenReturn("joinX");
        when(mockRoom.getPlayers()).thenReturn(Set.of("playerA"));
        when(mockRoom.getName()).thenReturn("Test Room");
        when(mockRoom.getGameType()).thenReturn(EGameType.TEXAS_HOLDEM);
        when(mockRoom.getMaxPlayers()).thenReturn(4);
        when(mockRoom.isPublic()).thenReturn(true);

        when(roomManager.getAllRooms()).thenReturn(List.of(mockRoom));

        mockMvc.perform(get("/game")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId").value("roomX"))
                .andExpect(jsonPath("$[0].players[0]").value("playerA"));
    }

    @Test
    @WithMockUser(username = "playerZ")
    void shouldLeaveRoom() throws Exception {
        GameRoom mockRoom = mock(GameRoom.class);
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));

        mockMvc.perform(post("/game/room123/leave")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Player playerZ left room room123"));

        verify(mockRoom).leave("playerZ");
    }
}