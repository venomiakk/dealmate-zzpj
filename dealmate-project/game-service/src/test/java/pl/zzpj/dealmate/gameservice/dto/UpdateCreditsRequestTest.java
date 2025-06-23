package pl.zzpj.dealmate.gameservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UpdateCreditsRequestTest {

    @Test
    void shouldCreateUpdateCreditsRequestCorrectly() {

        String username = "testUser";
        BigDecimal amountChange = new BigDecimal("150.00");


        UpdateCreditsRequest request = new UpdateCreditsRequest(username, amountChange);


        assertEquals(username, request.username());
        assertEquals(amountChange, request.amountChange());
    }
}
