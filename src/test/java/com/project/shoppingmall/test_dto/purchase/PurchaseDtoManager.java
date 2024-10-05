package com.project.shoppingmall.test_dto.purchase;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.purchase.PurchaseDto;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.test_dto.delivery.DeliveryDtoManager;
import java.util.List;

public class PurchaseDtoManager {
  public static void check(Purchase purchase, PurchaseDto target) {
    assertEquals(purchase.getId(), target.getPurchaseId());
    assertEquals(purchase.getBuyerId(), target.getBuyerId());
    assertEquals(purchase.getState(), target.getState());
    assertEquals(purchase.getPurchaseTitle(), target.getPurchaseTitle());
    DeliveryDtoManager.check(purchase.getDeliveryInfo(), target.getDeliveryInfo());
    assertEquals(purchase.getTotalPrice(), target.getTotalPrice());
    assertEquals(purchase.getCreateDate(), target.getDateTime());
    PurchaseItemDtoManager.checkList(purchase.getPurchaseItems(), target.getPurchaseItems());
  }

  public static void checkList(List<Purchase> purchaseList, List<PurchaseDto> targetList) {
    assertEquals(purchaseList.size(), targetList.size());
    for (int i = 0; i < targetList.size(); i++) {
      check(purchaseList.get(i), targetList.get(i));
    }
  }
}
