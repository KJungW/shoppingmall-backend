package com.project.shoppingmall.test_entity.purchaseitem;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.dto.purchase.PurchaseItemMakeData;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_dto.purchase.ProductDataForPurchaseManager;
import com.project.shoppingmall.type.RefundStateTypeForPurchaseItem;
import com.project.shoppingmall.util.JsonUtil;

public class PurchaseItemChecker {
  public static void checkNotRefunded(
      PurchaseItemMakeData makeData,
      BasketItem basketItem,
      Purchase purchase,
      PurchaseItem target) {
    assertEquals(purchase.getId(), target.getPurchase().getId());
    assertEquals(basketItem.getProduct().getId(), target.getProductId());
    assertEquals(basketItem.getProduct().getSeller().getId(), target.getSellerId());
    ProductDataForPurchaseManager.check(basketItem, getProductDataForPurchase(target));
    assertEquals(makeData.getExpectedFinalPrice(), target.getFinalPrice());
    assertEquals(false, target.getIsRefund());
    assertEquals(0, target.getRefunds().size());
    assertEquals(RefundStateTypeForPurchaseItem.NONE, target.getFinalRefundState());
    assertNull(target.getFinalRefundCreatedDate());
    assertNull(target.getReview());
  }

  private static ProductDataForPurchase getProductDataForPurchase(PurchaseItem target) {
    return JsonUtil.convertJsonToObject(target.getProductData(), ProductDataForPurchase.class);
  }
}
