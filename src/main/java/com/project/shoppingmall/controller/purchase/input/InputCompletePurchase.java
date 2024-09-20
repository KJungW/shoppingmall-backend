package com.project.shoppingmall.controller.purchase.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputCompletePurchase {
  @NotBlank
  @Length(min = 1, max = 100)
  @JsonProperty("imp_uid")
  private String paymentUid;

  @NotBlank
  @Length(min = 1, max = 100)
  @JsonProperty("merchant_uid")
  private String purchaseUid;

  @JsonProperty("status")
  private String status;

  @JsonProperty("cancellation_id")
  private String cancellationUid;
}
