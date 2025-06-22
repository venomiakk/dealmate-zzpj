package pl.zzpj.dealmate.gameservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.zzpj.dealmate.gameservice.dto.GameHistoryDto;
import pl.zzpj.dealmate.gameservice.model.GameHistory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Slf4j
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

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            File secureTempDir = new File("dealmate-project/game-service/tmp");
            if (!secureTempDir.exists() && !secureTempDir.mkdirs()) {
                log.error("Nie udało się utworzyć bezpiecznego katalogu tymczasowego: " + secureTempDir.getAbsolutePath());
                throw new RuntimeException("Błąd tworzenia katalogu tymczasowego");
            }

            File tempJsonFile = File.createTempFile("game_history", ".json", secureTempDir);
            boolean readableSet = tempJsonFile.setReadable(false, false);
            if (!readableSet) {
                log.warn("Nie udało się ustawić uprawnień do odczytu dla pliku: " + tempJsonFile.getAbsolutePath());
            }
            boolean writableSet = tempJsonFile.setWritable(true, true);
            if (!writableSet) {
                log.warn("Nie udało się ustawić uprawnień do zapisu dla pliku: " + tempJsonFile.getAbsolutePath());
            }
            boolean executableSet = tempJsonFile.setExecutable(false, false);
            if (!executableSet) {
                log.warn("Nie udało się ustawić uprawnień do wykonywania dla pliku: " + tempJsonFile.getAbsolutePath());
            }
            tempJsonFile.deleteOnExit();
            mapper.writeValue(tempJsonFile, dtos);

            String pythonExe = "python";
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
            boolean deleted = tempJsonFile.delete();
            if (!deleted) {
                // Możesz użyć loggera, np. log.warn(...)
                log.warn("Nie udało się usunąć pliku tymczasowego: " + tempJsonFile.getAbsolutePath());
            }
        
            return output.toString();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Wątek został przerwany podczas generowania wykresu.", e);
        } catch (Exception e) {
            return e.toString();
        }
    }
}
