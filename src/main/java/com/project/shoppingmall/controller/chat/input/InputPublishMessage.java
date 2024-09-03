package com.project.shoppingmall.controller.chat.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputPublishMessage {
  @NotNull private Long chatRoomId;
  @NotBlank private String message;
}
