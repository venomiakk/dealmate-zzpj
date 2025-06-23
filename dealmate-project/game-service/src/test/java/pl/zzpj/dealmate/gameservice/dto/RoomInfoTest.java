package pl.zzpj.dealmate.gameservice.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomInfoTest {

    @Test
    void shouldCreateRoomInfoCorrectly() {

        String roomId = "room123";
        String joinCode = "ABC123";
        PlayerDto player1 = new PlayerDto("player1", 1000L);
        PlayerDto player2 = new PlayerDto("player2", 500L);
        List<PlayerDto> players = List.of(player1, player2);
        String name = "FunRoom";
        String gameType = "BLACKJACK";
        int maxPlayers = 4;
        boolean isPublic = true;
        String ownerLogin = "owner1";
        double entryFee = 50.0;


        RoomInfo roomInfo = new RoomInfo(
                roomId, joinCode, players, name, gameType, maxPlayers, isPublic, ownerLogin, entryFee
        );


        assertEquals(roomId, roomInfo.roomId());
        assertEquals(joinCode, roomInfo.joinCode());
        assertEquals(players, roomInfo.players());
        assertEquals(name, roomInfo.name());
        assertEquals(gameType, roomInfo.gameType());
        assertEquals(maxPlayers, roomInfo.maxPlayers());
        assertEquals(isPublic, roomInfo.isPublic());
        assertEquals(ownerLogin, roomInfo.ownerLogin());
        assertEquals(entryFee, roomInfo.entryFee());
    }
}
