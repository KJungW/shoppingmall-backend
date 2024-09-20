package com.project.shoppingmall.controller.chat.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputUpdateChatReadRecord {
  @NotNull private Long chatRoomId;

  @NotBlank
  @Length(min = 1, max = 50)
  private String chatMessageId;
}
