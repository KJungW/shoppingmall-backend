package com.project.shoppingmall.service.alarm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.AlarmRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.test_entity.alarm.AlarmBuilder;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.type.LoginType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AlarmDeleteServiceTest {
  private AlarmDeleteService target;
  private AlarmRepository mockAlarmRepository;
  private MemberFindService mockMemberFindService;

  @BeforeEach
  public void beforeEach() {
    mockAlarmRepository = mock(AlarmRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    target = new AlarmDeleteService(mockAlarmRepository, mockMemberFindService);
  }

  @Test
  @DisplayName("deleteAlarmInController() : 정상흐름")
  public void deleteAlarmInController_ok() {
    // given
    long inputListenerId = 10L;
    long inputAlarmId = 20L;

    Member givenListener = MemberBuilder.makeMember(inputListenerId, LoginType.NAVER);
    Alarm givenAlarm = AlarmBuilder.makeMemberBanAlarm(inputAlarmId, givenListener);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenListener));
    when(mockAlarmRepository.findById(anyLong())).thenReturn(Optional.of(givenAlarm));

    // when
    target.deleteAlarmInController(inputListenerId, inputAlarmId);

    // then
    check_alarmRepository_deleteById(inputAlarmId);
  }

  @Test
  @DisplayName("deleteAlarmInController() : 다른 회원의 알림을 제거하려고 시도함")
  public void deleteAlarmInController_otherMemberAlarm() {
    // given
    long inputListenerId = 10L;
    long inputAlarmId = 20L;

    Member givenListener = MemberBuilder.makeMember(inputListenerId, LoginType.NAVER);
    Member givenOtherMember = MemberBuilder.makeMember(30L, LoginType.NAVER);
    Alarm givenOtherMemberAlarm = AlarmBuilder.makeMemberBanAlarm(634L, givenOtherMember);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenListener));
    when(mockAlarmRepository.findById(anyLong())).thenReturn(Optional.of(givenOtherMemberAlarm));

    // when then
    assertThrows(
        DataNotFound.class, () -> target.deleteAlarmInController(inputListenerId, inputAlarmId));
  }

  public void check_alarmRepository_deleteById(long givenAlarmId) {
    ArgumentCaptor<Long> alarmIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmRepository, times(1)).deleteById(alarmIdCaptor.capture());
    assertEquals(givenAlarmId, alarmIdCaptor.getValue());
  }
}
