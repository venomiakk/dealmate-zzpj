package pl.zzpj.dealmate.gameservice.dto;

import java.util.Collection;

public record RoomInfo(
        String roomId,
        String joinCode,
        Collection<PlayerDto> players,
        String name,
        String gameType,
        int maxPlayers,
        boolean isPublic,
        String ownerLogin,
        double entryFee
) {
}