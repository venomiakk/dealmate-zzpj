package pl.zzpj.dealmate.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.userservice.dto.UpdateUserRequest;
import pl.zzpj.dealmate.userservice.model.UserEntity;
import pl.zzpj.dealmate.userservice.dto.RegisterRequest;
import pl.zzpj.dealmate.userservice.service.UserService;


@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody RegisterRequest registerRequest) {
        log.debug("Registering user with request: {}", registerRequest);
        // TODO: Add URI builder
        UserEntity user = userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/getuser/username/{username}")
    public ResponseEntity<UserEntity> getUserByUsername(@PathVariable String username) {
        UserEntity user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/getuser/email/{email}")
    public ResponseEntity<UserEntity> getUserByEmail(@PathVariable String email) {
        UserEntity user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/update/{username}")
    public ResponseEntity<String> updateUser(@PathVariable String username,
                                             @RequestBody UpdateUserRequest updateUserRequest) {
        log.info("Updating user: {}", updateUserRequest);
        UserEntity newUser = userService.updateUserData(updateUserRequest);
        log.info("User updated: {}", newUser);
        return ResponseEntity.ok("User updated successfully");
    }

    @PostMapping("/update/credits/{username}")
    public ResponseEntity<String> updateUserCredits(@PathVariable String username, @RequestBody Long credits) {
        log.info("Updating user credits for {}: {}", username, credits);
        UserEntity user = userService.updateUserCredits(username, credits);
        log.info("User credits updated: {}", user);
        return ResponseEntity.ok("User credits updated successfully");
    }
}

