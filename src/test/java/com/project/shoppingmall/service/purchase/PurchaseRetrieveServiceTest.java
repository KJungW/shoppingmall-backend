package com.project.shoppingmall.service.purchase;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.purchase.PurchaseDto;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_dto.SliceResultManager;
import com.project.shoppingmall.test_dto.purchase.PurchaseDtoManager;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.purchase.PurchaseBuilder;
import java.util.List;
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
    Long inputMemberId = 10L;
    int inputSliceNumber = 0;
    int inputSliceSize = 5;

    Member givenBuyer = MemberBuilder.makeMember(10L);
    List<Purchase> givenPurchases =
        PurchaseBuilder.makePurchaseList(List.of(1L, 2L, 3L), givenBuyer);
    Slice<Purchase> givenSlice =
        SliceManager.setMockSlice(inputSliceNumber, inputSliceSize, givenPurchases);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenBuyer));
    when(mockPurchaseRetrieveRepository.findAllByBuyer(anyLong(), any())).thenReturn(givenSlice);

    // when
    SliceResult<PurchaseDto> result =
        target.retrieveAllByMember(inputMemberId, inputSliceNumber, inputSliceSize);

    // then
    check_purchaseRetrieveRepository_findAllByBuyer(
        inputMemberId, inputSliceNumber, inputSliceSize);
    SliceResultManager.checkOnlySliceData(givenSlice, result);
    PurchaseDtoManager.checkList(givenPurchases, result.getContentList());
  }

  @Test
  @DisplayName("retrieveAllByMember() : 유효하지 않은 회원ID")
  public void retrieveAllByMember_noMember() {
    // given
    Long inputMemberId = 10L;
    int inputSliceNumber = 0;
    int inputSliceSize = 5;

    when(mockMemberFindService.findById(any())).thenReturn(Optional.empty());

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.retrieveAllByMember(inputMemberId, inputSliceNumber, inputSliceSize));
  }

  public void check_purchaseRetrieveRepository_findAllByBuyer(
      Long inputMemberId, int inputSliceNumber, int inputSliceSize) {
    ArgumentCaptor<Long> memberIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseRetrieveRepository, times(1))
        .findAllByBuyer(memberIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(inputMemberId, memberIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(inputSliceNumber, captoredPageRequest.getPageNumber());
    assertEquals(inputSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate", captoredPageRequest.getSort().getOrderFor("createDate").getProperty());
  }
}
