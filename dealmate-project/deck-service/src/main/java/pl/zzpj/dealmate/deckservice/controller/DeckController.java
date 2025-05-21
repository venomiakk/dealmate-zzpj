package pl.zzpj.dealmate.deckservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.service.DeckService;

import java.util.List;

@RestController
@RequestMapping("/deck")
public class DeckController {
    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    //Creates a deck of unshuffled cards
    @PostMapping("/createdeck")
    public ResponseEntity<DeckEntity> createDeck(@RequestParam int deckCount) {
        try{
            DeckEntity deckEntity = deckService.createDeck(deckCount);
            return ResponseEntity.status(HttpStatus.CREATED).body(deckEntity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    //Reshuffles cards in already existing deck(if remaining=True will only shuffle
    //those cards remaining in the main stack, leaving any piles or drawn cards alone).
    @PostMapping("/reshuffledeck/{id}")
    public ResponseEntity<DeckEntity> reshuffleDeck(@PathVariable long id) {
        try {
            DeckEntity deckEntity = deckService.shuffleDeck(id);
            return ResponseEntity.status(HttpStatus.OK).body(deckEntity);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }





    @GetMapping("/drawcards/{id}/{count}")
    public ResponseEntity<List<CardDTO>> drawCardsFromDeck(@PathVariable long id, @PathVariable int count) {
        try{
            return ResponseEntity.ok(deckService.drawCardsFromDeck(id, count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
