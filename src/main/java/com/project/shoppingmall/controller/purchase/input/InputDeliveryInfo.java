package com.project.shoppingmall.controller.purchase.input;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputDeliveryInfo {
  @NotEmpty private String senderName;
  @NotEmpty private String senderAddress;

  @Size(min = 5, max = 5)
  @NotEmpty
  private String senderPostCode;

  @NotEmpty private String senderTel;

  public DeliveryDto makeDeliveryDto() {
    return new DeliveryDto(senderName, senderAddress, senderPostCode, senderTel);
  }
}
