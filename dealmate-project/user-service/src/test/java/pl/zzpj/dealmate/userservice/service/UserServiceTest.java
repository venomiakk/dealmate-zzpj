package pl.zzpj.dealmate.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.zzpj.dealmate.userservice.exception.UserWithLoginExistsException;
import pl.zzpj.dealmate.userservice.model.UserEntity;
import pl.zzpj.dealmate.userservice.payload.request.RegisterRequest;
import pl.zzpj.dealmate.userservice.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("testUser", "email@example.com", "password");
        UserEntity savedUser = new UserEntity("testUser", "email@example.com", "encodedPassword");

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("email@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        UserEntity result = userService.registerUser(request);

        // Then
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getEmail()).isEqualTo("email@example.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        RegisterRequest request = new RegisterRequest("testUser", "email@example.com", "password");

        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        // When & Then
        try {
            userService.registerUser(request);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UserWithLoginExistsException.class);
            assertThat(e.getMessage()).isEqualTo("Error: Username is already taken!");
        }
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest("testUser", "email@example.com", "password");

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("email@example.com")).thenReturn(true);

        // When & Then
        try {
            userService.registerUser(request);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UserWithLoginExistsException.class);
            assertThat(e.getMessage()).isEqualTo("Error: Email is already in use!");
        }
    }

    @Test
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        String username = "testUser";
        UserEntity user = new UserEntity(username, "email@example.com", "password");

        when(userRepository.findByUsername(username)).thenReturn(java.util.Optional.of(user));

        // When
        UserEntity result = userService.getUserByUsername(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo("email@example.com");
        assertThat(result.getPassword()).isEqualTo("password");
    }

    //@Test
    //void shouldThrowWhenUserNotFound() {
    //    // Given
    //    String username = "nonExistentUser";
    //
    //    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
    //
    //    // When & Then
    //    try {
    //        userService.getUserByUsername(username);
    //    } catch (Exception e) {
    //        assertThat(e).isInstanceOf(UserWithLoginExistsException.class);
    //        assertThat(e.getMessage()).isEqualTo("User with username nonExistentUser does not exist");
    //    }
    //}
}