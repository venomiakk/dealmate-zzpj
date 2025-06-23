package pl.zzpj.dealmate.gameservice.game.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageLinksDtoTest {

    @Test
    void shouldCreateImageLinksDtoCorrectly() {
        // given
        String svgLink = "https://example.com/image.svg";
        String pngLink = "https://example.com/image.png";

        // when
        ImageLinksDto dto = new ImageLinksDto(svgLink, pngLink);

        // then
        assertEquals(svgLink, dto.svg());
        assertEquals(pngLink, dto.png());
    }
}
