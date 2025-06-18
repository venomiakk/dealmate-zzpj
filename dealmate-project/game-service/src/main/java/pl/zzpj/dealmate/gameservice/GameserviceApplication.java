package pl.zzpj.dealmate.gameservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GameserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameserviceApplication.class, args);
	}

}
