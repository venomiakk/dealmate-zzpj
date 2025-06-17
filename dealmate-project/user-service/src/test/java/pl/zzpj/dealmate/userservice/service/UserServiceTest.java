package pl.zzpj.dealmate.userservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.zzpj.dealmate.userservice.dto.UpdateUserRequest;
import pl.zzpj.dealmate.userservice.exception.custom.*;
import pl.zzpj.dealmate.userservice.model.ECountryCodes;
import pl.zzpj.dealmate.userservice.model.UserEntity;
import pl.zzpj.dealmate.userservice.dto.RegisterRequest;
import pl.zzpj.dealmate.userservice.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn(savedUser.getPassword());
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        UserEntity result = userService.registerUser(request);

        // Then
        assertThat(result.getUsername()).isEqualTo(savedUser.getUsername());
        assertThat(result.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(result.getPassword()).isEqualTo(savedUser.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        RegisterRequest request = new RegisterRequest("testUser", "email@example.com", "password");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UserWithLoginExistsException.class)
                .hasMessage("User with username " + request.getUsername() + " already exists!");
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest("testUser", "email@example.com", "password");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UserWithEmailExistsException.class)
                .hasMessage("User with email " + request.getEmail() + " already exists!");
    }

    @Test
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        UserEntity user = new UserEntity("testUser", "email@example.com", "password");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(java.util.Optional.of(user));

        // When
        UserEntity result = userService.getUserByUsername(user.getUsername());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void shouldThrowWhenUserWithUsernameNotFound() {
        // Given
        String username = "nonExistentUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(UserWithLoginDoesntExistException.class)
                .hasMessage("User with username " + username + " doesn't exist!");
    }

    @Test
    void shouldGetUserByEmailSuccessfully() {
        // Given
        UserEntity user = new UserEntity("testUser", "email@example.com", "password");

        // When
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.of(user));

        // Then
        UserEntity result = userService.getUserByEmail(user.getEmail());
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(user.getUsername());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());

    }

    @Test
    void shouldThrowWhenUserWithEmailNotFound() {
        // Given
        String email = "email@example.com";

        // When
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(UserWithEmailDoesntExistException.class)
                .hasMessage("User with email " + email + " doesn't exist!");
    }

    @Test
    void shouldUpdateUserDataSuccessfully() {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest("testUser", "NewFirstName",
                "NewLastName", ECountryCodes.PL);
        UserEntity existingUser = new UserEntity("testUser", "NewFirstName",
                "NewLastName", ECountryCodes.PL);

        // When
        when(userRepository.findByUsername(updateRequest.username())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        UserEntity updatedUser = userService.updateUserData(updateRequest);
        // Then
        assertThat(updatedUser.getUsername()).isEqualTo(updateRequest.username());
        assertThat(updatedUser.getFirstName()).isEqualTo(updateRequest.firstName());
        assertThat(updatedUser.getLastName()).isEqualTo(updateRequest.lastName());
        assertThat(updatedUser.getCountryCode()).isEqualTo(updateRequest.countryCode());

    }

    @Test
    void shouldThrowExceptionWhenNoUsernameInUpdateRequest() {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(null, "NewFirstName",
                "NewLastName", ECountryCodes.PL);

        // When & Then
        assertThatThrownBy(() -> userService.updateUserData(updateRequest))
                .isInstanceOf(NoUsernameInRequest.class)
                .hasMessage("Username is required in the request!");
    }

    @Test
    void shouldThrowExceptionWhenUserWithUsernameNotFoundInUpdate() {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest("nonExistentUser", "NewFirstName",
                "NewLastName", ECountryCodes.PL);

        // When
        when(userRepository.findByUsername(updateRequest.username())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userService.updateUserData(updateRequest))
                .isInstanceOf(UserWithLoginDoesntExistException.class)
                .hasMessage("User with username " + updateRequest.username() + " doesn't exist!");
    }

    @Test
    void shouldUpdateUserDataWithNullFields() {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest("testUser", null, null, null);
        UserEntity existingUser = new UserEntity("testUser", "OldFirstName", "OldLastName", ECountryCodes.PL);

        // When
        when(userRepository.findByUsername(updateRequest.username())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);
        UserEntity updatedUser = userService.updateUserData(updateRequest);

        // Then
        assertThat(updatedUser.getUsername()).isEqualTo(updateRequest.username());
        assertThat(updatedUser.getFirstName()).isEqualTo(existingUser.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(existingUser.getLastName());
        assertThat(updatedUser.getCountryCode()).isEqualTo(existingUser.getCountryCode());
    }
}
