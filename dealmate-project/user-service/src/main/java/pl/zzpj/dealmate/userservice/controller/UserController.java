package pl.zzpj.dealmate.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.userservice.model.UserEntity;
import pl.zzpj.dealmate.userservice.payload.request.RegisterRequest;
import pl.zzpj.dealmate.userservice.service.UserService;

import java.util.Enumeration;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("authtest")
    public ResponseEntity<String> authTest(Authentication authentication, HttpServletRequest request) {
        // Logowanie nagłówków - pomocne przy debugowaniu
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headers = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
        }

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication object is null\nHeaders:\n" + headers.toString());
        }

        try {
            String className = authentication.getClass().getName();

            if (authentication instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
                String username = authentication.getName();
                String jwtString = jwtAuthenticationToken.getToken().getTokenValue();

                return ResponseEntity.ok("Success! Hi " + username + ", jwt: " + jwtString);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication is not JwtAuthenticationToken but: " + className);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage() + "\nHeaders:\n" + headers.toString());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody RegisterRequest registerRequest) {
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
}

