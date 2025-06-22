package pl.zzpj.dealmate.aiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.zzpj.dealmate.aiservice.controller.AiSuggestionController;
import pl.zzpj.dealmate.aiservice.dto.PokerAiRequest;
import pl.zzpj.dealmate.aiservice.service.AiSuggestionService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(AiSuggestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiSuggestionServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiSuggestionService service;

    @Test
    void shouldReturnMoveFromService() throws Exception {
        PokerAiRequest request = new PokerAiRequest(1, 2);
        String expectedMove = "CALL";
        when(service.getBestMove(request)).thenReturn(expectedMove);

        mockMvc.perform(post("/ai/suggest-move")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).isEqualTo(expectedMove);
                });
    }
}