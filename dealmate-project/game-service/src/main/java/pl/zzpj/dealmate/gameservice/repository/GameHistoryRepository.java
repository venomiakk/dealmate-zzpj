package pl.zzpj.dealmate.gameservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import java.util.List;

public interface GameHistoryRepository extends JpaRepository<GameHistory, Long> {
    List<GameHistory> findByPlayerIdOrderByTimestampDesc(String playerId);
}