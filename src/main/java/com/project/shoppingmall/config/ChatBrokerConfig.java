package com.project.shoppingmall.config;

import com.project.shoppingmall.handler.chat.ChatErrorHandler;
import com.project.shoppingmall.interceptor.ChatHandShakingProcessInterceptor;
import com.project.shoppingmall.interceptor.ChatMessagingPreProcessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class ChatBrokerConfig implements WebSocketMessageBrokerConfigurer {
  private final ChatHandShakingProcessInterceptor chatHandShakingProcessInterceptor;
  private final ChatMessagingPreProcessInterceptor chatMessagingPreProcessInterceptor;
  private final ChatErrorHandler chatErrorHandler;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .addInterceptors(chatHandShakingProcessInterceptor)
        .setAllowedOrigins("*");
    registry.setErrorHandler(chatErrorHandler);
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/sub");
    registry.setApplicationDestinationPrefixes("/pub");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(chatMessagingPreProcessInterceptor);
  }
}
