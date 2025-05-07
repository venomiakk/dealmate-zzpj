package pl.zzpj.dealmate.userservice.exception.custom;

public class UserWithEmailDoesntExistException extends RuntimeException {
    public UserWithEmailDoesntExistException(String email) {
        super(
                "User with email " + email + " doesn't exist!"
        );
    }
}
