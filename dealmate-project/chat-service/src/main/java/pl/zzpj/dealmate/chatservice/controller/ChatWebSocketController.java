package pl.zzpj.dealmate.chatservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import pl.zzpj.dealmate.chatservice.dto.ChatMessageDto;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/chat.sendMessage")
    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessageDto chatMessage) {
        log.warn("Received message for room {}: {}", roomId, chatMessage);
        chatMessage.setSender(chatMessage.getSender());
        chatMessage.setTimestamp(System.currentTimeMillis());

        log.info("Broadcasting message from {} to room {}: {}", chatMessage.getSender(), roomId, chatMessage.getContent());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", chatMessage);
    }
}