package pl.zzpj.dealmate.gameservice.dto;

import java.util.Set; // Make sure to import Set

public record RoomInfo(
        String roomId,
        String joinCode,
        Set<String> players, // Ensure this is Set<String>
        String name,
        String gameType,
        int maxPlayers,
        boolean isPublic,
        String ownerLogin,
        double entryFee
) {
}