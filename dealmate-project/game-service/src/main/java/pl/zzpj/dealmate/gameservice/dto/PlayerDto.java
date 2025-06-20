package pl.zzpj.dealmate.gameservice.dto;

// DTO do przesyłania info o graczu z kredytami
public record PlayerDto(String login, Long credits) {
    public String getLogin() {
        return login;
    }
    public Long getCredits() {
        return credits;
    }
}