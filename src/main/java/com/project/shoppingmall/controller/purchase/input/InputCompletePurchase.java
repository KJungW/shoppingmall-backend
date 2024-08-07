package com.project.shoppingmall.controller.purchase.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputCompletePurchase {
  @NotEmpty
  @JsonProperty("imp_uid")
  private String paymentUid;

  @NotEmpty
  @JsonProperty("merchant_uid")
  private String purchaseUid;

  @JsonProperty("status")
  private String status;

  @JsonProperty("cancellation_id")
  private String cancellationUid;
}
