package pl.zzpj.dealmate.gameservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomWebSocketControllerTest {

    @Mock
    private RoomManager roomManager;
    @Mock
    private Principal principal;

    @Test
    void shouldHandleJoin() {
        // Given
        RoomWebSocketController controller = new RoomWebSocketController(roomManager);
        String playerId = "testPlayer";

        // When
        when(principal.getName()).thenReturn(playerId);

        // when
        controller.handleJoin(principal);

        // then – tylko obserwacja stdout (lub możesz dodać logikę do testowania)
        verify(principal).getName();
    }
}