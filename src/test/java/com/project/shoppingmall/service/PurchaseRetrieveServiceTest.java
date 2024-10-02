package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.purchase.PurchaseRetrieveService;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

class PurchaseRetrieveServiceTest {
  private PurchaseRetrieveService target;
  private PurchaseRetrieveRepository mockPurchaseRetrieveRepository;
  private MemberFindService mockMemberFindService;

  @BeforeEach
  public void beforeEach() {
    mockPurchaseRetrieveRepository = mock(PurchaseRetrieveRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    target = new PurchaseRetrieveService(mockPurchaseRetrieveRepository, mockMemberFindService);
  }

  @Test
  @DisplayName("retrieveAllByMember() : 정상흐름")
  public void retrieveAllByMember_ok() {
    // given
    Long givenMemberId = 10L;
    int givenSliceNumber = 0;
    int givenSliceSize = 5;
    Member givenMember = MemberBuilder.fullData().build();
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    Slice mockSliceResult = mock(Slice.class);
    when(mockSliceResult.getContent()).thenReturn(new ArrayList<>());
    when(mockPurchaseRetrieveRepository.findAllByBuyer(anyLong(), any()))
        .thenReturn(mockSliceResult);

    // when
    target.retrieveAllByMember(givenMemberId, givenSliceNumber, givenSliceSize);

    // then
    ArgumentCaptor<Long> memberIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseRetrieveRepository, times(1))
        .findAllByBuyer(memberIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenMemberId, memberIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNumber, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate", captoredPageRequest.getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("retrieveAllByMember() : 유효하지 않은 회원ID")
  public void retrieveAllByMember_noMember() {
    // given
    Long givenMemberId = 10L;
    int givenSliceNumber = 0;
    int givenSliceSize = 5;
    when(mockMemberFindService.findById(any())).thenReturn(Optional.empty());

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.retrieveAllByMember(givenMemberId, givenSliceNumber, givenSliceSize));
  }
}
