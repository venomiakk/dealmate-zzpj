package pl.zzpj.dealmate.gameservice.dto;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoomStateUpdateDtoTest {

    @Test
    void shouldCreateRoomStateUpdateDtoCorrectly() {

        String roomId = "room456";
        Set<String> players = Set.of("player1", "player2", "player3");
        String systemMessage = "Gracz player1 dołączył do pokoju.";


        RoomStateUpdateDto dto = new RoomStateUpdateDto(roomId, players, systemMessage);


        assertEquals(roomId, dto.roomId());
        assertEquals(players, dto.players());
        assertEquals(systemMessage, dto.systemMessage());
    }
}
