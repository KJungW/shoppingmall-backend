package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.type.LoginType;
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

  public static Purchase makeCompleteStatePurchase(Member buyer, List<PurchaseItem> purchaseItems) {
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
    purchase.convertStateToComplete("test-completeUid" + UUID.randomUUID());
    return purchase;
  }

  public static Purchase makeCompleteStatePurchase(
      long id, Member buyer, List<PurchaseItem> purchaseItems) {
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
    purchase.convertStateToComplete("test-completeUid" + UUID.randomUUID());
    ReflectionTestUtils.setField(purchase, "id", id);
    return purchase;
  }

  public static Purchase makPurchaseItem(long id, List<PurchaseItem> purchaseItems) {
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
    purchase.convertStateToComplete("test-completeUid" + UUID.randomUUID());
    ReflectionTestUtils.setField(purchase, "id", id);
    return purchase;
  }
}
