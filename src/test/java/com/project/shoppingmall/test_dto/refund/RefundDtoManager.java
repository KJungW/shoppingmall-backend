package com.project.shoppingmall.test_dto.refund;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.refund.RefundDto;
import com.project.shoppingmall.entity.Refund;
import java.util.List;

public class RefundDtoManager {

  public static void check(Refund refund, RefundDto target) {
    assertEquals(refund.getId(), target.getRefundId());
    assertEquals(refund.getRefundPrice(), target.getRefundPrice());
    assertEquals(refund.getRequestTitle(), target.getRequestTitle());
    assertEquals(refund.getRequestContent(), target.getRequestContent());
    assertEquals(refund.getResponseContent(), target.getResponseContent());
    assertEquals(refund.getState(), target.getRefundState());
    assertEquals(refund.getPurchaseItem().getId(), target.getPurchaseItemId());
  }

  public static void checkList(List<Refund> refundList, List<RefundDto> targetList) {
    assertEquals(refundList.size(), targetList.size());
    for (int i = 0; i < targetList.size(); i++) {
      check(refundList.get(i), targetList.get(i));
    }
  }
}
