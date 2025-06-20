package pl.zzpj.dealmate.gameservice.game.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "action")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayerAction.Hit.class, name = "HIT"),
        @JsonSubTypes.Type(value = PlayerAction.Stand.class, name = "STAND")
})
public sealed interface PlayerAction {
    record Hit() implements PlayerAction {}
    record Stand() implements PlayerAction {}
}