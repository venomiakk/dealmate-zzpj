package pl.zzpj.dealmate.deckservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.exception.CreateDeckException;
import pl.zzpj.dealmate.deckservice.exception.DrawCardsException;

import pl.zzpj.dealmate.deckservice.exception.ShuffleDeckException;
import pl.zzpj.dealmate.deckservice.payload.response.AddToPileApiResponse;

import pl.zzpj.dealmate.deckservice.payload.response.CreateDeckApiResponse;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;
import pl.zzpj.dealmate.deckservice.payload.response.DrawCardsApiResponse;
import pl.zzpj.dealmate.deckservice.payload.response.ShuffleDeckApiResponse;
import pl.zzpj.dealmate.deckservice.repository.DeckRepository;
import pl.zzpj.dealmate.deckservice.repository.PileRepository;

import java.util.List;

@Service
public class DeckService {
    //TODO: Consider using async WebClient because RestTemplate is deprecated
    private final RestTemplate restTemplate;
    private final DeckRepository deckRepository;
    private final PileRepository pileRepository;
    @Value("${deck.api.url}")
    private String deckApiUrl;

    public DeckService(RestTemplate restTemplate, DeckRepository deckRepository, PileRepository pileRepository) {
        this.restTemplate = restTemplate;
        this.deckRepository = deckRepository;
        this.pileRepository = pileRepository;
    }

    public DeckEntity createDeck(int deckCount){
        String url = deckApiUrl + "/new/shuffle/?deck_count=" + deckCount;
        CreateDeckApiResponse createDeckApiResponse = restTemplate.getForObject(url, CreateDeckApiResponse.class);
        if (createDeckApiResponse != null) {
            DeckEntity deckEntity = new DeckEntity();
            deckEntity.setDeckId(createDeckApiResponse.getDeckId());
            deckEntity.setShuffled(createDeckApiResponse.isShuffled());
            deckEntity.setRemainingCards(createDeckApiResponse.getRemainingCards());
            deckRepository.save(deckEntity);
            return deckEntity;
        } else {
            throw new CreateDeckException("Failed to create a new deck");
        }
    }

    public PileEntity addCardsToPile(Long id, String pileName, List<String> cards) {
        DeckEntity deck = deckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        // WALIDACJA: czy karty były wcześniej wyciągnięte?
        if (!deck.getDrawnCardCodes().containsAll(cards)) {
            throw new RuntimeException("One or more cards were not previously drawn from this deck!");
        }

        // budujemy URL do API
        String cardsQueryParam = String.join(",", cards);
        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/add/?cards=" + cardsQueryParam;

        AddToPileApiResponse response = restTemplate.getForObject(url, AddToPileApiResponse.class);

        if (response != null && response.isSuccess()) {
            int remaining = response.getPiles().get(pileName).getRemaining();

            // szukamy lub tworzymy nowy pile
            PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck)
                    .orElseGet(() -> {
                        PileEntity newPile = new PileEntity();
                        newPile.setPileName(pileName);
                        newPile.setDeck(deck);
                        return newPile;
                    });

            pile.setRemainingCards(remaining);
            pile.setCardCodes(cards);
            pileRepository.save(pile);

            // USUWAMY karty z listy wyciągniętych (bo zostały "odłożone")
            deck.getDrawnCardCodes().removeAll(cards);
            deckRepository.save(deck);

            return pile;
        } else {
            throw new RuntimeException("Failed to add cards to pile");
        }
    }






    public DeckEntity shuffleDeck(long id) {
        DeckEntity deckEntity = deckRepository.findById(id)
                .orElseThrow(() -> new ShuffleDeckException("Deck not found in database"));

        String url = deckApiUrl + "/" + deckEntity.getDeckId() + "/shuffle/?remaining=true";

        ShuffleDeckApiResponse shuffleDeckApiResponse = restTemplate.getForObject(url, ShuffleDeckApiResponse.class);

        if (shuffleDeckApiResponse != null && shuffleDeckApiResponse.isShuffled()) {
            deckEntity.setShuffled(shuffleDeckApiResponse.isShuffled());
            deckEntity.setRemainingCards(shuffleDeckApiResponse.getRemainingCards());
            deckRepository.save(deckEntity);
            return deckEntity;
        } else {
            throw new ShuffleDeckException("Failed to shuffle the deck using external API");
        }
    }



    public List<CardDTO> drawCardsFromDeck(long id, int count) {
        DeckEntity deckEntity = deckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        if (deckEntity.getRemainingCards() < count) {
            throw new DrawCardsException("Not enough cards in the deck!");
        }
        String url = deckApiUrl + "/" + deckEntity.getDeckId() + "/draw/?count=" + count;
        DrawCardsApiResponse drawCardsApiResponse = restTemplate.getForObject(url, DrawCardsApiResponse.class);

        if (drawCardsApiResponse == null) {
            throw new DrawCardsException("Failed to draw cards from the deck!");
        }
        List<CardDTO> cards = drawCardsApiResponse.getCards();

        // Zapisujemy kody wyciągniętych kart
        List<String> cardCodes = cards.stream()
                .map(CardDTO::getCode)
                .toList();

        deckEntity.setRemainingCards(drawCardsApiResponse.getRemaining());
        deckEntity.getDrawnCardCodes().addAll(cardCodes);
        deckRepository.save(deckEntity);

        return cards;
    }


}
