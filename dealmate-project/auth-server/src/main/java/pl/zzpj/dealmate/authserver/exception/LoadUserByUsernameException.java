package pl.zzpj.dealmate.authserver.exception;

public class LoadUserByUsernameException extends RuntimeException {
    public LoadUserByUsernameException(String username, Throwable cause) {
        super("User with username " + username + " not found", cause);
    }
}
