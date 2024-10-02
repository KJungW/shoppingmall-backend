package com.project.shoppingmall.testdata.alarm;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.type.AlarmType;

public class Alarm_RealDataBuilder {
  public static Alarm makeMemberBanAlarm(Member listener) {
    return Alarm.builder().listener(listener).alarmType(AlarmType.MEMBER_BAN).build();
  }

  public static Alarm makeReviewBanAlarm(Member listener, Review review) {
    return Alarm.builder()
        .listener(listener)
        .alarmType(AlarmType.REVIEW_BAN)
        .targetReview(review)
        .build();
  }

  public static Alarm makeProductBanAlarm(Member listener, Product product) {
    return Alarm.builder()
        .listener(listener)
        .alarmType(AlarmType.PRODUCT_BAN)
        .targetProduct(product)
        .build();
  }

  public static Alarm makeRefundRequestAlarm(Member listener, Refund refund) {
    return Alarm.builder()
        .listener(listener)
        .alarmType(AlarmType.REFUND_REQUEST)
        .targetRefund(refund)
        .build();
  }
}
