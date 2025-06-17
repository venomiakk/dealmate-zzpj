package pl.zzpj.dealmate.userservice.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.zzpj.dealmate.userservice.exception.custom.*;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserWithLoginExistsException.class)
    public ResponseEntity<String> handleUserWithLoginExistsException(UserWithLoginExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UserWithEmailExistsException.class)
    public ResponseEntity<String> handleUserWithEmailExistsException(UserWithEmailExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UserWithLoginDoesntExistException.class)
    public ResponseEntity<String> handleUserWithLoginDoesntExistException(UserWithLoginDoesntExistException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserWithEmailDoesntExistException.class)
    public ResponseEntity<String> handleUserWithEmailDoesntExistException(UserWithEmailDoesntExistException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(NoUsernameInRequest.class)
    public ResponseEntity<String> handleNoUsernameInRequestException(NoUsernameInRequest ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
