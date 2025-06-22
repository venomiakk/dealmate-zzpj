package pl.zzpj.dealmate.gameservice.game.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameActionRequestTest {

    @Test
    void shouldCreateGameActionRequestCorrectly() {
        // given
        PlayerAction action = new PlayerAction.Hit();
        String playerId = "player123";

        // when
        GameActionRequest request = new GameActionRequest(action, playerId);

        // then
        assertEquals(action, request.action());
        assertEquals(playerId, request.playerId());
    }
}
