package pl.zzpj.dealmate.gameservice.game.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import pl.zzpj.dealmate.gameservice.game.dto.GameActionRequest;
import pl.zzpj.dealmate.gameservice.game.dto.PlayerAction;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final RoomManager roomManager;

    @MessageMapping("/game/{roomId}/action")
    public void handleGameAction(@Payload GameActionRequest request,
                                 @DestinationVariable String roomId) {

        String playerName = request.playerId();
        PlayerAction playerAction = request.action();

        if (playerName == null || playerAction == null) {
            log.warn("Received invalid game action request in room {}: {}", roomId, request);
            return;
        }

        log.info("Received action {} from player {} in room {}", playerAction, playerName, roomId);

        roomManager.getRoomById(roomId).ifPresent(room -> {
            room.handlePlayerAction(playerName, playerAction);
        });
    }
}