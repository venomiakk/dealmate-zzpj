package pl.zzpj.dealmate.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    //@Bean
    //public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    //    http
    //            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    //            .csrf(ServerHttpSecurity.CsrfSpec::disable)
    //            .authorizeExchange(exchange -> exchange
    //                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
    //                    .pathMatchers("/userservice/user/register").permitAll()
    //                    .pathMatchers("/ws-chat/**").permitAll()
    //                    .anyExchange().authenticated()
    //            )
    //            .oauth2ResourceServer(oauth2 ->
    //                oauth2.jwt(Customizer.withDefaults()));
    //
    //
    //    return http.build();
    //}

    @Bean
    @Order(1)
    public SecurityWebFilterChain webSocketCorsBypassFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/ws-chat/**")) // Stosuj tylko do tej ścieżki
                .authorizeExchange(exchange -> exchange
                        .anyExchange().permitAll() // Zezwalamy na publiczny dostęp
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable); // <--- WAŻNE: Wyłączamy CORS dla tego łańcucha

        return http.build();
    }

    /**
     * Domyślny łańcuch o niższym priorytecie (Order 2) dla całego pozostałego API.
     * Ten łańcuch WŁĄCZA CORS i zabezpiecza wszystkie inne endpointy.
     */
    @Bean
    @Order(2)
    public SecurityWebFilterChain apiSecurityFilterChain(ServerHttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <--- WAŻNE: Włączamy CORS tutaj
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/userservice/user/register").permitAll()
                        // Reszta wymaga uwierzytelnienia
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        //configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Origin", "Accept"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}