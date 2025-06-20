package pl.zzpj.dealmate.chatservice.dto;

import java.util.Set;

public record RoomStateUpdateDto(
        String roomId,
        Set<String> players,
        String systemMessage
) {}