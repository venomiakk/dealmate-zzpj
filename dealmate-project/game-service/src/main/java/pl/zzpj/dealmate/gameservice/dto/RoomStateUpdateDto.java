package pl.zzpj.dealmate.gameservice.dto;

import java.util.Set;

// DTO do wysyłania aktualizacji z GameService do ChatService
public record RoomStateUpdateDto(
        String roomId,
        Set<String> players,
        String systemMessage // np. "Gracz X dołączył do pokoju."
) {}