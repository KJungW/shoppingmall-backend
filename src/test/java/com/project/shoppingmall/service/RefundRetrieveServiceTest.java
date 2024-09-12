package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.RefundRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import com.project.shoppingmall.service.refund.RefundRetrieveService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.PurchaseBuilder;
import com.project.shoppingmall.testdata.PurchaseItemBuilder;
import com.project.shoppingmall.util.JsonUtil;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

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
  public void retrieveAllByPurchaseItem_ok_buyer() throws IOException {
    // given
    // - 인자세팅
    long givenBuyerId = 3L;
    long givenPurchaseItemId = 5L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    // - memberService.findById() 세팅
    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenBuyerId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenBuyer));

    // - purchaseItemService.findById() 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "id", givenPurchaseItemId);
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", PurchaseBuilder.fullData().build());
    ReflectionTestUtils.setField(givenPurchaseItem.getPurchase(), "buyerId", givenBuyerId);
    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // - JsonUtil.convertJsonToObject() 세팅
    long givenSellerId = 40L;
    ProductDataForPurchase mockProductData = mock(ProductDataForPurchase.class);
    when(mockProductData.getSellerId()).thenReturn(givenSellerId);
    jsonUtil.when(() -> JsonUtil.convertJsonToObject(any(), any())).thenReturn(mockProductData);

    // when
    target.retrieveAllByPurchaseItem(
        givenBuyerId, givenPurchaseItemId, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> purchaseItemIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockRefundRetrieveRepository, times(1))
        .findByPurchaseItem(purchaseItemIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenPurchaseItemId, purchaseItemIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate", captoredPageRequest.getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("retrieveAllByPurchaseItem() : 정상흐름 - 판매자 입장")
  public void retrieveAllByPurchaseItem_ok_seller() throws IOException {
    // given
    // - 인자세팅
    long givenSellerId = 3L;
    long givenPurchaseItemId = 5L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    // - memberService.findById() 세팅
    Member givenSeller = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenSeller, "id", givenSellerId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));

    // - purchaseItemService.findById() 세팅
    long givenBuyerId = 60L;
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "id", givenPurchaseItemId);
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", PurchaseBuilder.fullData().build());
    ReflectionTestUtils.setField(givenPurchaseItem.getPurchase(), "buyerId", givenBuyerId);
    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // - JsonUtil.convertJsonToObject() 세팅
    ProductDataForPurchase mockProductData = mock(ProductDataForPurchase.class);
    when(mockProductData.getSellerId()).thenReturn(givenSellerId);
    jsonUtil.when(() -> JsonUtil.convertJsonToObject(any(), any())).thenReturn(mockProductData);

    // when
    target.retrieveAllByPurchaseItem(
        givenBuyerId, givenPurchaseItemId, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> purchaseItemIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockRefundRetrieveRepository, times(1))
        .findByPurchaseItem(purchaseItemIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenPurchaseItemId, purchaseItemIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate", captoredPageRequest.getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("retrieveAllByPurchaseItem() : ")
  public void retrieveAllByPurchaseItem_otherMemberRefunds() throws IOException {
    // given
    // - 인자세팅
    long givenWrongMemberId = 3L;
    long givenPurchaseItemId = 5L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    // - memberService.findById() 세팅
    Member givenSeller = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenSeller, "id", givenWrongMemberId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenSeller));

    // - purchaseItemService.findById() 세팅
    long givenBuyerId = 60L;
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "id", givenPurchaseItemId);
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", PurchaseBuilder.fullData().build());
    ReflectionTestUtils.setField(givenPurchaseItem.getPurchase(), "buyerId", givenBuyerId);
    when(mockPurchaseItemFindService.findById(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // - JsonUtil.convertJsonToObject() 세팅
    long givenSellerId = 70L;
    ProductDataForPurchase mockProductData = mock(ProductDataForPurchase.class);
    when(mockProductData.getSellerId()).thenReturn(givenSellerId);
    jsonUtil.when(() -> JsonUtil.convertJsonToObject(any(), any())).thenReturn(mockProductData);

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.retrieveAllByPurchaseItem(
                givenBuyerId, givenPurchaseItemId, givenSliceNum, givenSliceSize));
  }
}
