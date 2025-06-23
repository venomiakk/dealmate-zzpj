package pl.zzpj.dealmate.gameservice.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.zzpj.dealmate.gameservice.model.EGameType;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateRoomRequestTest {

    @Test
    void shouldCreateRecordAndGetFields() {
        CreateRoomRequest request = new CreateRoomRequest(
                "owner1",
                "Room name",
                EGameType.BLACKJACK,
                5,
                true,
                100L
        );

        assertThat(request.ownerLogin()).isEqualTo("owner1");
        assertThat(request.name()).isEqualTo("Room name");
        assertThat(request.gameType()).isEqualTo(EGameType.BLACKJACK);
        assertThat(request.maxPlayers()).isEqualTo(5);
        assertThat(request.isPublic()).isTrue();
        assertThat(request.entryFee()).isEqualTo(100L);
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {
        CreateRoomRequest request1 = new CreateRoomRequest("owner1", "Room name", EGameType.BLACKJACK, 5, true, 100L);
        CreateRoomRequest request2 = new CreateRoomRequest("owner1", "Room name", EGameType.BLACKJACK, 5, true, 100L);

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void shouldVerifyToString() {
        CreateRoomRequest request = new CreateRoomRequest("owner1", "Room name", EGameType.BLACKJACK, 5, true, 100L);
        String toString = request.toString();

        assertThat(toString).contains("owner1", "Room name", "BLACKJACK", "5", "true", "100");
    }

    @Test
    void shouldSerializeAndDeserializeJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CreateRoomRequest request = new CreateRoomRequest("owner1", "Room name", EGameType.BLACKJACK, 5, true, 100L);

        String json = objectMapper.writeValueAsString(request);
        CreateRoomRequest deserialized = objectMapper.readValue(json, CreateRoomRequest.class);

        assertThat(deserialized).isEqualTo(request);
    }
}
