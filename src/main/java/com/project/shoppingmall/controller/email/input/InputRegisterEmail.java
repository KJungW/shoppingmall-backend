package com.project.shoppingmall.controller.email.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputRegisterEmail {
  @NotNull private Long memberId;

  @NotBlank
  @Length(min = 1, max = 50)
  private String certificationNumber;

  @Email
  @NotBlank
  @Length(min = 1, max = 320)
  private String email;
}
