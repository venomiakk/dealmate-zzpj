package pl.zzpj.dealmate.userservice.exception;

public class UserWithLoginExistsException extends RuntimeException {
    public UserWithLoginExistsException(String message) {
        super(message);
    }
}
