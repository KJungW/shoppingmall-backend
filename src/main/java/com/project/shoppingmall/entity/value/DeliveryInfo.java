package com.project.shoppingmall.entity.value;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeliveryInfo {
  private String senderName;
  private String senderAddress;
  private String senderPostCode;
  private String senderTel;

  public DeliveryInfo(DeliveryDto dto) {
    this.senderName = dto.getSenderName();
    this.senderAddress = dto.getSenderAddress();
    this.senderPostCode = dto.getSenderPostCode();
    this.senderTel = dto.getSenderTel();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    DeliveryInfo deliveryInfo = (DeliveryInfo) object;
    return Objects.equals(senderName, deliveryInfo.senderName)
        && Objects.equals(senderAddress, deliveryInfo.senderAddress)
        && Objects.equals(senderPostCode, deliveryInfo.senderPostCode)
        && Objects.equals(senderTel, deliveryInfo.senderTel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(senderName, senderAddress, senderPostCode, senderTel);
  }
}
