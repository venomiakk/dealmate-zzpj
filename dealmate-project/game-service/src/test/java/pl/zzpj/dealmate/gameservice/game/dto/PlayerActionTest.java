package pl.zzpj.dealmate.gameservice.game.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerActionTest {

    @Test
    void shouldCreateHitAction() {

        PlayerAction action = new PlayerAction.Hit();


        assertTrue(action instanceof PlayerAction.Hit);
    }

    @Test
    void shouldCreateStandAction() {

        PlayerAction action = new PlayerAction.Stand();


        assertTrue(action instanceof PlayerAction.Stand);
    }
}
