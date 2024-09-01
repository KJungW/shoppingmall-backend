package com.project.shoppingmall.handler.chat;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Component
public class ChatErrorHandler extends StompSubProtocolErrorHandler {
  @Override
  public Message<byte[]> handleClientMessageProcessingError(
      Message<byte[]> clientMessage, Throwable ex) {
    StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
    errorAccessor.setMessage(ex.getCause().getMessage());
    return MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders());
  }
}
