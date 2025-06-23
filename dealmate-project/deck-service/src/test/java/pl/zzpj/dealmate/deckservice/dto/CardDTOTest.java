package pl.zzpj.dealmate.deckservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardDTOTest {

    @Test
    void shouldSetAndGetFieldsProperly() {
        CardDTO card = new CardDTO();
        card.setCode("AS");
        card.setValue("Ace");
        card.setSuit("Spades");

        ImageLinksDTO imageLinks = new ImageLinksDTO();
        imageLinks.setPng("pngUrl");
        imageLinks.setSvg("svgUrl");
        card.setImages(imageLinks);

        assertThat(card.getCode()).isEqualTo("AS");
        assertThat(card.getValue()).isEqualTo("Ace");
        assertThat(card.getSuit()).isEqualTo("Spades");
        assertThat(card.getImages()).isEqualTo(imageLinks);
    }

    @Test
    void getPngImage_ShouldReturnCorrectPngUrl() {
        CardDTO card = new CardDTO();
        assertThat(card.getPngImage()).isNull();

        ImageLinksDTO imageLinks = new ImageLinksDTO();
        imageLinks.setPng("pngImageUrl");
        card.setImages(imageLinks);

        assertThat(card.getPngImage()).isEqualTo("pngImageUrl");
    }

    @Test
    void getSvgImage_ShouldReturnCorrectSvgUrl() {
        CardDTO card = new CardDTO();
        assertThat(card.getSvgImage()).isNull();

        ImageLinksDTO imageLinks = new ImageLinksDTO();
        imageLinks.setSvg("svgImageUrl");
        card.setImages(imageLinks);

        assertThat(card.getSvgImage()).isEqualTo("svgImageUrl");
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {
        CardDTO card1 = new CardDTO();
        card1.setCode("AS");
        card1.setValue("Ace");
        card1.setSuit("Spades");

        CardDTO card2 = new CardDTO();
        card2.setCode("AS");
        card2.setValue("Ace");
        card2.setSuit("Spades");

        assertThat(card1).isEqualTo(card2);
        assertThat(card1.hashCode()).isEqualTo(card2.hashCode());
    }

    @Test
    void shouldVerifyToString() {
        CardDTO card = new CardDTO();
        card.setCode("AS");
        card.setValue("Ace");
        card.setSuit("Spades");

        assertThat(card.toString()).contains("AS", "Ace", "Spades");
    }
}
