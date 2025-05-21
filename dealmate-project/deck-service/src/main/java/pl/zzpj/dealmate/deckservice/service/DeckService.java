package pl.zzpj.dealmate.deckservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.exception.CreateDeckException;
import pl.zzpj.dealmate.deckservice.exception.DrawCardsException;
import pl.zzpj.dealmate.deckservice.exception.ShuffleDeckException;
import pl.zzpj.dealmate.deckservice.payload.response.CreateDeckApiResponse;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.payload.response.DrawCardsApiResponse;
import pl.zzpj.dealmate.deckservice.payload.response.ShuffleDeckApiResponse;
import pl.zzpj.dealmate.deckservice.repository.DeckRepository;

import java.util.List;

@Service
public class DeckService {
    //TODO: Consider using async WebClient because RestTemplate is deprecated
    private final RestTemplate restTemplate;
    private final DeckRepository deckRepository;
    @Value("${deck.api.url}")
    private String deckApiUrl;

    public DeckService(RestTemplate restTemplate, DeckRepository deckRepository) {
        this.restTemplate = restTemplate;
        this.deckRepository = deckRepository;
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
        // ?: Currently returned images are doubled because of getPngImage and getSvgImage in CardDTO
        // ?: Consider flattening the response in the future
        DeckEntity deckEntity = deckRepository.findById(id).orElseThrow(() -> new RuntimeException("Deck not found"));
        String url = deckApiUrl + "/" + deckEntity.getDeckId() + "/draw/?count=" + count;
        DrawCardsApiResponse drawCardsApiResponse = restTemplate.getForObject(url, DrawCardsApiResponse.class);

        if (drawCardsApiResponse == null) throw new DrawCardsException("Failed to draw cards from the deck!");
        if (deckEntity.getRemainingCards() < count) throw new DrawCardsException("Not enough cards in the deck!");

        List<CardDTO> cards = drawCardsApiResponse.getCards();
        deckEntity.setRemainingCards(drawCardsApiResponse.getRemaining());
        deckRepository.save(deckEntity);
        return cards;

    }

}
