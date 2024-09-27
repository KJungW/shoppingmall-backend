package com.project.shoppingmall.controller.refund.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputRequestRefund {
  @NotNull private long purchaseItemId;

  @NotBlank
  @Length(min = 1, max = 100)
  private String requestTitle;

  @NotEmpty
  @Length(min = 1, max = 1000)
  private String requestContent;
}
