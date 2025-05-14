package pl.zzpj.dealmate.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.authserver.client.UserServiceClient;
import pl.zzpj.dealmate.authserver.dto.UserDetailsDto;
import pl.zzpj.dealmate.authserver.exception.LoadUserByUsernameException;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    public CustomUserDetailsService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("Attempting to load user by username: {}", username);
        try {
            UserDetailsDto userDetailsDto = userServiceClient.getUserByUsername(username);
            log.debug("User details retrieved: {}", userDetailsDto);
            return User
                    .withUsername(userDetailsDto.username())
                    .password(userDetailsDto.password())
                    .roles(userDetailsDto.role().replaceFirst("^ROLE_", ""))
                    .build();
        } catch (Exception e) {
            log.error("Error loading user by username: {}", username, e);
            throw new LoadUserByUsernameException(username, e);
        }
    }
}
