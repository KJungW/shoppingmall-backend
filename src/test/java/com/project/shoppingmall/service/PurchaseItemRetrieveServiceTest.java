package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.dto.refund.RefundPurchaseItemForSeller;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseItemRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemRetrieveService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.ProductBuilder;
import com.project.shoppingmall.testdata.PurchaseBuilder;
import com.project.shoppingmall.testdata.PurchaseItemBuilder;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.util.JsonUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

class PurchaseItemRetrieveServiceTest {
  private PurchaseItemRetrieveService target;
  private PurchaseItemRetrieveRepository mockPurchaseItemRetrieveRepository;
  private MemberFindService mockMemberFindService;
  private ProductFindService mockProductFindService;

  @BeforeEach
  public void beforeEach() {
    mockPurchaseItemRetrieveRepository = mock(PurchaseItemRetrieveRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    mockProductFindService = mock(ProductFindService.class);
    target =
        new PurchaseItemRetrieveService(
            mockPurchaseItemRetrieveRepository, mockMemberFindService, mockProductFindService);
  }

  @Test
  @DisplayName("retrieveAllByProduct() : 정상흐름")
  public void retrieveAllByProduct_ok() throws IOException {
    // given
    long givenMemberId = 10L;
    long givenProductId = 20L;
    int givenSliceNumber = 0;
    int givenSliceSize = 5;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    Product givenProduct = ProductBuilder.fullData().seller(givenMember).build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    when(mockProductFindService.findById(any())).thenReturn(Optional.of(givenProduct));

    // when
    target.retrieveAllForSeller(givenMemberId, givenProductId, givenSliceNumber, givenSliceSize);

    // then
    ArgumentCaptor<Long> productIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseItemRetrieveRepository, times(1))
        .findAllForSeller(productIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductId, productIdCaptor.getValue());

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
  @DisplayName("retrieveAllByProduct() : 다른 회원의 판매 제품에 대한 구매목록 조회 시도")
  public void retrieveAllByProduct_otherMemberProduct() throws IOException {
    // given
    long givenMemberId = 10L;
    long givenProductId = 20L;
    int givenSliceNumber = 0;
    int givenSliceSize = 5;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    long wrongMemberId = 2L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", wrongMemberId);
    when(mockProductFindService.findById(any())).thenReturn(Optional.of(givenProduct));

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.retrieveAllForSeller(
                givenMemberId, givenProductId, givenSliceNumber, givenSliceSize));
  }

