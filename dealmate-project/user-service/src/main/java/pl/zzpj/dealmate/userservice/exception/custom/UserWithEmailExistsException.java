package pl.zzpj.dealmate.userservice.exception.custom;

public class UserWithEmailExistsException extends RuntimeException {
    public UserWithEmailExistsException(String email) {
        super("User with email " + email + " already exists!");
    }

}
