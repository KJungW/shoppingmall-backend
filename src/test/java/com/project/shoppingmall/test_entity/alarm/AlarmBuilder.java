package com.project.shoppingmall.test_entity.alarm;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import com.project.shoppingmall.type.AlarmType;
import com.project.shoppingmall.type.LoginType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class AlarmBuilder {

  public static Alarm makeMemberBanAlarm(long id, Member givenListener) {
    Alarm givenAlarm =
        Alarm.builder().listener(givenListener).alarmType(AlarmType.MEMBER_BAN).build();
    ReflectionTestUtils.setField(givenAlarm, "id", id);
    return givenAlarm;
  }

  public static List<Alarm> makeMemberBanAlarmList(List<Long> idList, Member givenListener) {
    List<Alarm> givenAlarmList = new ArrayList<>();
    idList.forEach(
        id -> {
          givenAlarmList.add(AlarmBuilder.makeMemberBanAlarm(id, givenListener));
        });
    return givenAlarmList;
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
