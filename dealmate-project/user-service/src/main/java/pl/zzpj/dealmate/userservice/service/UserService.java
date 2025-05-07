package pl.zzpj.dealmate.userservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.userservice.exception.custom.UserWithEmailDoesntExistException;
import pl.zzpj.dealmate.userservice.exception.custom.UserWithEmailExistsException;
import pl.zzpj.dealmate.userservice.exception.custom.UserWithLoginDoesntExistException;
import pl.zzpj.dealmate.userservice.exception.custom.UserWithLoginExistsException;
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
            throw new UserWithLoginExistsException(registerRequest.getUsername());
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserWithEmailExistsException(registerRequest.getEmail());
        }

        UserEntity user = new UserEntity(registerRequest.getUsername(), registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        return userRepository.save(user);
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithLoginDoesntExistException(username));
    }

    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserWithEmailDoesntExistException(email));
    }
}
