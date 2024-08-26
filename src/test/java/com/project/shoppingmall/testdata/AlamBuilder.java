package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.type.AlarmType;
import java.io.IOException;
import org.springframework.test.util.ReflectionTestUtils;

public class AlamBuilder {

  public static Alarm.AlarmBuilder memberBanFullData() {
    Member givenMember = MemberBuilder.fullData().build();
    return Alarm.builder().listener(givenMember).alarmType(AlarmType.MEMBER_BAN);
  }

  public static Alarm.AlarmBuilder reviewBanFullData() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", givenMember.getId());

    return Alarm.builder()
        .listener(givenMember)
        .alarmType(AlarmType.REVIEW_BAN)
        .targetReview(givenReview);
  }

  public static Alarm.AlarmBuilder productBanFullData() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenMember.getId());

    return Alarm.builder()
        .listener(givenMember)
        .alarmType(AlarmType.PRODUCT_BAN)
        .targetProduct(givenProduct);
  }

  public static Alarm.AlarmBuilder makeRefundFullData() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    Refund givenRefund = RefundBuilder.makeRefundWithPurchaseItem();
    ReflectionTestUtils.setField(givenRefund.getPurchaseItem(), "sellerId", givenMember.getId());

    return Alarm.builder()
        .listener(givenMember)
        .alarmType(AlarmType.REFUND_REQUEST)
        .targetRefund(givenRefund);
  }

  public static Alarm.AlarmBuilder typeDeleteFullData() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenMember.getId());

    return Alarm.builder()
        .listener(givenMember)
        .alarmType(AlarmType.TYPE_DELETE)
        .targetProduct(givenProduct);
  }

  public static Alarm.AlarmBuilder typeUpdateFullData() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenMember.getId());

    return Alarm.builder()
        .listener(givenMember)
        .alarmType(AlarmType.TYPE_UPDATE)
        .targetProduct(givenProduct);
  }
}
