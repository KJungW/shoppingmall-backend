package com.project.shoppingmall.controller.email.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputRegisterEmail {
  @NotNull private Long memberId;
  @NotBlank private String certificationNumber;
  @Email @NotBlank private String email;
}
