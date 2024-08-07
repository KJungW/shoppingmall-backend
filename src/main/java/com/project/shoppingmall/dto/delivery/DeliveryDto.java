package com.project.shoppingmall.dto.delivery;

import com.project.shoppingmall.entity.value.DeliveryInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeliveryDto {
  private String senderName;
  private String senderAddress;
  private String senderPostCode;
  private String senderTel;

  public DeliveryDto(DeliveryInfo deliveryInfo) {
    this.senderName = deliveryInfo.getSenderName();
    this.senderAddress = deliveryInfo.getSenderAddress();
    this.senderPostCode = deliveryInfo.getSenderPostCode();
    this.senderTel = deliveryInfo.getSenderTel();
  }
}
