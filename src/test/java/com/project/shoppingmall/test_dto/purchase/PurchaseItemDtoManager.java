package com.project.shoppingmall.test_dto.purchase;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.dto.purchase.PurchaseItemDto;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.test_dto.product.ProductOptionDtoManager;
import com.project.shoppingmall.util.JsonUtil;
import java.util.List;

public class PurchaseItemDtoManager {
  public static void check(PurchaseItem purchaseItem, PurchaseItemDto target) {
    ProductDataForPurchase productData =
        JsonUtil.convertJsonToObject(purchaseItem.getProductData(), ProductDataForPurchase.class);
    assertEquals(purchaseItem.getId(), target.getPurchaseItemId());
    assertEquals(productData.getProductId(), target.getProductId());
    assertEquals(productData.getSellerId(), target.getSellerId());
    assertEquals(productData.getSellerName(), target.getSellerName());
    assertEquals(productData.getProductName(), target.getProductName());
    assertEquals(productData.getProductTypeName(), target.getProductTypeName());
    checkOptionInTarget(productData, target);
    assertEquals(productData.getPrice(), target.getPrice());
    assertEquals(productData.getDiscountAmount(), target.getDiscountAmount());
    assertEquals(productData.getDiscountRate(), target.getDiscountRate());
    assertEquals(purchaseItem.getFinalPrice(), target.getFinalPrice());
    assertEquals(purchaseItem.getIsRefund(), target.isRefund());
  }

  public static void checkList(
      List<PurchaseItem> purchaseItemList, List<PurchaseItemDto> targetList) {
    assertEquals(purchaseItemList.size(), targetList.size());
    for (int i = 0; i < targetList.size(); i++) {
      check(purchaseItemList.get(i), targetList.get(i));
    }
  }

  private static void checkOptionInTarget(
      ProductDataForPurchase productData, PurchaseItemDto target) {
    if (productData.getSingleOption() != null)
      ProductOptionDtoManager.check(
          productData.getSingleOption(), target.getSelectedSingleOption());
    if (productData.getMultiOptions() != null)
      ProductOptionDtoManager.check(
          productData.getMultiOptions(), target.getSelectedMultiOptions());
  }
}
