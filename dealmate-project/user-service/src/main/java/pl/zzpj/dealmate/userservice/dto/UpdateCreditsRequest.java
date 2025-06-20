package pl.zzpj.dealmate.userservice.dto;
import java.math.BigDecimal;

public record UpdateCreditsRequest(String username, BigDecimal amountChange) {
}