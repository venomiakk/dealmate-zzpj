package pl.zzpj.dealmate.gameservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.zzpj.dealmate.gameservice.dto.RoomStateUpdateDto;

@FeignClient(name = "chatservice")
public interface ChatServiceClient {

    @PostMapping("/api/chat/system/update")
    void notifyRoomStateChange(@RequestBody RoomStateUpdateDto updateDto);
}