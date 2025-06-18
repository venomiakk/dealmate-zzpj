package pl.zzpj.dealmate.userservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.userservice.dto.UpdateUserRequest;
import pl.zzpj.dealmate.userservice.exception.custom.*;
import pl.zzpj.dealmate.userservice.model.UserEntity;
import pl.zzpj.dealmate.userservice.dto.RegisterRequest;
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

    public UserEntity updateUserData(UpdateUserRequest updateUserRequest){
        if (updateUserRequest.username() == null){
            throw new NoUsernameInRequest();
        }
        UserEntity user = userRepository.findByUsername(updateUserRequest.username())
                .orElseThrow(() -> new UserWithLoginDoesntExistException(updateUserRequest.username()));
        if (updateUserRequest.firstName() != null) {
            user.setFirstName(updateUserRequest.firstName());
        }
        if (updateUserRequest.lastName() != null) {
            user.setLastName(updateUserRequest.lastName());
        }
        if (updateUserRequest.countryCode() != null) {
            user.setCountryCode(updateUserRequest.countryCode());
        }
        return userRepository.save(user);
    }

    public UserEntity updateUserCredits(String username, Long credits) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserWithLoginDoesntExistException(username));
        if (user.getCredits() == null) {
            user.setCredits(0L);
        }
        //?: This might need additional validation
        user.setCredits(user.getCredits() + credits);
        return userRepository.save(user);
    }
}
