package pl.zzpj.dealmate.gameservice.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageTest {

    @Test
    void shouldSetAndGetFieldsProperly() {
        ChatMessage message = new ChatMessage();
        message.setSender("user1");
        message.setContent("Hello");
        message.setRoomId("room123");
        message.setTimestamp(123456789L);

        assertThat(message.getSender()).isEqualTo("user1");
        assertThat(message.getContent()).isEqualTo("Hello");
        assertThat(message.getRoomId()).isEqualTo("room123");
        assertThat(message.getTimestamp()).isEqualTo(123456789L);
    }

    @Test
    void shouldUseAllArgsConstructor() {
        ChatMessage message = new ChatMessage("user1", "Hi", "room123", 987654321L);

        assertThat(message.getSender()).isEqualTo("user1");
        assertThat(message.getContent()).isEqualTo("Hi");
        assertThat(message.getRoomId()).isEqualTo("room123");
        assertThat(message.getTimestamp()).isEqualTo(987654321L);
    }

    @Test
    void shouldUseSystemMessageConstructor() {
        long before = System.currentTimeMillis();

        ChatMessage message = new ChatMessage("system", "Game started", "room123");

        long after = System.currentTimeMillis();

        assertThat(message.getSender()).isEqualTo("system");
        assertThat(message.getContent()).isEqualTo("Game started");
        assertThat(message.getRoomId()).isEqualTo("room123");
        assertThat(message.getTimestamp()).isBetween(before, after);
    }

    @Test
    void shouldSerializeAndDeserializeJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ChatMessage message = new ChatMessage("user1", "Hello", "room123", 123456L);

        String json = mapper.writeValueAsString(message);
        ChatMessage deserialized = mapper.readValue(json, ChatMessage.class);

        assertThat(deserialized.getSender()).isEqualTo("user1");
        assertThat(deserialized.getContent()).isEqualTo("Hello");
        assertThat(deserialized.getRoomId()).isEqualTo("room123");
        assertThat(deserialized.getTimestamp()).isEqualTo(123456L);
    }
}
