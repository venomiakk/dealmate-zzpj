package pl.zzpj.dealmate.gameservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String playerId; // Login gracza

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EGameType gameType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameResult result;

    @Column(nullable = false)
    private BigDecimal amount; // Wygrana/przegrana kwota

    @CreationTimestamp
    private LocalDateTime timestamp;
}