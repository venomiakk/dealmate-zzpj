package pl.zzpj.dealmate.deckservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deck")
public class DeckController {

     //Example endpoint
     @GetMapping("/example")
     public ResponseEntity<String> exampleEndpoint() {
         return ResponseEntity.ok("This is an example endpoint");
     }
}
