package pl.zzpj.dealmate.gameservice.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.zzpj.dealmate.gameservice.dto.GameHistoryDto;
import pl.zzpj.dealmate.gameservice.model.GameHistory;
import pl.zzpj.dealmate.gameservice.service.GameHistoryService;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import pl.zzpj.dealmate.gameservice.service.GameHistoryGraphService;

@Slf4j
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final GameHistoryService gameHistoryService;
    private final GameHistoryGraphService gameHistoryGraphService;

    @GetMapping("/{playerId}")
    public ResponseEntity<List<GameHistoryDto>> getPlayerHistory(@PathVariable String playerId) {
        List<GameHistory> history = gameHistoryService.getHistoryForPlayer(playerId);
        List<GameHistoryDto> dtos = history.stream()
                .map(h -> new GameHistoryDto(h.getId(), h.getGameType().name(), h.getResult().name(), h.getAmount(), h.getTimestamp()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/generateGraph/{playerId}")
    public ResponseEntity<String> generateGraphFromJson(@PathVariable String playerId) {
        try {
            return ResponseEntity.ok(gameHistoryGraphService.generateGraphFromJson(playerId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Błąd: " + e.getMessage());
        }
    }
}