//package pl.zzpj.dealmate.aiservice.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.servlet.MockMvc;
//import pl.zzpj.dealmate.aiservice.dto.*;
//import pl.zzpj.dealmate.aiservice.service.AiSuggestionService;
//import java.util.List;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AiSuggestionController.class)
//@AutoConfigureMockMvc(addFilters = false)
//class AiSuggestionControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private AiSuggestionService service;
//
//    @Test
//    void shouldReturnAiMove() throws Exception {
//        // given
//        PokerAiRequest request = new PokerAiRequest(
//                List.of(),
//                List.of(new CardDto(Rank.ACE, Suit.SPADES),
//                        new CardDto(Rank.KING, Suit.HEARTS)),
//                List.of(new CardDto(Rank.TEN, Suit.CLUBS),
//                        new CardDto(Rank.TWO, Suit.DIAMONDS),
//                        new CardDto(Rank.EIGHT, Suit.SPADES))
//        );
//
//        when(service.getBestMove(request)).thenReturn("CALL");
//
//        // when / then
//        mockMvc.perform(post("/ai/suggest-move")
//                        .contentType("application/json")
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(content().string("CALL"));
//    }
//}