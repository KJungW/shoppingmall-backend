package com.project.shoppingmall.test_dto.delivery;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import org.junit.jupiter.api.Assertions;

public class DeliveryDtoManager {
  public static DeliveryDto make() {
    return new DeliveryDto("testName", "testAddress", "11011", "010-000-0000");
  }

  public static void check(DeliveryInfo deliveryInfo, DeliveryDto target) {
    Assertions.assertEquals(deliveryInfo.getSenderName(), target.getSenderName());
    Assertions.assertEquals(deliveryInfo.getSenderAddress(), target.getSenderAddress());
    Assertions.assertEquals(deliveryInfo.getSenderPostCode(), target.getSenderPostCode());
    Assertions.assertEquals(deliveryInfo.getSenderTel(), target.getSenderTel());
  }
}
