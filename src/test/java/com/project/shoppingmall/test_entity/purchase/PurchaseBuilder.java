package com.project.shoppingmall.test_entity.purchase;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.test_entity.value.DeliveryInfoManager;
import com.project.shoppingmall.testutil.TestUtil;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.PurchaseStateType;
import java.util.List;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class PurchaseBuilder {

  public static Purchase.PurchaseBuilder fullData() {
    Member givenBuyer = MemberBuilder.makeMember(10L, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(23L);
    List<PurchaseItem> givenPurchaseItems =
        PurchaseItemBuilder.makePurchaseItemList(TestUtil.makeIdList(3, 10L), givenProduct);

    DeliveryInfo givenDeliveryInfo = DeliveryInfoManager.make();
    int givenTotalPrice = givenPurchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();

    return Purchase.builder()
        .buyerId(givenBuyer.getId())
        .purchaseItems(givenPurchaseItems)
        .purchaseUid("test-purchaseUid" + UUID.randomUUID())
        .purchaseTitle("testPurchaseTitle")
        .deliveryInfo(givenDeliveryInfo)
        .totalPrice(givenTotalPrice);
  }

  public static Purchase makePurchase(long id, Member buyer) {
    PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(123L);
    int totalPrice = purchaseItem.getFinalPrice();
    Purchase purchase =
        fullData()
            .buyerId(buyer.getId())
            .purchaseItems(List.of(purchaseItem))
            .totalPrice(totalPrice)
            .build();
    ReflectionTestUtils.setField(purchase, "id", id);
    return purchase;
  }

  public static Purchase makePurchase(
      long id, Member buyer, List<PurchaseItem> purchaseItems, PurchaseStateType state) {
    int totalPrice = purchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();
    Purchase purchase =
        fullData()
            .buyerId(buyer.getId())
            .purchaseItems(purchaseItems)
            .totalPrice(totalPrice)
            .build();
    ReflectionTestUtils.setField(purchase, "id", id);
    updatePurchaseState(purchase, state);
    return purchase;
  }

  public static Purchase makePurchase(
      long id, List<PurchaseItem> purchaseItems, PurchaseStateType state) {
    int totalPrice = purchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();
    Purchase purchase = fullData().purchaseItems(purchaseItems).totalPrice(totalPrice).build();
    ReflectionTestUtils.setField(purchase, "id", id);
    updatePurchaseState(purchase, state);
    return purchase;
  }

  public static Purchase makePurchase(long id, Member buyer, List<PurchaseItem> purchaseItems) {
    int totalPrice = purchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();
    Purchase purchase =
        fullData()
            .buyerId(buyer.getId())
            .purchaseItems(purchaseItems)
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

  public static List<Purchase> makePurchaseList(List<Long> idList, Member buyer) {
    return idList.stream().map(id -> makePurchase(id, buyer)).toList();
  }
}
