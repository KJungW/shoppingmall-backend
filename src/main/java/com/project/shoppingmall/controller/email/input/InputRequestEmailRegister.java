package com.project.shoppingmall.controller.email.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InputRequestEmailRegister {
  @NotBlank @Email private String email;
}
