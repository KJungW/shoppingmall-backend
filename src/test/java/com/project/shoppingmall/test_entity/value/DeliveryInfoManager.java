package com.project.shoppingmall.test_entity.value;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import org.junit.jupiter.api.Assertions;

public class DeliveryInfoManager {
  public static DeliveryInfo make() {
    return new DeliveryInfo("testSenderName", "testAddress", "01010", "000-0000-0000");
  }

  public static DeliveryInfo make(String name, String address, String postCode, String tel) {
    return new DeliveryInfo(name, address, postCode, tel);
  }

  public static void check(DeliveryDto deliveryDto, DeliveryInfo target) {
    Assertions.assertEquals(deliveryDto.getSenderName(), target.getSenderName());
    Assertions.assertEquals(deliveryDto.getSenderAddress(), target.getSenderAddress());
    Assertions.assertEquals(deliveryDto.getSenderPostCode(), target.getSenderPostCode());
    Assertions.assertEquals(deliveryDto.getSenderTel(), target.getSenderTel());
  }
}
