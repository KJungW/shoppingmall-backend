package com.project.shoppingmall.service.alarm;

import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.repository.AlarmRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AlarmDeleteService {
  private final AlarmRepository alarmRepository;

  public void deleteAlarm(long alarmId) {
    alarmRepository.deleteById(alarmId);
  }

  public void deleteAlarmList(List<Alarm> alarms) {
    alarmRepository.deleteAllInBatch(alarms);
  }
}
