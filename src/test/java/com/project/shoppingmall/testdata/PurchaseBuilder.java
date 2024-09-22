package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.PurchaseStateType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class PurchaseBuilder {

  public static Purchase.PurchaseBuilder fullData() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", 10L);

    DeliveryInfo givenDeliveryInfo =
        new DeliveryInfo("testSenderName", "testAddress", "01010", "000-0000-0000");

    List<PurchaseItem> purchaseItems = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      PurchaseItem item = PurchaseItemBuilder.fullData().build();
      ReflectionTestUtils.setField(item, "finalPrice", 10000);
      purchaseItems.add(item);
    }

    return Purchase.builder()
        .buyerId(givenMember.getId())
        .purchaseItems(purchaseItems)
        .purchaseUid("testPurchaseUid1234")
        .purchaseTitle("testPurchaseTitle")
        .deliveryInfo(givenDeliveryInfo)
        .totalPrice(30000);
  }

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

  public static Purchase makePurchase(
      long id, Member buyer, List<PurchaseItem> purchaseItems, PurchaseStateType state) {
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
    ReflectionTestUtils.setField(purchase, "id", id);
    updatePurchaseState(purchase, state);
    return purchase;
  }

  public static Purchase makPurchase(
      long id, List<PurchaseItem> purchaseItems, PurchaseStateType state) {
    Member buyer = MemberBuilder.makeMember(30L, LoginType.NAVER);
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
    ReflectionTestUtils.setField(purchase, "id", id);
    updatePurchaseState(purchase, state);
    return purchase;
  }

  public static Purchase makPurchase(long id, List<PurchaseItem> purchaseItems, Member buyer) {
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
    ReflectionTestUtils.setField(purchase, "id", id);
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
