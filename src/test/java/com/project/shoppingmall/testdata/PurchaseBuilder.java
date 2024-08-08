package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class PurchaseBuilder {

  public static Purchase.PurchaseBuilder fullData() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    DeliveryInfo givenDeliveryInfo =
        new DeliveryInfo("testSenderName", "testAddress", "01010", "000-0000-0000");

    List<PurchaseItem> purchaseItems = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      PurchaseItem item = PurchaseItemBuilder.fullData().build();
      ReflectionTestUtils.setField(item, "finalPrice", 10000);
      purchaseItems.add(item);
    }

    return Purchase.builder()
        .buyer(givenMember)
        .purchaseItems(purchaseItems)
        .purchaseUid("testPurchaseUid1234")
        .purchaseTitle("testPurchaseTitle")
        .deliveryInfo(givenDeliveryInfo)
        .totalPrice(30000);
  }
}
