package com.project.shoppingmall.dto.chat;

import com.project.shoppingmall.entity.ChatMessage;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessageDto {
  private String chatId;
  private Long chatRoomId;
  private Long writerId;
  private String message;
  private LocalDateTime createDate;

  public ChatMessageDto(ChatMessage chatMessage) {
    this.chatId = chatMessage.getId().toString();
    this.chatRoomId = chatMessage.getChatRoomId();
    this.writerId = chatMessage.getWriterId();
    this.message = chatMessage.getMessage();
    this.createDate = chatMessage.getCreateDate();
  }
}
