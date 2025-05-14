package pl.zzpj.dealmate.authserver.dto;

public record UserDetailsDto(
        long id,
        String username,
        String password,
        String email,
        String role,
        String firstName,
        String lastName
) {
}
