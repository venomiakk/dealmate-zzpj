package pl.zzpj.dealmate.authserver.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pl.zzpj.dealmate.authserver.service.CustomUserDetailsService;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
public class SecurityConfig {

    @Value("${gateway.client.id}")
    private String gatewayClientID;
    @Value("${gateway.client.host.url}")
    private String gatewayClientHostUrl;
    @Value("${gateway.client.secret}")
    private String gatewayClientSecret;
    @Value("${public.client.host.url}")
    private String publicClientHostUrl;
    @Value("${public.client.id}")
    private String publicClientID;

    private final Oauth2AccessTokenCustomizer oauth2AccessTokenCustomizer;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(Oauth2AccessTokenCustomizer oauth2AccessTokenCustomizer,
                          CustomUserDetailsService customUserDetailsService) {
        this.oauth2AccessTokenCustomizer = oauth2AccessTokenCustomizer;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        // * API Gateway Client
        RegisteredClient gatewayClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(gatewayClientID)
                .clientSecret(passwordEncoder().encode(gatewayClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(gatewayClientHostUrl + "/login/oauth2/code/" + gatewayClientID)
                .postLogoutRedirectUri(gatewayClientHostUrl + "/logout")
                .scope(OidcScopes.OPENID)
                .scope("profile")
                .scope("email")
                .build();

        // * Public Client (SPA)
        RegisteredClient publicWebClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId(publicClientID)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // Public client does not use client secret
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(publicClientHostUrl + "/callback")
                .postLogoutRedirectUri(publicClientHostUrl + "/")
                .scope(OidcScopes.OPENID)
                .scope("profile")
                .scope("email")
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .tokenSettings(
                        TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .reuseRefreshTokens(false)
                                .build()
                )
                .build();
        return new InMemoryRegisteredClientRepository(gatewayClient, publicWebClient);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .cors(Customizer.withDefaults())
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, authorizationServer ->
                        authorizationServer
                                .oidc(Customizer.withDefaults())
                                .clientAuthentication(clientAuthenticationConfigurer ->
                                        clientAuthenticationConfigurer
                                                .authenticationConverter(new PublicClientRefreshTokenAuthenticationConverter())
                                                .authenticationProvider(
                                                        new PublicClientRefreshTokenAuthenticationProvider(
                                                                registeredClientRepository(),
                                                                new InMemoryOAuth2AuthorizationService()
                                                        )
                                                )
                                )
                )
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        http    // If any errors occur redirect user to login page
                // ?: Should custom cors configuration be applied here?
                .exceptionHandling(exceptions ->
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                // enable auth server to accept JWT for endpoints such as /userinfo
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(2) // security filter chain for the rest of your application and any custom endpoints you may have
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults()) // Enable form login
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        log.info("SecurityConfig1: Configuring AuthenticationManager");
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        log.info("SecuriityConfig2: AuthenticationManager configured with CustomUserDetailsService");
        return authenticationManagerBuilder.build();
    }

    // * CORS configuration to allow requests from the public client (SPA)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        //config.addAllowedOrigin(publicClientHostUrl);
        //config.setAllowedOrigins(List.of(publicClientHostUrl, gatewayClientHostUrl));
        config.setAllowedOriginPatterns(List.of("*"));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    //@Bean
    //OAuth2TokenGenerator<OAuth2Token> tokenGenerator(JWKSource<SecurityContext> jwkSource) {
    //    JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
    //    JwtGenerator jwtAccessTokenGenerator = new JwtGenerator(jwtEncoder);
    //    jwtAccessTokenGenerator.setJwtCustomizer(oauth2AccessTokenCustomizer);
    //
    //    return new DelegatingOAuth2TokenGenerator(jwtAccessTokenGenerator);
    //}

    @Bean
    OAuth2TokenGenerator<OAuth2Token> tokenGenerator(JWKSource<SecurityContext> jwkSource){
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwtAccessTokenGenerator = new JwtGenerator(jwtEncoder);
        jwtAccessTokenGenerator.setJwtCustomizer(oauth2AccessTokenCustomizer);

        return new DelegatingOAuth2TokenGenerator(
                jwtAccessTokenGenerator,
                new OAuth2PublicClientRefreshTokenGenerator() // add customized refresh token generator
        );
    }

}
