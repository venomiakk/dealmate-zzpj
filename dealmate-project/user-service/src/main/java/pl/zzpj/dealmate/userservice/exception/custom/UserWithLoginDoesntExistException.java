package pl.zzpj.dealmate.userservice.exception.custom;

public class UserWithLoginDoesntExistException extends RuntimeException {
  public UserWithLoginDoesntExistException(String username) {
    super("User with username " + username + " doesn't exist!");
  }
}
