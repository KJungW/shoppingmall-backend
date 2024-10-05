package com.project.shoppingmall.service.refund;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.refund.RefundDto;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.RefundRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_dto.SliceResultManager;
import com.project.shoppingmall.test_dto.refund.RefundDtoManager;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.purchase.PurchaseBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.test_entity.refund.RefundBuilder;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.util.JsonUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

class RefundRetrieveServiceTest {
  private RefundRetrieveService target;
  private RefundRetrieveRepository mockRefundRetrieveRepository;
  private MemberFindService mockMemberFindService;
  private PurchaseItemFindService mockPurchaseItemFindService;
  private static MockedStatic<JsonUtil> jsonUtil;

  @BeforeEach
  public void beforeEach() {
    mockRefundRetrieveRepository = mock(RefundRetrieveRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    mockPurchaseItemFindService = mock(PurchaseItemFindService.class);
    jsonUtil = mockStatic(JsonUtil.class);
    target =
        new RefundRetrieveService(
            mockRefundRetrieveRepository, mockMemberFindService, mockPurchaseItemFindService);
  }

  @AfterEach
  public void afterEach() {
    jsonUtil.close();
  }

  @Test
  @DisplayName("retrieveAllByPurchaseItem() : 정상흐름 - 구매자 입장")
  public void retrieveAllByPurchaseItem_ok_buyer() {
    // given
    long inputBuyerId = 3L;
    long inputPurchaseItemId = 5L;
    int inputSliceNum = 0;
    int inputSliceSize = 5;

    Member givenBuyer = MemberBuilder.makeMember(inputBuyerId, LoginType.NAVER);
    Member givenSeller = MemberBuilder.makeMember(512L, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(50L, givenSeller);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputPurchaseItemId, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            10L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    List<Refund> refunds = RefundBuilder.makeList(List.of(1L, 2L, 3L), givenPurchaseItem);
    Slice<Refund> givneSlice = SliceManager.setMockSlice(inputSliceNum, inputSliceSize, refunds);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenBuyer));
    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockRefundRetrieveRepository.findByPurchaseItem(anyLong(), any())).thenReturn(givneSlice);

    // when
    SliceResult<RefundDto> result =
        target.retrieveAllByPurchaseItem(
            inputBuyerId, inputPurchaseItemId, inputSliceNum, inputSliceSize);

    // then
    check_refundRetrieveRepository_findByPurchaseItem(
        inputPurchaseItemId, inputSliceNum, inputSliceSize, "createDate");
    SliceResultManager.checkOnlySliceData(givneSlice, result);
    RefundDtoManager.checkList(refunds, result.getContentList());
  }

  @Test
  @DisplayName("retrieveAllByPurchaseItem() : 정상흐름 - 판매자 입장")
  public void retrieveAllByPurchaseItem_ok_seller() {
    // given
    long inputSellerId = 3L;
    long inputPurchaseItemId = 5L;
    int inputSliceNum = 0;
    int inputSliceSize = 5;

    Member givenBuyer = MemberBuilder.makeMember(202L, LoginType.NAVER);
    Member givenSeller = MemberBuilder.makeMember(inputSellerId, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(50L, givenSeller);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputPurchaseItemId, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            10L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    List<Refund> refunds = RefundBuilder.makeList(List.of(1L, 2L, 3L), givenPurchaseItem);
    Slice<Refund> givneSlice = SliceManager.setMockSlice(inputSliceNum, inputSliceSize, refunds);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));
    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockRefundRetrieveRepository.findByPurchaseItem(anyLong(), any())).thenReturn(givneSlice);

    // when
    SliceResult<RefundDto> result =
        target.retrieveAllByPurchaseItem(
            inputSellerId, inputPurchaseItemId, inputSliceNum, inputSliceSize);

    // then
    check_refundRetrieveRepository_findByPurchaseItem(
        inputPurchaseItemId, inputSliceNum, inputSliceSize, "createDate");
    SliceResultManager.checkOnlySliceData(givneSlice, result);
    RefundDtoManager.checkList(refunds, result.getContentList());
  }

  @Test
  @DisplayName("retrieveAllByPurchaseItem() : 현재 회원과 상관없는 구매아이템에 대해 환불조회")
  public void retrieveAllByPurchaseItem_otherMemberRefunds() {
    // given
    // - 인자세팅
    long inputWrongMemberId = 3L;
    long inputPurchaseItemId = 5L;
    int inputSliceNum = 0;
    int inputSliceSize = 5;

    Member givenOtherMember = MemberBuilder.makeMember(inputWrongMemberId, LoginType.NAVER);
    Member givenBuyer = MemberBuilder.makeMember(202L, LoginType.NAVER);
    Member givenSeller = MemberBuilder.makeMember(523L, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(50L, givenSeller);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(inputPurchaseItemId, givenProduct);
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(
            10L, givenBuyer, List.of(givenPurchaseItem), PurchaseStateType.COMPLETE);
    List<Refund> refunds = RefundBuilder.makeList(List.of(1L, 2L, 3L), givenPurchaseItem);
    Slice<Refund> mockSliceResult =
        SliceManager.setMockSlice(inputSliceNum, inputSliceSize, refunds);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenOtherMember));
    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    when(mockRefundRetrieveRepository.findByPurchaseItem(anyLong(), any()))
        .thenReturn(mockSliceResult);

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.retrieveAllByPurchaseItem(
                inputWrongMemberId, inputPurchaseItemId, inputSliceNum, inputSliceSize));
  }

  public void check_refundRetrieveRepository_findByPurchaseItem(
      long givenPurchaseItem, long givenSliceNum, long givenSliceSize, String sortField) {
    ArgumentCaptor<Long> purchaseItemIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockRefundRetrieveRepository, times(1))
        .findByPurchaseItem(purchaseItemIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenPurchaseItem, purchaseItemIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC, captoredPageRequest.getSort().getOrderFor(sortField).getDirection());
    assertEquals(sortField, captoredPageRequest.getSort().getOrderFor(sortField).getProperty());
  }
}
