package pl.zzpj.dealmate.gameservice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsDtoTest {

    @Test
    void shouldCreateUserDetailsDtoCorrectly() {

        Long id = 1L;
        String username = "testUser";
        String email = "test@example.com";
        String firstName = "John";
        String lastName = "Doe";
        String countryCode = "PL";
        Long credits = 1000L;
        LocalDate createdAt = LocalDate.of(2024, 6, 22);


        UserDetailsDto dto = new UserDetailsDto(id, username, email, firstName, lastName, countryCode, credits, createdAt);


        assertEquals(id, dto.id());
        assertEquals(username, dto.username());
        assertEquals(email, dto.email());
        assertEquals(firstName, dto.firstName());
        assertEquals(lastName, dto.lastName());
        assertEquals(countryCode, dto.countryCode());
        assertEquals(credits, dto.credits());
        assertEquals(createdAt, dto.createdAt());
    }
}
