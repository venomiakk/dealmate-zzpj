package pl.zzpj.dealmate.gameservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.time.LocalDate;
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

    @MockitoBean
    private UserServiceClient userServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "player1")
    void shouldCreateRoom() throws Exception {
        // given
        CreateRoomRequest request = new CreateRoomRequest(
                "ownerLogin",
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                4,
                true,
                0L);
        GameRoom mockRoom = mock(GameRoom.class);
        when(mockRoom.getRoomId()).thenReturn("room123");
        when(mockRoom.getJoinCode()).thenReturn("abc123");
        when(mockRoom.getPlayers()).thenReturn(Set.of("player1"));
        when(mockRoom.getName()).thenReturn("Test Room");
        when(mockRoom.getGameType()).thenReturn(String.valueOf(EGameType.TEXAS_HOLDEM));
        when(mockRoom.getMaxPlayers()).thenReturn(4);
        when(mockRoom.isPublic()).thenReturn(true);
        when(userServiceClient.getUserByUsername("player1"))
                .thenReturn(new UserDetailsDto(1L, "player1", "","","",
                        "", 1000L, LocalDate.now())); // 100 kredytów, wystarczy na test

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
        when(userServiceClient.getUserByUsername("playerX"))
                .thenReturn(new UserDetailsDto(1L, "playerX", "","","",
                        "", 1000L, LocalDate.now()));
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));
        when(mockRoom.join("playerX")).thenReturn(true);

        mockMvc.perform(post("/game/room123/join")
                        .with(csrf()))
                .andExpect(status().isOk());

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
        when(userServiceClient.getUserByUsername("playerY"))
                .thenReturn(new UserDetailsDto(1L, "playerY", "","","",
                        "", 1000L, LocalDate.now()));
        when(roomManager.getRoomByJoinCode("code123")).thenReturn(Optional.of(mockRoom));
        when(mockRoom.join("playerY")).thenReturn(true);
        mockMvc.perform(post("/game/join-code/code123")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(mockRoom).join("playerY");
    }

    @Test
    @WithMockUser(username = "playerY")
    void shouldThrowWhenNonExistentUserJoinsByCode() throws Exception {
        when(userServiceClient.getUserByUsername("playerY")).thenReturn(null);
        when(roomManager.getRoomByJoinCode("code123")).thenReturn(Optional.of(mock(GameRoom.class)));

        mockMvc.perform(post("/game/join-code/code123")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "playerY")
    void shouldThrowWhenUserHasNullCreditsWhileJoiningByCode() throws Exception {
        GameRoom room = mock(GameRoom.class);
        when(room.getEntryFee()).thenReturn((double) 100L); // Wymagane 100 kredytów do wejścia
        when(userServiceClient.getUserByUsername("playerY"))
                .thenReturn(new UserDetailsDto(1L, "playerY", "","","",
                        "", null, LocalDate.now())); // Null kredytów, nie wystarczy na test
        when(roomManager.getRoomByJoinCode("code123")).thenReturn(Optional.of(room));

        mockMvc.perform(post("/game/join-code/code123")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "playerY")
    void shouldThrowWhenUserHasInsufficientCreditsWhileJoiningByCode() throws Exception {
        GameRoom room = mock(GameRoom.class);
        when(room.getEntryFee()).thenReturn((double) 100L); // Wymagane 100 kredytów do wejścia
        when(userServiceClient.getUserByUsername("playerY"))
                .thenReturn(new UserDetailsDto(1L, "playerY", "","","",
                        "", 50L, LocalDate.now())); // 50 kredytów, nie wystarczy na test
        when(roomManager.getRoomByJoinCode("code123")).thenReturn(Optional.of(room));

        mockMvc.perform(post("/game/join-code/code123")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not enough credits to join a room by code"));
    }

    @Test
    @WithMockUser(username = "playerY")
    void shouldThrowWhenNonExistentRoomJoinsByCode() throws Exception {
        when(roomManager.getRoomByJoinCode("badCode")).thenReturn(Optional.empty());

        mockMvc.perform(post("/game/join-code/badCode")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "playerY")
    void shouldListAllRooms() throws Exception {
        GameRoom mockRoom = mock(GameRoom.class);
        when(mockRoom.getRoomId()).thenReturn("roomX");
        when(mockRoom.getJoinCode()).thenReturn("joinX");
        when(mockRoom.getPlayers()).thenReturn(Set.of("playerA"));
        when(mockRoom.getName()).thenReturn("Test Room");
        when(mockRoom.getGameType()).thenReturn(String.valueOf(EGameType.TEXAS_HOLDEM));
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
        when(mockRoom.leave("playerZ")).thenReturn(true);
        mockMvc.perform(post("/game/room123/leave")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(mockRoom).leave("playerZ");
    }

    @Test
    @WithMockUser(username = "playerZ")
    void shouldReturnRoomById() throws Exception {
        GameRoom mockRoom = mock(GameRoom.class);
        when(mockRoom.getRoomId()).thenReturn("room123");
        when(mockRoom.getJoinCode()).thenReturn("join123");
        when(mockRoom.getPlayers()).thenReturn(Set.of("playerZ"));
        when(mockRoom.getName()).thenReturn("Test Room");
        when(mockRoom.getGameType()).thenReturn(String.valueOf(EGameType.TEXAS_HOLDEM));
        when(mockRoom.getMaxPlayers()).thenReturn(4);
        when(mockRoom.isPublic()).thenReturn(true);
        when(mockRoom.getOwnerLogin()).thenReturn("ownerLogin");

        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));

        mockMvc.perform(get("/game/get/room123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("room123"))
                .andExpect(jsonPath("$.players[0]").value("playerZ"))
                .andExpect(jsonPath("$.ownerLogin").value("ownerLogin"));
    }

    @Test
    @WithMockUser(username = "playerZ")
    void shouldThrowWhenUserNotFoundWhileJoiningRoom() throws Exception {
        when(userServiceClient.getUserByUsername("playerZ")).thenReturn(null);
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mock(GameRoom.class)));

        mockMvc.perform(post("/game/room123/join")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "playerZ")
    void shouldThrowWhenUserHasNullCreditsWhileJoiningRoom() throws Exception {
        GameRoom room = mock(GameRoom.class);
        when(room.getEntryFee()).thenReturn((double) 100L); // Wymagane 100 kredytów do wejścia
        when(userServiceClient.getUserByUsername("playerZ"))
                .thenReturn(new UserDetailsDto(1L, "playerZ", "","","",
                        "", null, LocalDate.now())); // Null kredytów, nie wystarczy na test
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(room));

        mockMvc.perform(post("/game/room123/join")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "playerZ")
    void shouldThrowWhenUserHasInsufficientCreditsWhileJoiningRoom() throws Exception {
        GameRoom room = mock(GameRoom.class);
        when(room.getEntryFee()).thenReturn((double) 100L); // Wymagane 100 kredytów do wejścia
        when(userServiceClient.getUserByUsername("playerZ"))
                .thenReturn(new UserDetailsDto(1L, "playerZ", "","","",
                        "", 50L, LocalDate.now())); // 50 kredytów, nie wystarczy na test
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(room));

        mockMvc.perform(post("/game/room123/join")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not enough credits to join a room"));
    }

    @Test
    @WithMockUser(username = "playerZ")
    void shouldThrowWhenNonExistentUserCreatesRoom() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest(
                "nonExistentUser",
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                4,
                true,
                0L);

        when(userServiceClient.getUserByUsername("nonExistentUser")).thenReturn(null);

        mockMvc.perform(post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "playerZ")
    void shouldThrowWhenUserHasNullCreditsWhileCreatingRoom() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest(
                "playerZ",
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                4,
                true,
                100L); // Wymagane 100 kredytów do stworzenia pokoju

        when(userServiceClient.getUserByUsername("playerZ"))
                .thenReturn(new UserDetailsDto(1L, "playerZ", "","","",
                        "", null, LocalDate.now())); // Null kredytów, nie wystarczy na test

        mockMvc.perform(post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "playerZ")
    void shouldThrowWhenUserHasInsufficientCreditsWhileCreatingRoom() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest(
                "playerZ",
                "Test Room",
                EGameType.TEXAS_HOLDEM,
                4,
                true,
                100L); // Wymagane 100 kredytów do stworzenia pokoju

        when(userServiceClient.getUserByUsername("playerZ"))
                .thenReturn(new UserDetailsDto(1L, "playerZ", "","","",
                        "", 50L, LocalDate.now())); // 50 kredytów, nie wystarczy na test

        mockMvc.perform(post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not enough credits to create a room"));
    }


}