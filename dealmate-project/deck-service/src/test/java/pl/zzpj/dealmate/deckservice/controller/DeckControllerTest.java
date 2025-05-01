package pl.zzpj.dealmate.deckservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.zzpj.dealmate.deckservice.model.DeckEntity;
import pl.zzpj.dealmate.deckservice.service.DeckService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DeckControllerTest {

    @Mock
    private DeckService deckService;

    @InjectMocks
    private DeckController deckController;

    private DeckEntity deckEntity;

    @BeforeEach
    void setUp() {
        deckEntity = new DeckEntity();
        deckEntity.setDeckId("testDeckId");
        deckEntity.setShuffled(true);
        deckEntity.setRemainingCards(52);
    }
}