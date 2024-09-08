package com.project.shoppingmall.controller.member.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputRequestSignup {
  @NotBlank @Email private String email;
  @NotBlank private String password;

  @NotBlank
  @Length(min = 3, max = 30)
  private String nickName;
}
