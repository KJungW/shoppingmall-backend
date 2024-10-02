package com.project.shoppingmall.service.alarm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.alarm.AlarmDto;
import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.repository.AlarmRetrieveRepository;
import com.project.shoppingmall.testdata.alarm.AlamBuilder;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

class AlarmRetrieveServiceTest {
  private AlarmRetrieveService target;
  private AlarmRetrieveRepository mockAlarmRetrieveRepository;

  @BeforeEach
  public void beforeEach() {
    mockAlarmRetrieveRepository = mock(AlarmRetrieveRepository.class);
    target = new AlarmRetrieveService(mockAlarmRetrieveRepository);
  }

  @Test
  @DisplayName("retrieveAllByMember() : 정상흐름")
  public void retrieveAllByMember_ok() {
    // given
    int inputSliceNumber = 3;
    int inputSliceSize = 5;
    long inputListenerId = 30L;

    int givenAlarmListSize = 5;
    List<Alarm> givenAlarmData = new ArrayList<>();
    for (int i = 0; i < givenAlarmListSize; i++) {
      givenAlarmData.add(AlamBuilder.makeMemberBanAlarm(634L));
    }

    Slice<Alarm> givenSliceData = mock(Slice.class);
    when(givenSliceData.getNumber()).thenReturn(inputSliceNumber);
    when(givenSliceData.getSize()).thenReturn(inputSliceSize);
    when(givenSliceData.isFirst()).thenReturn(false);
    when(givenSliceData.isLast()).thenReturn(false);
    when(givenSliceData.hasNext()).thenReturn(true);
    when(givenSliceData.hasPrevious()).thenReturn(true);
    when(givenSliceData.getContent()).thenReturn(givenAlarmData);
    when(mockAlarmRetrieveRepository.retrieveAllByMember(anyLong(), any()))
        .thenReturn(givenSliceData);

    // then
    SliceResult<AlarmDto> sliceResult =
        target.retrieveAllByMember(inputSliceNumber, inputSliceSize, inputListenerId);

    // then
    ArgumentCaptor<Long> listenerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockAlarmRetrieveRepository, times(1))
        .retrieveAllByMember(listenerIdCaptor.capture(), pageRequestCaptor.capture());
    assertEquals(inputListenerId, listenerIdCaptor.getValue());
    assertEquals(inputSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(inputSliceNumber, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.DESC,
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate",
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getProperty());

    assertEquals(inputSliceNumber, sliceResult.getCurrentSliceNumber());
    assertEquals(inputSliceSize, sliceResult.getSliceSize());
    assertEquals(givenAlarmListSize, sliceResult.getContentList().size());
  }
}
