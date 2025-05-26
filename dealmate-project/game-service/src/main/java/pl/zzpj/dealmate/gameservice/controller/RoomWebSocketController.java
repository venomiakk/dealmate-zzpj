package pl.zzpj.dealmate.gameservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import pl.zzpj.dealmate.gameservice.service.RoomManager;

import java.security.Principal;

@Controller
public class RoomWebSocketController {

    private final RoomManager roomManager;

    public RoomWebSocketController(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @MessageMapping("/join")
    public void handleJoin(Principal principal) {
        String playerId = principal.getName();
        System.out.println("WebSocket join from: " + playerId);
    }
}
