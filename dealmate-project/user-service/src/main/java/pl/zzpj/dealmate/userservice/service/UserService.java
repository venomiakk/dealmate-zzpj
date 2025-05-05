package pl.zzpj.dealmate.userservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.userservice.exception.UserWithLoginDoesntExistException;
import pl.zzpj.dealmate.userservice.exception.UserWithLoginExistsException;
import pl.zzpj.dealmate.userservice.model.UserEntity;
import pl.zzpj.dealmate.userservice.payload.request.RegisterRequest;
import pl.zzpj.dealmate.userservice.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserWithLoginExistsException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserWithLoginExistsException("Error: Email is already in use!");
        }

        UserEntity user = new UserEntity(registerRequest.getUsername(), registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        return userRepository.save(user);
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithLoginDoesntExistException(
                        "User with username " + username + " does not exist"));
    }
}
