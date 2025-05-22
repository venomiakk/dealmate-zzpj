package pl.zzpj.dealmate.aiservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiContriller {
    @GetMapping("/texasHoldem")
    public ResponseEntity registerUser() {
        return ResponseEntity.status(HttpStatus.CREATED).body("lets go");
    }
}
