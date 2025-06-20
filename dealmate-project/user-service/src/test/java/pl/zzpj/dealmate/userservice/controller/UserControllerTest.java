package pl.zzpj.dealmate.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.zzpj.dealmate.userservice.dto.UpdateUserRequest;
import pl.zzpj.dealmate.userservice.exception.custom.*;
import pl.zzpj.dealmate.userservice.model.ECountryCodes;
import pl.zzpj.dealmate.userservice.model.UserEntity;
import pl.zzpj.dealmate.userservice.dto.RegisterRequest;
import pl.zzpj.dealmate.userservice.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("username", "email@example.com",
                "password");

        // When
        when(userService.registerUser(request)).thenReturn(new UserEntity(request.getUsername(), request.getEmail(),
                request.getPassword()));

        // Then
        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

    }

    @Test
    void shouldThrowUserWithLoginExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("username", "email@example.com", "password");

        // When
        when(userService.registerUser(request))
                .thenThrow(new UserWithLoginExistsException(request.getUsername()));

        // Then
        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertTrue(responseBody.contains("User with username " + request.getUsername() + " already exists!"));
                });
    }

    @Test
    void shouldThrowUserWithEmailExists() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("username", "email@example.com", "password");

        // When
        when(userService.registerUser(request))
                .thenThrow(new UserWithEmailExistsException(request.getEmail()));

        // Then
        mockMvc.perform(post("/user/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).contains("User with email " + request.getEmail() + " already exists!");
                });
    }

    @Test
    void shouldReturnUserByUsername() throws Exception {
        // Given
        UserEntity user = new UserEntity("username", "email@example.com", "password");
        when(userService.getUserByUsername(user.getUsername())).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/user/getuser/username/{username}", user.getUsername()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    UserEntity returnedUser = objectMapper.readValue(responseBody, UserEntity.class);
                    assertThat(returnedUser.getUsername()).isEqualTo(user.getUsername());
                    assertThat(returnedUser.getEmail()).isEqualTo(user.getEmail());
                });
    }

    @Test
    void shouldThrowUserWithLoginDoesntExist() throws Exception {
        // Given
        String username = "nonExistentUser";

        // When
        when(userService.getUserByUsername(username))
                .thenThrow(new UserWithLoginDoesntExistException(username));

        // Then
        mockMvc.perform(get("/user/getuser/username/{username}", username))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertTrue(responseBody.contains("User with username " + username + " doesn't exist!"));
                });
    }

    @Test
    void shouldReturnUserByEmail() throws Exception {
        // Given
        UserEntity user = new UserEntity("username", "email@example.com", "password");

        // When
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);

        // Then
        mockMvc.perform(get("/user/getuser/email/{email}", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    UserEntity returnedUser = objectMapper.readValue(responseBody, UserEntity.class);
                    assertThat(returnedUser.getUsername()).isEqualTo(user.getUsername());
                    assertThat(returnedUser.getEmail()).isEqualTo(user.getEmail());
                });
    }

    @Test
    void shouldThrowUserWithEmailDoesntExist() throws Exception {
        // Given
        String email = "email@example.com";

        // When
        when(userService.getUserByEmail(email))
                .thenThrow(new UserWithEmailDoesntExistException(email));

        // Then
        mockMvc.perform(get("/user/getuser/email/{email}", email))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).contains("User with email " + email + " doesn't exist!");
                });
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest("username", "John", "Doe",
                ECountryCodes.PL);
        UserEntity updatedUser = new UserEntity(updateRequest.username(), "John", "Doe", ECountryCodes.PL);
        // When & Then
        when(userService.updateUserData(updateRequest)).thenReturn(updatedUser);
        mockMvc.perform(patch("/user/update/{username}", updateRequest.username())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).isEqualTo("User updated successfully");
                });
    }

    @Test
    void shouldThrowNoUsernameInRequest() throws Exception {
        // Given
        UpdateUserRequest updateRequest = new UpdateUserRequest(null, "John", "Doe", ECountryCodes.PL);

        // When
        when(userService.updateUserData(updateRequest))
                .thenThrow(new NoUsernameInRequest());

        // Then
        mockMvc.perform(patch("/user/update/{username}", "nonExistentUser")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).contains("Username is required in the request!");
                });
    }

    @Test
    void shouldUpdateUserCreditsSuccessfully() throws Exception {
        // Given
        String username = "username";
        Long credits = 100L;
        UserEntity user = new UserEntity(username, "aa", "bb");
        user.setCredits(credits);
        // When
        when(userService.updateUserCredits(username, credits)).thenReturn(user);
        // Then
        mockMvc.perform(post("/user/update/credits/{username}", username)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(credits)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).isEqualTo("User credits updated successfully");
                });
    }

    @Test
    void shouldThowUserWithLoginDoesntExistExceptionWhenUpdatingCredits() throws Exception {
        // Given
        String username = "nonExistentUser";
        Long credits = 100L;

        // When
        when(userService.updateUserCredits(username, credits))
                .thenThrow(new UserWithLoginDoesntExistException(username));

        // Then
        mockMvc.perform(post("/user/update/credits/{username}", username)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(credits)))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    assertThat(responseBody).contains("User with username " + username + " doesn't exist!");
                });
    }
}