package pl.zzpj.dealmate.chatservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.warn("Chat-service filter chain initialized");
        // *: Endpoint protection example
        // *: But shouldn't it be in api gateway?
        http
                // * Disable CSRF protection for testing purposes
                // * POST requests are not working with CSRF enabled
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers("api/chat/**").permitAll()
                        .anyRequest().authenticated() // Require authentication for all other requests
                        //.anyRequest().permitAll()
                )
                .oauth2ResourceServer(resource -> resource
                        .jwt(Customizer.withDefaults()));
                //);

        return http.build();
    }

    //@Bean
    //@Order(1)
    //public SecurityFilterChain webSocketSecurityFilterChain(HttpSecurity http) throws Exception {
    //    http
    //            .securityMatcher("/ws-chat/**") // Stosuj ten łańcuch TYLKO do ścieżki WebSocket
    //            .authorizeHttpRequests(authorize -> authorize
    //                    .anyRequest().permitAll() // Zezwól na wszystko w tej ścieżce
    //            )
    //            .csrf(AbstractHttpConfigurer::disable) // Wyłącz CSRF dla WS
    //            .cors(AbstractHttpConfigurer::disable) // Wyłącz CORS (obsługuje go Gateway)
    //            .headers(headers ->
    //                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
    //            );
    //
    //    return http.build();
    //}
    //
    //@Bean
    //@Order(2)
    //public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    //    http
    //            .csrf(AbstractHttpConfigurer::disable)
    //            .cors(AbstractHttpConfigurer::disable)
    //            .authorizeHttpRequests(authorize -> authorize
    //                    .anyRequest().authenticated() // Zabezpiecz wszystkie inne endpointy
    //            )
    //            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    //
    //    return http.build();
    //}

}
