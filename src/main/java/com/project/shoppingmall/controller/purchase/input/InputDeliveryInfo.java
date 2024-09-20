package com.project.shoppingmall.controller.purchase.input;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputDeliveryInfo {
  @NotBlank
  @Length(min = 1, max = 50)
  private String senderName;

  @NotBlank
  @Length(min = 1, max = 80)
  private String senderAddress;

  @NotBlank
  @Length(min = 5, max = 5)
  private String senderPostCode;

  @NotBlank
  @Length(min = 1, max = 50)
  private String senderTel;

  public DeliveryDto makeDeliveryDto() {
    return new DeliveryDto(senderName, senderAddress, senderPostCode, senderTel);
  }
}
