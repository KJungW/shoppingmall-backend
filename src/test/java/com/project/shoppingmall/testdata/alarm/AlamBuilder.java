package com.project.shoppingmall.testdata.alarm;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.testdata.product.ProductBuilder;
import com.project.shoppingmall.testdata.review.ReviewBuilder;
import com.project.shoppingmall.type.AlarmType;
import com.project.shoppingmall.type.LoginType;
import org.springframework.test.util.ReflectionTestUtils;

public class AlamBuilder {

  public static Alarm makeMemberBanAlarm(long id) {
    Member givenMember = MemberBuilder.makeMember(10L, LoginType.NAVER);
    Alarm givenAlarm =
        Alarm.builder().listener(givenMember).alarmType(AlarmType.MEMBER_BAN).build();
    ReflectionTestUtils.setField(givenAlarm, "id", id);
    return givenAlarm;
  }

  public static Alarm makeReviewBanAlarm(long id) {
    Member givenMember = MemberBuilder.makeMember(10L, LoginType.NAVER);
    Review givenReview =
        ReviewBuilder.makeReview(500L, givenMember, ProductBuilder.makeProduct(20L));
    Alarm alarm =
        Alarm.builder()
            .listener(givenMember)
            .alarmType(AlarmType.REVIEW_BAN)
            .targetReview(givenReview)
            .build();
    ReflectionTestUtils.setField(alarm, "id", id);
    return alarm;
  }

  public static Alarm makeProductBanAlarm(long id) {
    Member givenMember = MemberBuilder.makeMember(10L, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(20L);
    Alarm alarm =
        Alarm.builder()
            .listener(givenMember)
            .alarmType(AlarmType.PRODUCT_BAN)
            .targetProduct(givenProduct)
            .build();
    ReflectionTestUtils.setField(alarm, "id", id);
    return alarm;
  }
}
