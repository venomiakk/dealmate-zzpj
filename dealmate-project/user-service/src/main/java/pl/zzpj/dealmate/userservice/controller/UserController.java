package pl.zzpj.dealmate.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.userservice.exception.UserWithEmailExistsException;
import pl.zzpj.dealmate.userservice.exception.UserWithLoginDoesntExistException;
import pl.zzpj.dealmate.userservice.exception.UserWithLoginExistsException;
import pl.zzpj.dealmate.userservice.model.UserEntity;
import pl.zzpj.dealmate.userservice.payload.request.RegisterRequest;
import pl.zzpj.dealmate.userservice.service.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("User service is running");
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody RegisterRequest registerRequest) {
        UserEntity user = userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    //TODO: Handle exceptions globally using ie. @RestControllerAdvice

    @ExceptionHandler(UserWithLoginExistsException.class)
    public ResponseEntity<String> handleUserWithLoginExistsException(UserWithLoginExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UserWithEmailExistsException.class)
    public ResponseEntity<String> handleUserWithEmailExistsException(UserWithEmailExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @GetMapping("/getuser/{username}")
    public ResponseEntity<UserEntity> getUserByUsername(@PathVariable String username) {
        UserEntity user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @ExceptionHandler(UserWithLoginDoesntExistException.class)
    public ResponseEntity<String> handleUserWithLoginDoesntExistException(UserWithLoginDoesntExistException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

