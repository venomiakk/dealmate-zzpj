// Plik: gameservice/src/main/java/pl/zzpj/dealmate/gameservice/game/dto/CardDto.java
package pl.zzpj.dealmate.gameservice.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// ZMIANA: Pole "image" zostało zastąpione przez obiekt "images"
public record CardDto(
        String code,
        String value,
        String suit,
        @JsonProperty("images") ImageLinksDto images
) {}