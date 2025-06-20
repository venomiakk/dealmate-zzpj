package pl.zzpj.dealmate.chatservice.dto;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RoomInfoUpdateDtoTest {

    @Test
    void testRoomInfoUpdateDto() {
        RoomInfoUpdateDto update = new RoomInfoUpdateDto(Set.of("player1", "player2"));

        assertThat(update.players().size()).isEqualTo(2);
    }
}