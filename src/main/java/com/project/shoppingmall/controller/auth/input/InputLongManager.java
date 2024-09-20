package com.project.shoppingmall.controller.auth.input;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputLongManager {
  @NotBlank
  @Length(min = 5, max = 30)
  private String serialNumber;

  @NotBlank
  @Length(min = 5, max = 30)
  private String password;
}
