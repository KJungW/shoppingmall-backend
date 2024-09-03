package com.project.shoppingmall.controller.chat.input;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InputMakeChatRoom {
  @NotNull private Long productId;
}
