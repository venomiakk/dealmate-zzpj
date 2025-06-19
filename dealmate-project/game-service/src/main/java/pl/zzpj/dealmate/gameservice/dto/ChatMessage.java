package pl.zzpj.dealmate.gameservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender;
    private String content;
    private String roomId; // To identify which room the message belongs to
    private long timestamp; // When the message was sent

    // Constructor for system messages or when sender/timestamp are set internally
    public ChatMessage(String sender, String content, String roomId) {
        this.sender = sender;
        this.content = content;
        this.roomId = roomId;
        this.timestamp = System.currentTimeMillis();
    }
}