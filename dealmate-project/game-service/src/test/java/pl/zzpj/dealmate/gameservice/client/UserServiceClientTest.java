package pl.zzpj.dealmate.gameservice.client;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceClientTest {

    @Test
    void testGetUserByUsername() {
        UserServiceClient userServiceClient = Mockito.mock(UserServiceClient.class);
        UserDetailsDto userDetails = new UserDetailsDto(1L, "user1", "email", "John", "Doe", "PL", 100L, LocalDate.now());

        Mockito.when(userServiceClient.getUserByUsername("user1")).thenReturn(userDetails);

        UserDetailsDto result = userServiceClient.getUserByUsername("user1");
        assertEquals(userDetails, result);
    }

    @Test
    void testUpdateUserCredits() {
        UserServiceClient userServiceClient = Mockito.mock(UserServiceClient.class);
        userServiceClient.updateUserCredits("user1", 50L);
        Mockito.verify(userServiceClient).updateUserCredits("user1", 50L);
    }
}
