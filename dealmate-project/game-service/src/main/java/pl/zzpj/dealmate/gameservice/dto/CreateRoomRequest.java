package pl.zzpj.dealmate.gameservice.dto;

import pl.zzpj.dealmate.gameservice.model.EGameType;

public record CreateRoomRequest(
        String ownerLogin,
        String name,
        EGameType gameType,
        int maxPlayers,
        boolean isPublic
) {
}
