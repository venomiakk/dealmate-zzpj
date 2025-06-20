package pl.zzpj.dealmate.gameservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zzpj.dealmate.gameservice.client.UserServiceClient;
// import pl.zzpj.dealmate.gameservice.dto.UpdateCreditsRequest; // Ten import nie jest już potrzebny
import pl.zzpj.dealmate.gameservice.model.EGameType;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.model.GameResult;
import pl.zzpj.dealmate.gameservice.repository.GameHistoryRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameHistoryService {

    private final GameHistoryRepository gameHistoryRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public void recordGameResults(String playerId, GameResult result, BigDecimal entryFee) {
        BigDecimal amountChange = switch (result) {
            case WIN -> entryFee;
            case BLACKJACK_WIN -> entryFee.multiply(new BigDecimal("1.5"));
            case LOSS -> entryFee.negate();
            case PUSH -> BigDecimal.ZERO;
        };

        // 1. Zapisz historię gry
        GameHistory history = GameHistory.builder()
                .playerId(playerId)
                .gameType(EGameType.BLACKJACK)
                .result(result)
                .amount(amountChange)
                .build();
        gameHistoryRepository.save(history);

        // 2. Zaktualizuj kredyty użytkownika
        // ZMIANA: Wywołujemy nową wersję metody z dwoma argumentami.
        // Konwertujemy BigDecimal na Long, ponieważ tego oczekuje Twój endpoint.
        if (amountChange.compareTo(BigDecimal.ZERO) != 0) {
            userServiceClient.updateUserCredits(playerId, amountChange.longValue());
        }
    }

    public List<GameHistory> getHistoryForPlayer(String playerId) {
        return gameHistoryRepository.findByPlayerIdOrderByTimestampDesc(playerId);
    }
}