package pl.zzpj.dealmate.chatservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import pl.zzpj.dealmate.chatservice.dto.ChatMessageDto;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketControllerTest {
    // Wstrzykujemy losowy port, na którym uruchomił się serwer
    @LocalServerPort
    private int port;

    // Klient STOMP do wysyłania i odbierania wiadomości
    private WebSocketStompClient stompClient;

    // URL do połączenia z naszym serwerem WebSocket
    private String URL;

    // Używamy CompletableFuture do obsługi asynchronicznej odpowiedzi
    private CompletableFuture<ChatMessageDto> completableFuture;

    @BeforeEach
    void setUp() {
        // Przed każdym testem resetujemy CompletableFuture
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/ws-chat";

        // Tworzymy klienta SockJS, ponieważ backend go używa. To zapewnia kompatybilność.
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);
        stompClient = new WebSocketStompClient(sockJsClient);

        // Ustawiamy konwerter wiadomości na JSON (taki sam jak domyślny w Springu)
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void shouldSendMessageAndBroadcastToCorrectTopic() throws Exception {
        // GIVEN (Dane wejściowe)
        String roomId = "test-room-123";
        ChatMessageDto testMessage = new ChatMessageDto();
        testMessage.setSender("TestUser");
        testMessage.setContent("Hello, World!");

        String subscribeUrl = "/topic/room/" + roomId + "/chat";
        String sendUrl = "/app/room/" + roomId + "/chat.sendMessage";

        // ACT (Działanie)
        // Nawiązujemy połączenie z serwerem. Operacja jest asynchroniczna.
        StompSession stompSession = stompClient.connectAsync(URL, new StompSessionHandlerAdapter() {}).get(3, TimeUnit.SECONDS);

        // Subskrybujemy temat, na który kontroler powinien wysłać wiadomość
        stompSession.subscribe(subscribeUrl, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                // Określamy typ DTO, na który ma być zdeserializowana odpowiedź
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // Gdy wiadomość dotrze na subskrybowany temat, kończymy nasz Future
                completableFuture.complete((ChatMessageDto) payload);
            }
        });

        // Wysyłamy wiadomość na endpoint, którego nasłuchuje kontroler
        stompSession.send(sendUrl, testMessage);

        // ASSERT (Sprawdzenie wyników)
        // Czekamy na wiadomość zwrotną (maksymalnie 3 sekundy)
        ChatMessageDto receivedMessage = completableFuture.get(3, TimeUnit.SECONDS);

        // Sprawdzamy, czy otrzymana wiadomość jest poprawna
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getContent()).isEqualTo(testMessage.getContent());
        assertThat(receivedMessage.getSender()).isEqualTo(testMessage.getSender());
        // Sprawdzamy, czy kontroler ustawił timestamp
        assertThat(receivedMessage.getTimestamp()).isNotNull().isPositive();
    }
}