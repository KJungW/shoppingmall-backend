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
public class AlarmFindService {
  private final AlarmRepository alarmRepository;

  public List<Alarm> findByTargetProduct(long productId) {
    return alarmRepository.findAlarmByTargetProduct(productId);
  }

  public List<Alarm> findByTargetReview(long reviewId) {
    return alarmRepository.findAlarmByTargetReview(reviewId);
  }

  public List<Alarm> findAllByListener(long listenerId) {
    return alarmRepository.findAllByListener(listenerId);
  }
}
