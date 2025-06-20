package pl.zzpj.dealmate.chatservice.dto;

import java.util.Set;

// Frontend oczekuje obiektu z polem 'players'
public record RoomInfoUpdateDto(Set<String> players) {}