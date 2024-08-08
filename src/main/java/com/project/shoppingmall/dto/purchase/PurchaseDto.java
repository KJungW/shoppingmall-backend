package com.project.shoppingmall.dto.purchase;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.type.PurchaseStateType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseDto {
  private long purchaseId;
  private long buyerId;
  private PurchaseStateType state;
  private String purchaseTitle;
  private DeliveryDto deliveryInfo;
  private int totalPrice;
  private LocalDateTime dateTime;
  private List<PurchaseItemDto> purchaseItems;

  public PurchaseDto(Purchase purchase) {
    this.purchaseId = purchase.getId();
    this.buyerId = purchase.getBuyer().getId();
    this.state = purchase.getState();
    this.purchaseTitle = purchase.getPurchaseTitle();
    this.deliveryInfo = new DeliveryDto(purchase.getDeliveryInfo());
    this.totalPrice = purchase.getTotalPrice();
    this.dateTime = purchase.getCreateDate();
    this.purchaseItems = purchase.getPurchaseItems().stream().map(PurchaseItemDto::new).toList();
  }
}
