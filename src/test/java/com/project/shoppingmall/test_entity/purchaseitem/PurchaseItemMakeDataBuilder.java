package com.project.shoppingmall.test_entity.purchaseitem;

import com.project.shoppingmall.dto.purchase.PurchaseItemMakeData;
import java.util.ArrayList;
import java.util.List;

public class PurchaseItemMakeDataBuilder {
  public static PurchaseItemMakeData make(long basketItemId, int finalPrice) {
    return new PurchaseItemMakeData(basketItemId, finalPrice);
  }

  public static List<PurchaseItemMakeData> make(
      List<Long> basketItemIdList, List<Integer> finalPriceList) {
    if (basketItemIdList.size() != finalPriceList.size())
      throw new IllegalArgumentException(
          "basketItemIdList와 finalPriceList의 원소개수가 맞지 않아 데이터를 생성할 수 없습니다.");

    List<PurchaseItemMakeData> result = new ArrayList<>();
    for (int i = 0; i < basketItemIdList.size(); i++) {
      result.add(make(basketItemIdList.get(i), finalPriceList.get(i)));
    }
    return result;
  }
}
