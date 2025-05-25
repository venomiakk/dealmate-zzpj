package pl.zzpj.dealmate.gameservice.temporary;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/authtest")
    public ResponseEntity<String> authTest(Authentication authentication, HttpServletRequest request) {
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

            if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
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

    @GetMapping("/headers")
    public ResponseEntity<String> getHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headers = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
        }
        return ResponseEntity.ok(headers.toString());
    }
}
