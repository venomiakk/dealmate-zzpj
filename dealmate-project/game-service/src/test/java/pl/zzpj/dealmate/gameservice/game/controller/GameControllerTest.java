package pl.zzpj.dealmate.gameservice.game.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pl.zzpj.dealmate.gameservice.game.dto.GameActionRequest;
import pl.zzpj.dealmate.gameservice.game.dto.PlayerAction;

import pl.zzpj.dealmate.gameservice.model.GameRoom;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.util.Optional;

import static org.mockito.Mockito.*;

class GameControllerTest {

    private RoomManager roomManager;
    private GameController gameController;

    @BeforeEach
    void setup() {
        roomManager = mock(RoomManager.class);
        gameController = new GameController(roomManager);
    }

    @Test
    void shouldHandleValidGameAction() {
        GameActionRequest request = new GameActionRequest(new PlayerAction.Hit(), "player1");
        GameRoom gameRoom = mock(GameRoom.class);

        when(roomManager.getRoomById("roomId")).thenReturn(Optional.of(gameRoom));

        gameController.handleGameAction(request, "roomId");

        verify(gameRoom, times(1)).handlePlayerAction("player1", request.action());
    }


}
