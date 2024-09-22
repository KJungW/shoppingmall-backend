package com.project.shoppingmall.service.alarm;

import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.AlarmRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmDeleteService {
  private final AlarmRepository alarmRepository;
  private final MemberFindService memberFindService;

  public void deleteAlarm(long alarmId) {
    alarmRepository.deleteById(alarmId);
  }

  public void deleteAlarmList(List<Alarm> alarms) {
    alarmRepository.deleteAllInBatch(alarms);
  }

  public void deleteAlarmInController(long listenerId, long alarmId) {
    Member listener =
        memberFindService
            .findById(listenerId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 회원이 존재하지 않습니다."));
    Alarm alarm =
        alarmRepository
            .findById(alarmId)
            .orElseThrow(() -> new DataNotFound("id에 해당하는 알림이 존재하지 않습니다."));

    if (!alarm.getListener().getId().equals(listener.getId())) {
      throw new DataNotFound("다른 회원의 알림을 제거하려고 시도하고 있습니다.");
    }

    deleteAlarm(alarmId);
  }
}
