package pl.zzpj.dealmate.deckservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

// * : Should be changed to @SpringBootTest
class RestTemplateConfigTest {

    @Test
    void restTemplate() {
        RestTemplate restTemplate = new RestTemplateConfig().restTemplate();
        assertThat(restTemplate).isNotNull();
    }
}