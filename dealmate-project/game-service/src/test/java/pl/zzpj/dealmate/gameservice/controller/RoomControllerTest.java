package pl.zzpj.dealmate.gameservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
import pl.zzpj.dealmate.gameservice.dto.CreateRoomRequest;
import pl.zzpj.dealmate.gameservice.dto.PlayerDto;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private UserDetailsDto sampleUserDto;
    private GameRoom mockRoom;

    @BeforeEach
    void setUp() {
        // Helper to create a sample user DTO for tests
        sampleUserDto = new UserDetailsDto(1L, "player1", "player1@test.com", "Test", "Player", "PL", 1000L, LocalDate.now());

        // Helper to create a basic mocked GameRoom
        mockRoom = mock(GameRoom.class);
        when(mockRoom.getRoomId()).thenReturn("room123");
        when(mockRoom.getJoinCode()).thenReturn("ABCDEF");
        when(mockRoom.getName()).thenReturn("Test Room");
        when(mockRoom.getGameType()).thenReturn(EGameType.BLACKJACK.name());
        when(mockRoom.getMaxPlayers()).thenReturn(4);
        when(mockRoom.isPublic()).thenReturn(true);
        when(mockRoom.getOwnerLogin()).thenReturn("player1");
        when(mockRoom.getEntryFee()).thenReturn(50.0);
    }

    @Test
    @WithMockUser(username = "player1")
    void shouldCreateRoom_whenUserHasEnoughCredits() throws Exception {
        // given
        CreateRoomRequest request = new CreateRoomRequest("player1", "Test Room", EGameType.BLACKJACK, 4, true, 50L);

        PlayerDto playerDto = new PlayerDto("player1", 1000L);
        when(mockRoom.getPlayers()).thenReturn(Map.of("player1", playerDto));

        when(userServiceClient.getUserByUsername("player1")).thenReturn(sampleUserDto);
        when(roomManager.createRoom(any(CreateRoomRequest.class))).thenReturn(mockRoom);

        // when/then
        mockMvc.perform(post("/game/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("room123"))
                .andExpect(jsonPath("$.players[0].login").value("player1"));
    }

    @Test
    @WithMockUser(username = "player2")
    void shouldJoinRoom_whenRoomExistsAndUserHasCredits() throws Exception {
        // given
        UserDetailsDto joiningUser = new UserDetailsDto(2L, "player2", "p2@test.com", "P", "Two", "DE", 200L, LocalDate.now());
        when(userServiceClient.getUserByUsername("player2")).thenReturn(joiningUser);
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));
        when(mockRoom.join("player2")).thenReturn(true);

        // when/then
        mockMvc.perform(post("/game/room123/join").with(csrf()))
                .andExpect(status().isOk());

        verify(mockRoom).join("player2");
    }

    @Test
    @WithMockUser(username = "player2")
    void shouldReturnBadRequest_whenJoiningRoomWithInsufficientCredits() throws Exception {
        // given
        UserDetailsDto poorUser = new UserDetailsDto(2L, "player2", "p2@test.com", "P", "Two", "DE", 10L, LocalDate.now());
        when(userServiceClient.getUserByUsername("player2")).thenReturn(poorUser);
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom)); // mockRoom wymaga 50 kredytów

        // when/then
        mockMvc.perform(post("/game/room123/join").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not enough credits to join a room"));
    }

    @Test
    @WithMockUser(username = "player1")
    void shouldLeaveRoom() throws Exception {
        // given
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));
        when(mockRoom.leave("player1")).thenReturn(true);

        // when/then
        mockMvc.perform(post("/game/room123/leave").with(csrf()))
                .andExpect(status().isOk());

        verify(mockRoom).leave("player1");
    }

    @Test
    @WithMockUser
    void shouldListAllRooms() throws Exception {
        // given
        PlayerDto playerDto = new PlayerDto("player1", 1000L);
        when(mockRoom.getPlayers()).thenReturn(Map.of("player1", playerDto));
        when(roomManager.getAllRooms()).thenReturn(List.of(mockRoom));

        // when/then
        mockMvc.perform(get("/game"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId").value("room123"))
                .andExpect(jsonPath("$[0].players[0].login").value("player1"))
                .andExpect(jsonPath("$[0].players[0].credits").value(1000L));
    }

    @Test
    @WithMockUser(username = "player1") // Użytkownik jest właścicielem
    void shouldStartGame_whenUserIsOwnerAndRoomIsNotEmpty() throws Exception {
        // given
        PlayerDto playerDto = new PlayerDto("player1", 1000L);
        when(mockRoom.getPlayers()).thenReturn(Map.of("player1", playerDto)); // Pokój nie jest pusty
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));

        // when/then
        mockMvc.perform(post("/game/room123/start").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Game start signal sent."));

        verify(mockRoom).signalGameStart();
    }

    @Test
    @WithMockUser(username = "another_player") // Użytkownik NIE jest właścicielem
    void shouldReturnForbidden_whenUserIsNotOwnerAndTriesToStartGame() throws Exception {
        // given
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));
        // Właściciel to "player1", a zalogowany to "another_player"

        // when/then
        mockMvc.perform(post("/game/room123/start").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only the room owner can start the game."));

        verify(mockRoom, never()).signalGameStart();
    }

    @Test
    @WithMockUser(username = "player1")
    void shouldReturnBadRequest_whenStartingAnEmptyRoom() throws Exception {
        // given
        when(mockRoom.getPlayers()).thenReturn(Collections.emptyMap()); // Pokój jest pusty
        when(roomManager.getRoomById("room123")).thenReturn(Optional.of(mockRoom));

        // when/then
        mockMvc.perform(post("/game/room123/start").with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot start an empty game."));

        verify(mockRoom, never()).signalGameStart();
    }

    @Test
    @WithMockUser(username = "player1")
    void shouldReturnNotFound_whenStartingNonExistentRoom() throws Exception {
        // given
        when(roomManager.getRoomById("nonexistent_room")).thenReturn(Optional.empty());

        // when/then
        mockMvc.perform(post("/game/nonexistent_room/start").with(csrf()))
                .andExpect(status().isNotFound());
    }
}