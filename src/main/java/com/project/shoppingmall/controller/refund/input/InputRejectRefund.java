package com.project.shoppingmall.controller.refund.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InputRejectRefund {
  @NotNull private long refundId;
  @NotEmpty private String responseMessage;
}
