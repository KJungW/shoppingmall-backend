package com.project.shoppingmall.controller.refund.input;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InputAcceptRefund {
  @NotNull private long refundId;
}
