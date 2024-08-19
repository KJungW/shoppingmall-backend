package com.project.shoppingmall.controller.auth.input;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputLongManager {
  @NotBlank private String serialNumber;
  @NotBlank private String password;
}
