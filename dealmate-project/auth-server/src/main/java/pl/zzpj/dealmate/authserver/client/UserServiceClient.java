package pl.zzpj.dealmate.authserver.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.zzpj.dealmate.authserver.dto.UserDetailsDto;

// ?: FeignConfig may be needed to attach authorization header
@FeignClient(name = "userservice")
public interface UserServiceClient {
    @GetMapping("/user/getuser/username/{username}")
    UserDetailsDto getUserByUsername(@PathVariable("username") String username);
}
