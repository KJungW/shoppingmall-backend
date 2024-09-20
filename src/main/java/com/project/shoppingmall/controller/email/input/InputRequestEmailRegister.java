package com.project.shoppingmall.controller.email.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
public class InputRequestEmailRegister {
  @Email
  @NotBlank
  @Length(min = 1, max = 320)
  private String email;
}
