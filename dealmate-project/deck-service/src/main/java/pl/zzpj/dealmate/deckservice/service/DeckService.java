package pl.zzpj.dealmate.deckservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.zzpj.dealmate.deckservice.external.response.CreateDeckApiResponse;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.repository.DeckRepository;

@Service
public class DeckService {
    //TODO: Consider using WebClient because RestTemplate is deprecated
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
            throw new RuntimeException("Failed to create a new deck");
        }
    }
}
