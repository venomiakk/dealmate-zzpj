package pl.zzpj.dealmate.gameservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.gameservice.dto.GameHistoryDto;
import pl.zzpj.dealmate.gameservice.model.GameHistory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GameHistoryGraphService {

    private final GameHistoryService gameHistoryService;


    public String generateGraphFromJson(String playerId) {
        try {
            List<GameHistory> history = gameHistoryService.getHistoryForPlayer(playerId);
            List<GameHistoryDto> dtos = history.stream()
                    .map(h -> new GameHistoryDto(
                            h.getId(),
                            h.getGameType().name(),
                            h.getResult().name(),
                            h.getAmount(),
                            h.getTimestamp()
                    )).toList();

            // Zapisz dane do tymczasowego pliku JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            File tempJsonFile = File.createTempFile("game_history", ".json");
            mapper.writeValue(tempJsonFile, dtos);

            String pythonExe = "python"; // lub "python" jeśli w PATH
            String scriptPath = "dealmate-project/game-service/src/main/resources/python/plot_generator.py";

            ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath, tempJsonFile.getAbsolutePath());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                process.destroy();
                throw new RuntimeException("Proces Pythona przekroczył limit czasu.");
            }

            int exitCode = process.exitValue();
            tempJsonFile.delete(); // usuń tymczasowy plik po zakończeniu


            return output.toString();

        } catch (Exception e) {
            return e.toString();
        }

    }
}
