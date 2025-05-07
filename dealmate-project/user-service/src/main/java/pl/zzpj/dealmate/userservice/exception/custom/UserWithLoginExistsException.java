package pl.zzpj.dealmate.userservice.exception.custom;

public class UserWithLoginExistsException extends RuntimeException {
    public UserWithLoginExistsException(String username) {
        super("User with username " + username + " already exists!");
    }
}
