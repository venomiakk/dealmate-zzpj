package pl.zzpj.dealmate.userservice.exception.custom;

public class NoUsernameInRequest extends RuntimeException {
    public NoUsernameInRequest() {
        super("Username is required in the request!");
    }
}
