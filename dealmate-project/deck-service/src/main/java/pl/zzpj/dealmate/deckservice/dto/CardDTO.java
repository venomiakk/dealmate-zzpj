package pl.zzpj.dealmate.deckservice.dto;

import lombok.Data;

@Data
public class CardDTO {
    private String code;
    private String value;
    private String suit;
    private ImageLinksDTO images;

    public String getPngImage() {
        return images != null ? images.getPng() : null;
    }

    public String getSvgImage() {
        return images != null ? images.getSvg() : null;
    }
}
