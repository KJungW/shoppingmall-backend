package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Getter
@Document(collection = "chatMessage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {
  @Id
  @Field(value = "_id", targetType = FieldType.OBJECT_ID)
  private String chatId;

  @Indexed private Long chatRoomId;
  private Long writerId;
  private String message;

  @Builder
  public ChatMessage(Long chatRoomId, Long writerId, String message) {
    if (chatRoomId == null) throw new ServerLogicError("ChatMessage의 chatRoomId 필드에 빈값이 입력되었습니다.");
    if (writerId == null) throw new ServerLogicError("ChatMessage의 writerId 필드에 빈값이 입력되었습니다.");
    if (message == null || message.isBlank())
      throw new ServerLogicError("ChatMessage의 message 필드에 빈값이 입력되었습니다.");
    this.chatRoomId = chatRoomId;
    this.writerId = writerId;
    this.message = message;
  }
}
