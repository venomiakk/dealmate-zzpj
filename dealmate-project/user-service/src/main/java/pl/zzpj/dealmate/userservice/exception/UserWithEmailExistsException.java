package pl.zzpj.dealmate.userservice.exception;

public class UserWithEmailExistsException extends RuntimeException {
    public UserWithEmailExistsException(String message) {
        super(message);
    }
}
