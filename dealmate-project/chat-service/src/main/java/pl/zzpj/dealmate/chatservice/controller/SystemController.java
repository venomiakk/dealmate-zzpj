package pl.zzpj.dealmate.chatservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.zzpj.dealmate.chatservice.dto.ChatMessageDto;
import pl.zzpj.dealmate.chatservice.dto.RoomInfoUpdateDto;
import pl.zzpj.dealmate.chatservice.dto.RoomStateUpdateDto;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class SystemController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/system/update")
    public void handleRoomStateUpdate(@RequestBody RoomStateUpdateDto updateDto) {
        log.info("Received system update for room {}: {}", updateDto.roomId(), updateDto.systemMessage());

        // 1. Wyślij wiadomość systemową na czat
        if (updateDto.systemMessage() != null && !updateDto.systemMessage().isBlank()) {
            ChatMessageDto systemMessage = new ChatMessageDto();
            systemMessage.setSender("System"); // Lub dowolna inna nazwa systemowa
            systemMessage.setContent(updateDto.systemMessage());
            systemMessage.setTimestamp(System.currentTimeMillis());
            messagingTemplate.convertAndSend("/topic/room/" + updateDto.roomId() + "/chat", systemMessage);
        }

        // 2. Wyślij zaktualizowaną listę graczy
        if (updateDto.players() != null) {
            RoomInfoUpdateDto playersUpdate = new RoomInfoUpdateDto(updateDto.players());
            messagingTemplate.convertAndSend("/topic/room/" + updateDto.roomId() + "/players", playersUpdate);
        }
    }
}