package pl.zzpj.dealmate.gameservice.dto;

import java.math.BigDecimal;

// DTO do aktualizacji kredytów w userservice
public record UpdateCreditsRequest(String username, BigDecimal amountChange) {
}