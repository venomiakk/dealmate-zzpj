package pl.zzpj.dealmate.chatservice.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private String sender;
    private String content;
    private long timestamp;
}