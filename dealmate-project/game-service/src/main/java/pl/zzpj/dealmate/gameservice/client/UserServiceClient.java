package pl.zzpj.dealmate.gameservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.zzpj.dealmate.gameservice.dto.UserDetailsDto;

@FeignClient(name = "userservice")
public interface UserServiceClient {
    @GetMapping("/user/getuser/username/{username}")
    UserDetailsDto getUserByUsername(@PathVariable("username") String username);
}
