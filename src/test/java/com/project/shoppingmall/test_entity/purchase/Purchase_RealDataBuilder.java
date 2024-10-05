package com.project.shoppingmall.test_entity.purchase;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.PurchaseStateType;
import java.util.List;
import java.util.UUID;

public class Purchase_RealDataBuilder {
  public static Purchase makePurchase(
      Member buyer, List<PurchaseItem> purchaseItems, PurchaseStateType state) {
    int totalPrice = purchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();
    DeliveryInfo deliveryInfo =
        new DeliveryInfo(buyer.getNickName(), "test address", "11011", "101-0000-0000");
    Purchase purchase =
        Purchase.builder()
            .buyerId(buyer.getId())
            .purchaseItems(purchaseItems)
            .purchaseUid("test-purchaseUid" + UUID.randomUUID())
            .purchaseTitle("test purchase")
            .deliveryInfo(deliveryInfo)
            .totalPrice(totalPrice)
            .build();
    updatePurchaseState(purchase, state);
    return purchase;
  }

  private static void updatePurchaseState(Purchase purchase, PurchaseStateType state) {
    switch (state) {
      case READY -> {}
      case FAIL -> purchase.convertStateToFail(UUID.randomUUID().toString());
      case DETECT_PRICE_TAMPERING -> purchase.convertStateToDetectPriceTampering(
          UUID.randomUUID().toString());
      case COMPLETE -> purchase.convertStateToComplete(UUID.randomUUID().toString());
      default -> throw new ServerLogicError("처리되지 않은 PurchaseStateType가 존재합니다");
    }
  }
}
