package pl.zzpj.dealmate.gameservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.zzpj.dealmate.gameservice.dto.RoomStateUpdateDto;

// "chat-service" to nazwa, pod jaką ChatService będzie zarejestrowany w serwerze Eureka/Consul
// lub po prostu adres URL, jeśli nie używasz service discovery
@FeignClient(name = "chat-service", url = "${chat-service.url}")
public interface ChatServiceClient {

    @PostMapping("/api/chat/system/update")
    void notifyRoomStateChange(@RequestBody RoomStateUpdateDto updateDto);
}