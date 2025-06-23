package pl.zzpj.dealmate.gameservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.gameservice.game.dto.CardDto;
import pl.zzpj.dealmate.gameservice.game.dto.DeckDto;

import java.util.List;

@FeignClient(name = "deckservice")
public interface DeckServiceClient {

    @PostMapping("/deck/createdeck")
    DeckDto createDeck(@RequestParam("deckCount") int deckCount);

    @GetMapping("/deck/{id}/drawcards/{count}")
    List<CardDto> drawCards(@PathVariable("id") long deckId, @PathVariable("count") int count);
}