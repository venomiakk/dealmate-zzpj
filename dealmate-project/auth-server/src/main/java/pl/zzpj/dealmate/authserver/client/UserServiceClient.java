package pl.zzpj.dealmate.authserver.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.zzpj.dealmate.authserver.dto.UserDetailsDto;

@FeignClient(name = "userservice", url = "http://user-service:8080")
public interface UserServiceClient {
    @GetMapping("/user/getuser/username/{username}")
    UserDetailsDto getUserByUsername(@PathVariable("username") String username);
}
