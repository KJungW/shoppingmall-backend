package com.project.shoppingmall.controller.refund.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InputAcceptRefund {
  @NotNull private long refundId;

  @NotBlank
  @Length(min = 1, max = 1000)
  private String responseMessage;
}
