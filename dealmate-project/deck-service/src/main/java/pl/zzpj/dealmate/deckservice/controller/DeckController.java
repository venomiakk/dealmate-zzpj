package pl.zzpj.dealmate.deckservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.service.DeckService;

@RestController
@RequestMapping("/deck")
public class DeckController {
    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping("/createdeck")
    public ResponseEntity<DeckEntity> createDeck(@RequestParam int deckCount) {
        try{
            DeckEntity deckEntity = deckService.createDeck(deckCount);
            return ResponseEntity.status(HttpStatus.CREATED).body(deckEntity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
