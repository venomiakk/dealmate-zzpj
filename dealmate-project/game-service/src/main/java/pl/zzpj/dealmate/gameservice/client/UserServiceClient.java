package pl.zzpj.dealmate.gameservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import pl.zzpj.dealmate.gameservice.dto.UpdateCreditsRequest;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;

@FeignClient(name = "userservice")
public interface UserServiceClient {
    @GetMapping("/user/getuser/username/{username}")
    UserDetailsDto getUserByUsername(@PathVariable("username") String username);

    @PostMapping("/user/update/credits/{username}")
    void updateUserCredits(@PathVariable("username") String username, @RequestBody Long credits);

}