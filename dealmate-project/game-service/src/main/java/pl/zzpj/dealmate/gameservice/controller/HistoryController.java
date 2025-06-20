package pl.zzpj.dealmate.gameservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.zzpj.dealmate.gameservice.dto.GameHistoryDto;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final GameHistoryService gameHistoryService;

    @GetMapping("/{playerId}")
    public ResponseEntity<List<GameHistoryDto>> getPlayerHistory(@PathVariable String playerId) {
        List<GameHistory> history = gameHistoryService.getHistoryForPlayer(playerId);
        List<GameHistoryDto> dtos = history.stream()
                .map(h -> new GameHistoryDto(h.getId(), h.getGameType().name(), h.getResult().name(), h.getAmount(), h.getTimestamp()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}