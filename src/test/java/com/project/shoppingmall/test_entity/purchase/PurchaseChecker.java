package com.project.shoppingmall.test_entity.purchase;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.purchase.PurchaseItemMakeData;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemChecker;
import com.project.shoppingmall.test_entity.value.DeliveryInfoManager;
import com.project.shoppingmall.type.PurchaseStateType;
import java.util.List;

public class PurchaseChecker {

  public static void check(
      Member buyer,
      List<PurchaseItemMakeData> purchaseItemMakeDatas,
      List<BasketItem> basketItemList,
      PurchaseStateType state,
      DeliveryDto deliveryDto,
      Purchase target) {
    assertEquals(buyer.getId(), target.getBuyerId());
    checkPurchaseItemsInPurchase(purchaseItemMakeDatas, basketItemList, target);
    assertFalse(target.getPurchaseUid().isBlank());
    checkPaymentUidAndStateInPurchase(state, target);
    assertFalse(target.getPurchaseTitle().isBlank());
    DeliveryInfoManager.check(deliveryDto, target.getDeliveryInfo());
    assertEquals(calcTotalPrice(purchaseItemMakeDatas), target.getTotalPrice());
  }

  private static void checkPaymentUidAndStateInPurchase(PurchaseStateType state, Purchase target) {
    assertEquals(state, target.getState());
    if (state.equals(PurchaseStateType.READY)) assertNull(target.getPaymentUid());
    else assertFalse(target.getPaymentUid().isBlank());
  }

  private static void checkPurchaseItemsInPurchase(
      List<PurchaseItemMakeData> purchaseItemMakeDatas,
      List<BasketItem> basketItemList,
      Purchase target) {
    assertEquals(target.getPurchaseItems().size(), purchaseItemMakeDatas.size());
    assertEquals(target.getPurchaseItems().size(), basketItemList.size());
    for (int i = 0; i < target.getPurchaseItems().size(); i++) {
      PurchaseItemChecker.checkNotRefunded(
          purchaseItemMakeDatas.get(i),
          basketItemList.get(i),
          target,
          target.getPurchaseItems().get(i));
    }
  }

  private static int calcTotalPrice(List<PurchaseItemMakeData> purchaseItemMakeDatas) {
    return purchaseItemMakeDatas.stream()
        .mapToInt(PurchaseItemMakeData::getExpectedFinalPrice)
        .sum();
  }
}
