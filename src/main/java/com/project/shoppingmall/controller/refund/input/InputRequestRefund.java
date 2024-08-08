package com.project.shoppingmall.controller.refund.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputRequestRefund {
  @NotNull private long purchaseItemId;
  @NotEmpty private String requestTitle;
  @NotEmpty private String requestContent;
}
