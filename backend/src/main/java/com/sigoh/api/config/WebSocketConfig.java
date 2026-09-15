package com.sigoh.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Canal de atualizacao em tempo real.
 *
 * O mapa de leitos e visto simultaneamente por varias pessoas; sem um canal
 * de push, cada tela so descobriria uma mudanca ao recarregar (ou por polling,
 * que desperdica requisicoes). O front-end assina "/topic/ocupacao" e recebe
 * um evento a cada mudanca de status.
 *
 * O broker embutido (simple broker) mantem as assinaturas em memoria, o que
 * atende uma unica instancia da aplicacao. Escalar para varias instancias
 * exigiria um broker externo (RabbitMQ/ActiveMQ) - decisao adiada ate existir
 * essa necessidade.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
}
