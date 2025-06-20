package pl.zzpj.dealmate.gameservice.dto;

import java.time.LocalDate;

public record UserDetailsDto(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String countryCode,
        Long credits,
        LocalDate createdAt
) {
}