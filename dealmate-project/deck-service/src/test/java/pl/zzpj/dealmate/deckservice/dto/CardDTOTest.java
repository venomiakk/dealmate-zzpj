package pl.zzpj.dealmate.deckservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardDTOTest {

    CardDTO card = new CardDTO();
    ImageLinksDTO imageLinks = new ImageLinksDTO();

    @Test
    void getPngImage() {
        assertThat(card.getPngImage()).isNull();
        imageLinks.setPng("pngImage");
        card.setImages(imageLinks);
        assertThat(card.getPngImage()).isEqualTo("pngImage");
    }

    @Test
    void getSvgImage() {
        assertThat(card.getSvgImage()).isNull();
        imageLinks.setSvg("svgImage");
        card.setImages(imageLinks);
        assertThat(card.getSvgImage()).isEqualTo("svgImage");
    }
}