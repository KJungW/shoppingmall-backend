package com.project.shoppingmall.service.alarm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.alarm.AlarmDto;
import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.repository.AlarmRetrieveRepository;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_entity.alarm.AlarmBuilder;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.testutil.TestUtil;
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

    Member givenListener = MemberBuilder.makeMember(inputListenerId);
    List<Long> givenAlarmIdList = TestUtil.makeIdList(inputSliceSize, 40L);
    List<Alarm> givenAlarmList =
        AlarmBuilder.makeMemberBanAlarmList(givenAlarmIdList, givenListener);
    Slice<Alarm> givenSliceData =
        SliceManager.setMockSlice(inputSliceNumber, inputSliceSize, givenAlarmList);

    when(mockAlarmRetrieveRepository.retrieveAllByMember(anyLong(), any()))
        .thenReturn(givenSliceData);

    // when
    SliceResult<AlarmDto> sliceResult =
        target.retrieveAllByMember(inputSliceNumber, inputSliceSize, inputListenerId);

    // then
    check_alarmRetrieveRepository_retrieveAllByMember(
        inputSliceNumber, inputSliceSize, inputListenerId);
    checkSliceResult(inputSliceNumber, inputSliceSize, sliceResult);
  }

  public void checkSliceResult(
      long inputSliceNumber, long inputSliceSize, SliceResult<AlarmDto> sliceResult) {
    assertEquals(inputSliceNumber, sliceResult.getCurrentSliceNumber());
    assertEquals(inputSliceSize, sliceResult.getSliceSize());
    assertEquals(inputSliceSize, sliceResult.getContentList().size());
  }

  public void check_alarmRetrieveRepository_retrieveAllByMember(
      long inputSliceNumber, long inputSliceSize, long inputListenerId) {
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
  }
}
