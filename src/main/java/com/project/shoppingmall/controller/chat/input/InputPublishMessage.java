package com.project.shoppingmall.controller.chat.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputPublishMessage {
  @NotNull private Long chatRoomId;

  @NotBlank
  @Length(min = 1, max = 500)
  private String message;
}
