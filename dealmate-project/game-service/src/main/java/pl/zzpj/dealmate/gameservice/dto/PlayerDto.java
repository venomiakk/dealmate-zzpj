package pl.zzpj.dealmate.gameservice.dto;

public record PlayerDto(String login, Long credits) {
    public String getLogin() {
        return login;
    }
    public Long getCredits() {
        return credits;
    }
}