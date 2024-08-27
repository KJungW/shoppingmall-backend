package com.project.shoppingmall.service.alarm;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.alarm.AlarmDto;
import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.repository.AlarmRetrieveRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlarmRetrieveService {
  private final AlarmRetrieveRepository alarmRetrieveRepository;

  public SliceResult<AlarmDto> retrieveAllByMember(
      int sliceNumber, int sliceSize, long listenerId) {
    PageRequest pageRequest =
        PageRequest.of(sliceNumber, sliceSize, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<Alarm> slice = alarmRetrieveRepository.retrieveAllByMember(listenerId, pageRequest);
    List<AlarmDto> alarmDtoList = slice.getContent().stream().map(AlarmDto::new).toList();
    return new SliceResult<>(slice, alarmDtoList);
  }
}
