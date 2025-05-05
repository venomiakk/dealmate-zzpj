package pl.zzpj.dealmate.userservice.exception;

public class UserWithLoginDoesntExistException extends RuntimeException {
  public UserWithLoginDoesntExistException(String message) {
    super(message);
  }
}
