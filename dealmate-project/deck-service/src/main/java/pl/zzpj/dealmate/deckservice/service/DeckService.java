package pl.zzpj.dealmate.deckservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import pl.zzpj.dealmate.deckservice.dto.CardDTO;
import pl.zzpj.dealmate.deckservice.exception.CreateDeckException;
import pl.zzpj.dealmate.deckservice.exception.DrawCardsException;

import pl.zzpj.dealmate.deckservice.exception.ShuffleDeckException;
import pl.zzpj.dealmate.deckservice.payload.response.*;

import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.model.PileEntity;
import pl.zzpj.dealmate.deckservice.repository.DeckRepository;
import pl.zzpj.dealmate.deckservice.repository.PileRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeckService {
    //*: Consider using async WebClient because RestTemplate is deprecated
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

    //Create already shuffled deck
    public DeckEntity createDeck(int deckCount) {
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


    //Reshuffle already existing deck based on deck_id
    public DeckEntity shuffleDeck(long id) {
        DeckEntity deckEntity = deckRepository.findById(id).orElseThrow(() -> new ShuffleDeckException("Deck not found in database"));

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

    //Draw x random cards from deck
    public List<CardDTO> drawCardsFromDeck(long id, int count) {
        DeckEntity deckEntity = deckRepository.findById(id).orElseThrow(() -> new RuntimeException("Deck not found"));

        if (deckEntity.getRemainingCards() < count) {
            throw new DrawCardsException("Not enough cards in the deck!");
        }
        String url = deckApiUrl + "/" + deckEntity.getDeckId() + "/draw/?count=" + count;
        DrawCardsApiResponse drawCardsApiResponse = restTemplate.getForObject(url, DrawCardsApiResponse.class);

        if (drawCardsApiResponse == null) {
            throw new DrawCardsException("Failed to draw cards from the deck!");
        }
        List<CardDTO> cards = drawCardsApiResponse.getCards();

        List<String> cardCodes = cards.stream().map(CardDTO::getCode).toList();

        deckEntity.setRemainingCards(drawCardsApiResponse.getRemaining());
        deckEntity.getDrawnCardCodes().addAll(cardCodes);
        deckRepository.save(deckEntity);

        return cards;
    }


    //Creating a pile out of drawned cards (that's why we save drawned cards in a database) and deck can have max 52 cards
    public PileEntity addCardsToPile(Long id, String pileName, List<String> cards) {
        DeckEntity deck = deckRepository.findById(id).orElseThrow(() -> new RuntimeException("Deck not found"));


        if (!deck.getDrawnCardCodes().containsAll(cards)) {
            throw new RuntimeException("One or more cards were not previously drawn from this deck!");
        }

        String cardsQueryParam = String.join(",", cards);
        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/add/?cards=" + cardsQueryParam;

        AddToPileApiResponse response = restTemplate.getForObject(url, AddToPileApiResponse.class);

        if (response != null && response.isSuccess()) {
            int remaining = response.getPiles().get(pileName).getRemaining();


            PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck).orElseGet(() -> {
                PileEntity newPile = new PileEntity();
                newPile.setPileName(pileName);
                newPile.setDeck(deck);
                return newPile;
            });

            pile.setRemainingCards(remaining);
            List<String> updatedCards = new ArrayList<>(pile.getCardCodes() != null ? pile.getCardCodes() : List.of());
            updatedCards.addAll(cards);
            pile.setCardCodes(updatedCards);

            pileRepository.save(pile);


            deck.getDrawnCardCodes().removeAll(cards);
            deckRepository.save(deck);

            return pile;
        } else {
            throw new RuntimeException("Failed to add cards to pile");
        }
    }


    public PileEntity shufflePile(@PathVariable long id, @PathVariable("pileName") String pileName) {
        DeckEntity deck = deckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/shuffle/";

        ShufflePileApiResponse response = restTemplate.getForObject(url, ShufflePileApiResponse.class);

        if (response != null && response.isSuccess() && response.getPiles().containsKey(pileName)) {
            int remaining = response.getPiles().get(pileName).getRemaining();

            PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck).orElseThrow(() ->
                    new RuntimeException("Pile not found for this deck"));

            pile.setRemainingCards(remaining);
            pileRepository.save(pile);

            return pile;
        } else {
            throw new RuntimeException("Failed to shuffle the pile or pile not found in response");
        }
    }


    public List<CardDTO> getCardsFromPile(Long deckId, String pileName) {
        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/list/";
        ListPileApiResponse response = restTemplate.getForObject(url, ListPileApiResponse.class);

        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("Failed to fetch pile from external API");
        }

        ListPileApiResponse.Pile pile = response.getPiles().get(pileName);
        if (pile == null || pile.getCards() == null) {
            throw new RuntimeException("Pile not found or empty");
        }

        return pile.getCards();
    }


    // Dobierz konkretne karty
    public List<CardDTO> drawSpecificCardsFromPile(Long deckId, String pileName, List<String> cardCodes) {
        DeckEntity deck = deckRepository.findById(deckId).orElseThrow(() -> new RuntimeException("Deck not found"));

        String cardsParam = String.join(",", cardCodes);
        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/draw/?cards=" + cardsParam;

        DrawFromPileApiResponse response = restTemplate.getForObject(url, DrawFromPileApiResponse.class);
        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("Failed to draw specific cards from pile");
        }

        List<CardDTO> cards = response.getCards();
        List<String> drawnCodes = cards.stream().map(CardDTO::getCode).toList();

        PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck)
                .orElseThrow(() -> new RuntimeException("Pile not found"));

        List<String> updatedCodes = new ArrayList<>(pile.getCardCodes());
        updatedCodes.removeAll(drawnCodes);
        pile.setCardCodes(updatedCodes);
        pile.setRemainingCards(response.getRemaining());
        pileRepository.save(pile);

        return cards;

    }

    // Dobierz N kart
    public List<CardDTO> drawCountFromPile(Long deckId, String pileName, int count) {
        DeckEntity deck = deckRepository.findById(deckId).orElseThrow(() -> new RuntimeException("Deck not found"));

        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/draw/?count=" + count;

        DrawFromPileApiResponse response = restTemplate.getForObject(url, DrawFromPileApiResponse.class);
        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("Failed to draw cards from pile");
        }

        List<CardDTO> cards = response.getCards();
        List<String> drawnCodes = cards.stream().map(CardDTO::getCode).toList();

        PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck)
                .orElseThrow(() -> new RuntimeException("Pile not found"));

        List<String> updatedCodes = new ArrayList<>(pile.getCardCodes());
        updatedCodes.removeAll(drawnCodes);
        pile.setCardCodes(updatedCodes);
        pile.setRemainingCards(response.getRemaining());
        pileRepository.save(pile);

        return cards;

    }

    // Dobierz kartę z dołu
    public List<CardDTO> drawBottomFromPile(Long deckId, String pileName) {
        DeckEntity deck = deckRepository.findById(deckId).orElseThrow(() -> new RuntimeException("Deck not found"));

        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/draw/bottom/";

        DrawFromPileApiResponse response = restTemplate.getForObject(url, DrawFromPileApiResponse.class);
        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("Failed to draw card from bottom of pile");
        }

        List<CardDTO> cards = response.getCards();
        List<String> drawnCodes = cards.stream().map(CardDTO::getCode).toList();

        PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck)
                .orElseThrow(() -> new RuntimeException("Pile not found"));

        List<String> updatedCodes = new ArrayList<>(pile.getCardCodes());
        updatedCodes.removeAll(drawnCodes);
        pile.setCardCodes(updatedCodes);
        pile.setRemainingCards(response.getRemaining());
        pileRepository.save(pile);

        return cards;

    }

    // Dobierz losową kartę
    public List<CardDTO> drawRandomFromPile(Long deckId, String pileName) {
        DeckEntity deck = deckRepository.findById(deckId).orElseThrow(() -> new RuntimeException("Deck not found"));

        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/draw/random/";

        DrawFromPileApiResponse response = restTemplate.getForObject(url, DrawFromPileApiResponse.class);
        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("Failed to draw random card from pile");
        }

        List<CardDTO> cards = response.getCards();
        List<String> drawnCodes = cards.stream().map(CardDTO::getCode).toList();

        PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck)
                .orElseThrow(() -> new RuntimeException("Pile not found"));

        List<String> updatedCodes = new ArrayList<>(pile.getCardCodes());
        updatedCodes.removeAll(drawnCodes);
        pile.setCardCodes(updatedCodes);
        pile.setRemainingCards(response.getRemaining());
        pileRepository.save(pile);

        return cards;

    }

    public void returnAllCardsToDeck(Long deckId) {
        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        String returnUrl = deckApiUrl + "/" + deck.getDeckId() + "/return";
        ReturnCardsApiResponse returnResponse = restTemplate.getForObject(returnUrl, ReturnCardsApiResponse.class);

        if (returnResponse == null || !returnResponse.isSuccess()) {
            throw new RuntimeException("Failed to return cards to deck");
        }

        List<PileEntity> piles = pileRepository.findAll()
                .stream()
                .filter(pile -> pile.getDeck().equals(deck))
                .toList();

        for (PileEntity pile : piles) {
            pile.getCardCodes().clear();
            pile.setRemainingCards(0);
            pileRepository.save(pile);
        }

        deck.getDrawnCardCodes().clear();

        String listUrl = deckApiUrl + "/" + deck.getDeckId() + "/list/";
        ListPileApiResponse listResponse = restTemplate.getForObject(listUrl, ListPileApiResponse.class);

        if (listResponse != null && listResponse.isSuccess()) {
            List<String> fullDeckCardCodes = listResponse.getPiles()
                    .values()
                    .stream()
                    .flatMap(pile -> pile.getCards().stream())
                    .map(CardDTO::getCode)
                    .toList();

        }
        deck.setRemainingCards(returnResponse.getRemaining());
        deckRepository.save(deck);
    }


    public void returnSpecificCardsToDeck(Long deckId, List<String> cardCodes) {
        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        String cardsParam = String.join(",", cardCodes);
        String url = deckApiUrl + "/" + deck.getDeckId() + "/return/?cards=" + cardsParam;

        ReturnCardsApiResponse response = restTemplate.getForObject(url, ReturnCardsApiResponse.class);
        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("Failed to return specific cards to deck");
        }

        deck.getDrawnCardCodes().removeAll(cardCodes);
        deck.setRemainingCards(response.getRemaining());
        deckRepository.save(deck);
    }


    public void returnAllCardsToPile(Long deckId, String pileName) {
        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/return/";

        ReturnCardsApiResponse response = restTemplate.getForObject(url, ReturnCardsApiResponse.class);
        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("Failed to return all cards to pile");
        }

        PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck)
                .orElseThrow(() -> new RuntimeException("Pile not found"));

        pile.setRemainingCards(response.getRemaining());
        pileRepository.save(pile);
    }


    public void returnSpecificCardsToPile(Long deckId, String pileName, List<String> cardCodes) {
        DeckEntity deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        String cardsParam = String.join(",", cardCodes);
        String url = deckApiUrl + "/" + deck.getDeckId() + "/pile/" + pileName + "/return/?cards=" + cardsParam;

        ReturnCardsApiResponse response = restTemplate.getForObject(url, ReturnCardsApiResponse.class);
        if (response == null || !response.isSuccess()) {
            throw new RuntimeException("Failed to return specific cards to pile");
        }

        PileEntity pile = pileRepository.findByPileNameAndDeck(pileName, deck)
                .orElseThrow(() -> new RuntimeException("Pile not found"));

        List<String> updated = new ArrayList<>(pile.getCardCodes() != null ? pile.getCardCodes() : List.of());
        updated.addAll(cardCodes);
        pile.setCardCodes(updated);
        pile.setRemainingCards(response.getRemaining());
        pileRepository.save(pile);
    }


}
