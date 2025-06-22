package pl.zzpj.dealmate.gameservice.client;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.zzpj.dealmate.gameservice.dto.RoomStateUpdateDto;

import java.util.Set;

class ChatServiceClientTest {

    @Test
    void testNotifyRoomStateChange() {
        ChatServiceClient chatServiceClient = Mockito.mock(ChatServiceClient.class);
        RoomStateUpdateDto dto = new RoomStateUpdateDto("room123", Set.of("player1", "player2"), "Player1 joined");

        chatServiceClient.notifyRoomStateChange(dto);

        Mockito.verify(chatServiceClient).notifyRoomStateChange(dto);
    }
}
