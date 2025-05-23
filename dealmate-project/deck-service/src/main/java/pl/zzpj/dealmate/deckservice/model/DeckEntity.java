package pl.zzpj.dealmate.deckservice.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
public class DeckEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deckId;
    private boolean isShuffled;
    private int remainingCards;

    @ElementCollection
    private List<String> drawnCardCodes = new ArrayList<>();

}
