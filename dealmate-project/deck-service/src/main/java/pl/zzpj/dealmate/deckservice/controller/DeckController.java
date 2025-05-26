package pl.zzpj.dealmate.deckservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.dto.DeckDTO;
import pl.zzpj.dealmate.deckservice.dto.PileDTO;
import pl.zzpj.dealmate.deckservice.mapper.EntityToDtoMapper;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;
import pl.zzpj.dealmate.deckservice.service.DeckService;

import java.util.List;

@RestController
@RequestMapping("/deck")
public class DeckController {
    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping("/createdeck")
    public ResponseEntity<DeckDTO> createDeck(@RequestParam int deckCount) {
        try {
            DeckEntity deckEntity = deckService.createDeck(deckCount);
            DeckDTO deckDTO = EntityToDtoMapper.toDeckDTO(deckEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body(deckDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/reshuffledeck/{id}")
    public ResponseEntity<DeckDTO> reshuffleDeck(@PathVariable long id) {
        try {
            DeckEntity deckEntity = deckService.shuffleDeck(id);
            DeckDTO deckDTO = EntityToDtoMapper.toDeckDTO(deckEntity);
            return ResponseEntity.ok(deckDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}/drawcards/{count}")
    public ResponseEntity<List<CardDTO>> drawCardsFromDeck(
            @PathVariable long id,
            @PathVariable int count) {
        try {
            List<CardDTO> cards = deckService.drawCardsFromDeck(id, count);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/{id}/pile/{pileName}/add")
    public ResponseEntity<PileDTO> addToPile(@PathVariable("id") Long id, @PathVariable("pileName") String pileName, @RequestParam List<String> cards) {
        try {
            PileEntity pile = deckService.addCardsToPile(id, pileName, cards);
            PileDTO pileDTO = EntityToDtoMapper.toPileDTO(pile);
            return ResponseEntity.ok(pileDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{id}/pile/{pileName}/shuffle")
    public ResponseEntity<PileDTO> shufflePile(@PathVariable long id, @PathVariable("pileName") String pileName) {
        try {
            PileEntity pile = deckService.shufflePile(id, pileName);
            PileDTO pileDTO = EntityToDtoMapper.toPileDTO(pile);
            return ResponseEntity.ok(pileDTO);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{id}/pile/{pileName}/list")
    public ResponseEntity<List<CardDTO>> listPileCards(
            @PathVariable("id") Long id,
            @PathVariable("pileName") String pileName) {
        try {
            List<CardDTO> cards = deckService.getCardsFromPile(id, pileName);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/{id}/pile/{pileName}/draw")
    public ResponseEntity<List<CardDTO>> drawSpecificCardsFromPile(
            @PathVariable("id") Long id,
            @PathVariable("pileName") String pileName,
            @RequestParam List<String> cards) {
        try {
            List<CardDTO> drawnCards = deckService.drawSpecificCardsFromPile(id, pileName, cards);
            return ResponseEntity.ok(drawnCards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}/pile/{pileName}/draw/count")
    public ResponseEntity<List<CardDTO>> drawCountFromPile(
            @PathVariable("id") Long id,
            @PathVariable("pileName") String pileName,
            @RequestParam int count) {
        try {
            List<CardDTO> drawnCards = deckService.drawCountFromPile(id, pileName, count);
            return ResponseEntity.ok(drawnCards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}/pile/{pileName}/draw/bottom")
    public ResponseEntity<List<CardDTO>> drawBottomFromPile(
            @PathVariable("id") Long id,
            @PathVariable("pileName") String pileName) {
        try {
            List<CardDTO> drawnCards = deckService.drawBottomFromPile(id, pileName);
            return ResponseEntity.ok(drawnCards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}/pile/{pileName}/draw/random")
    public ResponseEntity<List<CardDTO>> drawRandomFromPile(
            @PathVariable("id") Long id,
            @PathVariable("pileName") String pileName) {
        try {
            List<CardDTO> drawnCards = deckService.drawRandomFromPile(id, pileName);
            return ResponseEntity.ok(drawnCards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/{id}/return")
    public ResponseEntity<Void> returnAllToDeck(@PathVariable Long id) {
        try {
            deckService.returnAllCardsToDeck(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/return/cards")
    public ResponseEntity<Void> returnSpecificToDeck(@PathVariable Long id, @RequestParam List<String> cards) {
        try {
            deckService.returnSpecificCardsToDeck(id, cards);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/pile/{pileName}/return")
    public ResponseEntity<Void> returnAllToPile(@PathVariable Long id, @PathVariable String pileName) {
        try {
            deckService.returnAllCardsToPile(id, pileName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/pile/{pileName}/return/cards")
    public ResponseEntity<Void> returnSpecificToPile(
            @PathVariable Long id,
            @PathVariable String pileName,
            @RequestParam List<String> cards) {
        try {
            deckService.returnSpecificCardsToPile(id, pileName, cards);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




}
