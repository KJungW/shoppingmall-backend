package com.project.shoppingmall.controller.refund.input;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InputCompleteRefund {
  @NotNull private long refundId;
}