  @Test
  @DisplayName("retrieveRefundedAllForBuyer() : 정상흐름'")
  public void retrieveRefundedAllForBuyer_ok() {
    // given
    long givenBuyerId = 30L;
    int givenSliceNumber = 1;
    int givenSliceSize = 10;

    Member givenBuyer = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBuyer, "id", givenBuyerId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenBuyer));

    // when
    target.retrieveRefundedAllForBuyer(givenBuyerId, givenSliceNumber, givenSliceSize);

    // then
    ArgumentCaptor<Long> buyerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseItemRetrieveRepository, times(1))
        .findRefundedAllForBuyer(buyerIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenBuyerId, buyerIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNumber, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("finalRefundCreatedDate").getDirection());
    assertEquals(
        "finalRefundCreatedDate",
        captoredPageRequest.getSort().getOrderFor("finalRefundCreatedDate").getProperty());
  }

  @Test
  @DisplayName("retrieveRefundedAllForSeller() : 정상흐름")
  public void retrieveRefundedAllForSeller_ok() throws IOException {
    // given
    long inputSellerId = 22L;
    int inputSliceNumber = 1;
    int inputSliceSize = 6;

    Member givenSeller = MemberBuilder.makeMember(inputSellerId, LoginType.NAVER);
    List<Member> givenBuyerList = setBuyerList(List.of(1L, 2L, 3L));
    List<PurchaseItem> givenPurchaseItemList =
        setTwoPurchaseItemsPerBuyer(givenBuyerList, givenSeller);
    Slice<PurchaseItem> givenMockSlice =
        setSlice(inputSliceNumber, inputSliceSize, givenPurchaseItemList);

    set_memberFindService_findById(givenSeller);
    set_purchaseItemRetrieveRepository_findRefundedAllForSeller(givenMockSlice);
    set_memberFindService_findAllByIds(givenBuyerList);

    // when
    SliceResult<RefundPurchaseItemForSeller> resultRetrieveResult =
        target.retrieveRefundedAllForSeller(inputSellerId, inputSliceNumber, inputSliceSize);

    // then
    check_purchaseItemRetrieveRepository_findRefundedAllForSeller(
        givenSeller.getId(), inputSliceNumber, inputSliceSize, "finalRefundCreatedDate");
    check_purchaseItemRetrieveService_retrieveRefundedAllForSeller_result(
        givenBuyerList, givenPurchaseItemList, resultRetrieveResult);
  }

  @Test
  @DisplayName("retrieveRefundedAllForSeller() : Buyer가 이미 삭제된 PurchaseItem 조회")
  public void retrieveRefundedAllForSeller_alreadyDeletedBuyer() throws IOException {
    // given
    long inputSellerId = 22L;
    int inputSliceNumber = 1;
    int inputSliceSize = 6;

    Member givenSeller = MemberBuilder.makeMember(inputSellerId, LoginType.NAVER);
    List<Member> givenBuyerList = setBuyerList(List.of(1L, 2L, 3L));
    List<PurchaseItem> givenPurchaseItemList =
        setTwoPurchaseItemsPerBuyer(givenBuyerList, givenSeller);
    givenPurchaseItemList.add(setPurchaseItemWithDeletedBuyer(2142L, 644234L, givenSeller));
    Slice<PurchaseItem> givenMockSlice =
        setSlice(inputSliceNumber, inputSliceSize, givenPurchaseItemList);

    set_memberFindService_findById(givenSeller);
    set_purchaseItemRetrieveRepository_findRefundedAllForSeller(givenMockSlice);
    set_memberFindService_findAllByIds(givenBuyerList);

    // when
    SliceResult<RefundPurchaseItemForSeller> resultRetrieveResult =
        target.retrieveRefundedAllForSeller(inputSellerId, inputSliceNumber, inputSliceSize);

    // then
    check_purchaseItemRetrieveRepository_findRefundedAllForSeller(
        givenSeller.getId(), inputSliceNumber, inputSliceSize, "finalRefundCreatedDate");
    check_purchaseItemRetrieveService_retrieveRefundedAllForSeller_result(
        givenBuyerList, givenPurchaseItemList, resultRetrieveResult);
  }

  public List<Member> setBuyerList(List<Long> idList) {
    return idList.stream().map(id -> MemberBuilder.makeMember(id, LoginType.NAVER)).toList();
  }

  public PurchaseItem setPurchaseItemWithDeletedBuyer(
      long deletedBuyerId, long purchaseItemId, Member givenSeller) throws IOException {
    Member givenDeletedBuyer = MemberBuilder.makeMember(deletedBuyerId, LoginType.NAVER);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(purchaseItemId, givenSeller);
    Purchase givenPurchase =
        PurchaseBuilder.makeCompleteStatePurchase(
            1234L, givenDeletedBuyer, new ArrayList<>(List.of(givenPurchaseItem)));
    return givenPurchaseItem;
  }

  public List<PurchaseItem> setTwoPurchaseItemsPerBuyer(
      List<Member> givenBuyerList, Member givenSeller) throws IOException {
    List<PurchaseItem> givenPurchaseItemList = new ArrayList<>();
    for (int i = 0; i < givenBuyerList.size(); i++) {
      Member givenBuyer = givenBuyerList.get(i);
      PurchaseItem givenPurchase1 = PurchaseItemBuilder.makePurchaseItem((long) i, givenSeller);
      PurchaseItem givenPurchase2 =
          PurchaseItemBuilder.makePurchaseItem((long) i * 1000, givenSeller);
      Purchase givenPurchase =
          PurchaseBuilder.makeCompleteStatePurchase(
              i * 1000L, givenBuyer, new ArrayList<>(List.of(givenPurchase1, givenPurchase2)));
      givenPurchaseItemList.add(givenPurchase1);
      givenPurchaseItemList.add(givenPurchase2);
    }
    return givenPurchaseItemList;
  }

  public <T> Slice<T> setSlice(int givenSliceNum, int givenSliceSize, List<T> contents) {
    Slice<T> mockSlice = mock(Slice.class);
    when(mockSlice.getNumber()).thenReturn(givenSliceNum);
    when(mockSlice.getSize()).thenReturn(givenSliceSize);
    when(mockSlice.isFirst()).thenReturn(false);
    when(mockSlice.isLast()).thenReturn(false);
    when(mockSlice.hasNext()).thenReturn(true);
    when(mockSlice.hasPrevious()).thenReturn(true);
    when(mockSlice.getContent()).thenReturn(contents);
    return mockSlice;
  }

  public void set_memberFindService_findById(Member givenMember) {
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
  }

  public void set_purchaseItemRetrieveRepository_findRefundedAllForSeller(
      Slice<PurchaseItem> givenMockSlice) {
    when(mockPurchaseItemRetrieveRepository.findRefundedAllForSeller(anyLong(), any()))
        .thenReturn(givenMockSlice);
  }

  public void set_memberFindService_findAllByIds(List<Member> givenBuyerList) {
    when(mockMemberFindService.findAllByIds(anyList())).thenReturn(givenBuyerList);
  }

  public void checkRefundPurchaseItemForSeller(
      PurchaseItem expectedPurchaseItem, RefundPurchaseItemForSeller realTarget) {
    Purchase expectedPurchase = expectedPurchaseItem.getPurchase();
    ProductDataForPurchase expectedProductData =
        JsonUtil.convertJsonToObject(
            expectedPurchaseItem.getProductData(), ProductDataForPurchase.class);

    assertEquals(expectedPurchase.getId(), realTarget.getPurchaseId());
    assertEquals(expectedPurchase.getBuyerId(), realTarget.getBuyerId());
    assertNull(realTarget.getBuyerName());
    assertEquals(
        expectedPurchase.getDeliveryInfo().getSenderName(),
        realTarget.getDeliveryInfo().getSenderName());
    assertEquals(
        expectedPurchase.getDeliveryInfo().getSenderAddress(),
        realTarget.getDeliveryInfo().getSenderAddress());
    assertEquals(
        expectedPurchase.getDeliveryInfo().getSenderPostCode(),
        realTarget.getDeliveryInfo().getSenderPostCode());
    assertEquals(
        expectedPurchase.getDeliveryInfo().getSenderTel(),
        realTarget.getDeliveryInfo().getSenderTel());
    assertEquals(expectedPurchase.getCreateDate(), realTarget.getPurchaseDateTime());

    assertEquals(expectedPurchaseItem.getId(), realTarget.getPurchaseItemId());
    assertEquals(expectedProductData.getProductId(), realTarget.getProductId());
    assertEquals(expectedProductData.getSellerId(), realTarget.getSellerId());
    assertEquals(expectedProductData.getSellerName(), realTarget.getSellerName());
    assertEquals(expectedProductData.getProductName(), realTarget.getProductName());
    assertEquals(expectedProductData.getProductTypeName(), realTarget.getProductTypeName());

    if (realTarget.getSelectedSingleOption() != null)
      assertEquals(
          expectedProductData.getSingleOption().getOptionId(),
          realTarget.getSelectedSingleOption().getOptionId());
    if (realTarget.getSelectedMultiOptions() != null
        && !realTarget.getSelectedMultiOptions().isEmpty())
      assertArrayEquals(
          expectedProductData.getMultiOptions().stream()
              .map(ProductOptionDto::getOptionId)
              .toArray(),
          realTarget.getSelectedMultiOptions().stream()
              .map(ProductOptionDto::getOptionId)
              .toArray());

    assertEquals(expectedProductData.getPrice(), realTarget.getPrice());
    assertEquals(expectedProductData.getDiscountAmount(), realTarget.getDiscountAmount());
    assertEquals(expectedProductData.getDiscountRate(), realTarget.getDiscountRate());
    assertEquals(expectedPurchaseItem.getFinalPrice(), realTarget.getFinalPrice());
    assertEquals(expectedPurchaseItem.getIsRefund(), realTarget.isRefund());
    assertEquals(expectedPurchaseItem.getFinalRefundState(), realTarget.getRefundState());
  }

  public void checkRefundPurchaseItemForSeller(
      PurchaseItem expectedPurchaseItem,
      Member expectedBuyer,
      RefundPurchaseItemForSeller realTarget) {
    Purchase expectedPurchase = expectedPurchaseItem.getPurchase();
    ProductDataForPurchase expectedProductData =
        JsonUtil.convertJsonToObject(
            expectedPurchaseItem.getProductData(), ProductDataForPurchase.class);

    assertEquals(expectedPurchase.getId(), realTarget.getPurchaseId());
    assertEquals(expectedPurchase.getBuyerId(), realTarget.getBuyerId());
    assertEquals(expectedBuyer.getNickName(), realTarget.getBuyerName());
    assertEquals(
        expectedPurchase.getDeliveryInfo().getSenderName(),
        realTarget.getDeliveryInfo().getSenderName());
    assertEquals(
        expectedPurchase.getDeliveryInfo().getSenderAddress(),
        realTarget.getDeliveryInfo().getSenderAddress());
    assertEquals(
        expectedPurchase.getDeliveryInfo().getSenderPostCode(),
        realTarget.getDeliveryInfo().getSenderPostCode());
    assertEquals(
        expectedPurchase.getDeliveryInfo().getSenderTel(),
        realTarget.getDeliveryInfo().getSenderTel());
    assertEquals(expectedPurchase.getCreateDate(), realTarget.getPurchaseDateTime());

    assertEquals(expectedPurchaseItem.getId(), realTarget.getPurchaseItemId());
    assertEquals(expectedProductData.getProductId(), realTarget.getProductId());
    assertEquals(expectedProductData.getSellerId(), realTarget.getSellerId());
    assertEquals(expectedProductData.getSellerName(), realTarget.getSellerName());
    assertEquals(expectedProductData.getProductName(), realTarget.getProductName());
    assertEquals(expectedProductData.getProductTypeName(), realTarget.getProductTypeName());

    if (realTarget.getSelectedSingleOption() != null)
      assertEquals(
          expectedProductData.getSingleOption().getOptionId(),
          realTarget.getSelectedSingleOption().getOptionId());
    if (realTarget.getSelectedMultiOptions() != null
        && !realTarget.getSelectedMultiOptions().isEmpty())
      assertArrayEquals(
          expectedProductData.getMultiOptions().stream()
              .map(ProductOptionDto::getOptionId)
              .toArray(),
          realTarget.getSelectedMultiOptions().stream()
              .map(ProductOptionDto::getOptionId)
              .toArray());

    assertEquals(expectedProductData.getPrice(), realTarget.getPrice());
    assertEquals(expectedProductData.getDiscountAmount(), realTarget.getDiscountAmount());
    assertEquals(expectedProductData.getDiscountRate(), realTarget.getDiscountRate());
    assertEquals(expectedPurchaseItem.getFinalPrice(), realTarget.getFinalPrice());
    assertEquals(expectedPurchaseItem.getIsRefund(), realTarget.isRefund());
    assertEquals(expectedPurchaseItem.getFinalRefundState(), realTarget.getRefundState());
  }

  public void check_purchaseItemRetrieveService_retrieveRefundedAllForSeller_result(
      List<Member> givenBuyerList,
      List<PurchaseItem> givenPurchaseItemList,
      SliceResult<RefundPurchaseItemForSeller> resultRetrieveResult) {
    List<RefundPurchaseItemForSeller> realContents = resultRetrieveResult.getContentList();
    realContents.forEach(
        realContent -> {
          PurchaseItem expectedPurchaseItem =
              givenPurchaseItemList.stream()
                  .filter(
                      givenPurchaseItem ->
                          realContent.getPurchaseItemId() == givenPurchaseItem.getId())
                  .findFirst()
                  .get();
          Optional<Member> expectedBuyer =
              givenBuyerList.stream()
                  .filter(givenBuyer -> realContent.getBuyerId() == givenBuyer.getId())
                  .findFirst();

          if (expectedBuyer.isPresent())
            checkRefundPurchaseItemForSeller(
                expectedPurchaseItem, expectedBuyer.get(), realContent);
          else checkRefundPurchaseItemForSeller(expectedPurchaseItem, realContent);
        });
  }

  public void check_purchaseItemRetrieveRepository_findRefundedAllForSeller(
      long givenSellerId, int givenSliceNum, int givenSliceSize, String givenSortField) {
    ArgumentCaptor<Long> sellerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseItemRetrieveRepository, times(1))
        .findRefundedAllForSeller(sellerIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenSellerId, sellerIdCaptor.getValue());

    PageRequest realPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, realPageRequest.getPageNumber());
    assertEquals(givenSliceSize, realPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC, realPageRequest.getSort().getOrderFor(givenSortField).getDirection());
    assertEquals(
        givenSortField, realPageRequest.getSort().getOrderFor(givenSortField).getProperty());
  }
}
