package com.project.shoppingmall.controller.member.input;

import com.project.shoppingmall.final_value.RegularExpressions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InputRegisterAccount {
  @NotBlank
  @Pattern(regexp = RegularExpressions.Member_ACCOUNT_PATTERN)
  private String accountNumber;
}
