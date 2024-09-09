package com.project.shoppingmall.controller.member.input;

import com.project.shoppingmall.final_value.RegularExpressions;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputRequestSignup {
  @NotBlank @Email private String email;

  @NotBlank
  @Pattern(regexp = RegularExpressions.MEMBER_PASSWORD_PATTERN)
  private String password;

  @NotBlank
  @Length(min = 3, max = 30)
  private String nickName;
}
