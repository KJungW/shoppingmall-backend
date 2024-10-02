package com.project.shoppingmall.service.alarm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.AlarmRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.testdata.alarm.AlamBuilder;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

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

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    Alarm givenAlarm = AlamBuilder.makeMemberBanAlarm(634L);
    ReflectionTestUtils.setField(givenAlarm, "id", inputAlarmId);
    ReflectionTestUtils.setField(givenAlarm.getListener(), "id", inputListenerId);
    when(mockAlarmRepository.findById(anyLong())).thenReturn(Optional.of(givenAlarm));

    // when
    target.deleteAlarmInController(inputListenerId, inputAlarmId);

    // then
    ArgumentCaptor<Long> alarmIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmRepository, times(1)).deleteById(alarmIdCaptor.capture());
    assertEquals(givenAlarm.getId(), alarmIdCaptor.getValue());
  }

  @Test
  @DisplayName("deleteAlarmInController() : 다른 회원의 알림을 제거하려고 시도함")
  public void deleteAlarmInController_otherMemberAlarm() {
    // given
    long inputListenerId = 10L;
    long inputAlarmId = 20L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    long otherMemberId = 30L;
    Alarm givenAlarm = AlamBuilder.makeMemberBanAlarm(634L);
    ReflectionTestUtils.setField(givenAlarm, "id", inputAlarmId);
    ReflectionTestUtils.setField(givenAlarm.getListener(), "id", otherMemberId);
    when(mockAlarmRepository.findById(anyLong())).thenReturn(Optional.of(givenAlarm));

    // when then
    assertThrows(
        DataNotFound.class, () -> target.deleteAlarmInController(inputListenerId, inputAlarmId));
  }
}
