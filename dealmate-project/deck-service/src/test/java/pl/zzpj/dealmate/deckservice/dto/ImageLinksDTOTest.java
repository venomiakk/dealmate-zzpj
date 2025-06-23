package pl.zzpj.dealmate.deckservice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImageLinksDTOTest {

    @Test
    void shouldSetAndGetFieldsProperly() {
        ImageLinksDTO imageLinks = new ImageLinksDTO();
        imageLinks.setSvg("svgUrl");
        imageLinks.setPng("pngUrl");

        assertThat(imageLinks.getSvg()).isEqualTo("svgUrl");
        assertThat(imageLinks.getPng()).isEqualTo("pngUrl");
    }

    @Test
    void shouldHandleNullFields() {
        ImageLinksDTO imageLinks = new ImageLinksDTO();

        assertThat(imageLinks.getSvg()).isNull();
        assertThat(imageLinks.getPng()).isNull();
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {
        ImageLinksDTO imageLinks1 = new ImageLinksDTO();
        imageLinks1.setSvg("svgUrl");
        imageLinks1.setPng("pngUrl");

        ImageLinksDTO imageLinks2 = new ImageLinksDTO();
        imageLinks2.setSvg("svgUrl");
        imageLinks2.setPng("pngUrl");

        assertThat(imageLinks1).isEqualTo(imageLinks2);
        assertThat(imageLinks1.hashCode()).isEqualTo(imageLinks2.hashCode());
    }

    @Test
    void shouldVerifyToString() {
        ImageLinksDTO imageLinks = new ImageLinksDTO();
        imageLinks.setSvg("svgLink");
        imageLinks.setPng("pngLink");

        String toString = imageLinks.toString();
        assertThat(toString).contains("svgLink", "pngLink");
    }
}
