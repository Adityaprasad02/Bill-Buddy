package com.company.Bill_Bridge.configuration;


import jakarta.persistence.Entity;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class SocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/billbuddy")
                .setAllowedOriginPatterns("*")
                .addInterceptors() // implement while jwt auth to verify jwt
                .withSockJS() ;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic" , "/queue") ;
        registry.setApplicationDestinationPrefixes("/app") ;
        registry.setUserDestinationPrefix("/user") ;
    }
}
