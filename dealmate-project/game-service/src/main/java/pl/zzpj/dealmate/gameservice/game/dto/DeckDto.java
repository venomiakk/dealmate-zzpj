// Plik: gameservice/src/main/java/pl/zzpj/dealmate/gameservice/game/dto/DeckDto.java
package pl.zzpj.dealmate.gameservice.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeckDto(
        // ZMIANA: Typ zmieniony na String, nazwa w adnotacji na "deckId"
        @JsonProperty("deckId") String deckId,

        // Nazwa pola bez zmian
        boolean shuffled,

        // ZMIANA: Nazwa pola zmieniona na "remainingCards"
        @JsonProperty("remainingCards") int remainingCards,

        @JsonProperty("id") long id
) {}