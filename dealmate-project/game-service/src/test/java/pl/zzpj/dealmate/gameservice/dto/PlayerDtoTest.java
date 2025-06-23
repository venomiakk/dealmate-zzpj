package pl.zzpj.dealmate.gameservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerDtoTest {

    @Test
    void shouldCreatePlayerDtoCorrectly() {

        String login = "testPlayer";
        Long credits = 500L;


        PlayerDto playerDto = new PlayerDto(login, credits);


        assertEquals(login, playerDto.login());
        assertEquals(credits, playerDto.credits());


        assertEquals(login, playerDto.getLogin());
        assertEquals(credits, playerDto.getCredits());
    }
}
