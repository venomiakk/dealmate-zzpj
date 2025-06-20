package pl.zzpj.dealmate.chatservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ChatMessageDtoTest {

    @Test
    void testChatMessageDto() {
        ChatMessageDto message = new ChatMessageDto();
        message.setSender("Alice");
        message.setContent("Hello, world!");
        message.setTimestamp(System.currentTimeMillis());

        assertThat(message.getSender()).isEqualTo("Alice");
        assertThat(message.getContent()).isEqualTo("Hello, world!");
        assertTrue(message.getTimestamp() > 0);
    }
}