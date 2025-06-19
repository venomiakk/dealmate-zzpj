package pl.zzpj.dealmate.chatservice.dto;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RoomStateUpdateDtoTest {

    @Test
    void testRoomStateUpdateDto() {
        RoomStateUpdateDto update = new RoomStateUpdateDto("room123", Set.of("player1", "player2"), "Game started");

        assertThat(update.roomId()).isEqualTo("room123");
        assertThat(update.systemMessage()).isEqualTo("Game started");
        assertThat(update.players().size()).isEqualTo(2);

    }

}