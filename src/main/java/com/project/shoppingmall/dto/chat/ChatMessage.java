package com.project.shoppingmall.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMessage {
  @NotNull private Long chatRoomId;
  @NotBlank private String message;
}
