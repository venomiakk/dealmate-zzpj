package pl.zzpj.dealmate.deckservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class PileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pileName;

    //Relation to deck entity
    @ManyToOne
    @JoinColumn(name = "deck_id")
    private DeckEntity deck;

    private int remainingCards;

    @ElementCollection
    private List<String> cardCodes;
}

