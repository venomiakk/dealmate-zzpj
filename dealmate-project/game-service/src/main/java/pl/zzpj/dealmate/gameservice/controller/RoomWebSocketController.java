//package pl.zzpj.dealmate.gameservice.controller;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Controller;
//import pl.zzpj.dealmate.gameservice.dto.ChatMessage; // New ChatMessage DTO
//import pl.zzpj.dealmate.gameservice.model.GameRoom;
//import pl.zzpj.dealmate.gameservice.service.RoomManager;
//
//import java.security.Principal;
//
//@Slf4j // Add Slf4j annotation
//@Controller
//public class RoomWebSocketController {
//
//    private final RoomManager roomManager;
//
//    public RoomWebSocketController(RoomManager roomManager) {
//        this.roomManager = roomManager;
//    }
//
//    // This mapping now handles sending chat messages to a specific room
//// WebSocket endpoint: /app/room/{roomId}/chat.sendMessage
//// Messages will be broadcast to /topic/room/{roomId}/chat
//    @MessageMapping("/room/{roomId}/chat.sendMessage")
//    public void sendMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage, Principal principal) {
//        String sender = principal.getName();
//        log.info("Received chat message from {} for room {}: {}", sender, roomId, chatMessage.getContent());
//
//        roomManager.getRoomById(roomId).ifPresent(room -> {
//// Set the sender of the message to the authenticated user's name
//            chatMessage.setSender(sender);
//            chatMessage.setRoomId(roomId); // Ensure room ID is set on message
//            room.sendChatMessage(chatMessage);
//        });
//    }
//
//    // You can keep a general join message mapping if needed, but specific room joins
//// are better handled via HTTP POST and then WebSocket updates from GameRoom.
//    @MessageMapping("/join")
//    public void handleJoin(Principal principal) {
//        String playerId = principal.getName();
//        log.info("General WebSocket connection established from: " + playerId);
//// This is more for general connection status than specific room actions.
//    }
//}