package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.type.AlarmType;
import java.io.IOException;

public class AlarmBuilder {

  public static Alarm makeMemberBanAlarm(Member listener) throws IOException {
    return Alarm.builder().listener(listener).alarmType(AlarmType.MEMBER_BAN).build();
  }
}
