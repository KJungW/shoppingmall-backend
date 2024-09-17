package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.dto.purchase.PurchaseItemDtoForSeller;
import com.project.shoppingmall.dto.refund.RefundPurchaseItemForSeller;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.value.DeliveryInfo;
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
import java.time.LocalDateTime;
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
  @DisplayName("retrieveAllForSeller() : 정상흐름")
  public void retrieveAllForSeller_ok() {
    // given
    long inputSellerId = 10L;
    long inputProductId = 20L;
    int inputSliceNumber = 0;
    int inputSliceSize = 5;

    Member givenSeller = MemberBuilder.makeMember(inputSellerId, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenSeller);
    List<PurchaseItem> givenPurchaseItems =
        setPurchaseItems(List.of(10L, 20L, 30L, 40L), givenProduct);
    Slice<PurchaseItem> givenMockSlice =
        setSlice(inputSliceNumber, inputSliceSize, givenPurchaseItems);

    set_memberFindService_findById(givenSeller);
    set_productFindService_findById(givenProduct);
    set_purchaseItemRetrieveRepository_findAllForSeller(givenMockSlice);

    // when
    SliceResult<PurchaseItemDtoForSeller> sliceResult =
        target.retrieveAllForSeller(
            inputSellerId, inputProductId, inputSliceNumber, inputSliceSize);

    // then
    check_purchaseItemRetrieveRepository_findAllForSeller(
        inputProductId, inputSliceNumber, inputSliceSize, "createDate");
    checkSliceResultWithPurchaseItemDtoForSeller(
        inputSliceNumber, inputSliceSize, givenPurchaseItems, sliceResult);
  }

  @Test
  @DisplayName("retrieveAllByProduct() : 다른 회원의 판매 제품에 대한 구매목록 조회 시도")
  public void retrieveAllByProduct_otherMemberProduct() {
    // given
    long inputSellerId = 10L;
    long inputProductId = 20L;
    int inputSliceNumber = 0;
    int inputSliceSize = 5;

    Member givenSeller = MemberBuilder.makeMember(inputSellerId, LoginType.NAVER);
    Member givenOtherSeller = MemberBuilder.makeMember(inputSellerId + 20, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenOtherSeller);
    List<PurchaseItem> givenPurchaseItems =
        setPurchaseItems(List.of(10L, 20L, 30L, 40L), givenProduct);
    Slice<PurchaseItem> givenMockSlice =
        setSlice(inputSliceNumber, inputSliceSize, givenPurchaseItems);

    set_memberFindService_findById(givenSeller);
    set_productFindService_findById(givenProduct);
    set_purchaseItemRetrieveRepository_findAllForSeller(givenMockSlice);

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.retrieveAllForSeller(
                inputSellerId, inputProductId, inputSliceNumber, inputSliceSize));
  }

  @Test
  @DisplayName("retrieveSalesInMonth() : 다른 회원의 판매 제품에 대한 구매목록 조회 시도")
  public void retrieveSalesInMonth_ok() {
    // given
    long inputSellerId = 10L;
    int inputYear = 2023;
    int inputMonth = 10;
    int inputSliceNumber = 0;
    int inputSliceSize = 5;

    Member givenSeller = MemberBuilder.makeMember(inputSellerId, LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(30L, givenSeller);
    List<PurchaseItem> givenPurchaseItems =
        setPurchaseItems(List.of(10L, 20L, 30L, 40L), givenProduct);
    Slice<PurchaseItem> givenMockSlice =
        setSlice(inputSliceNumber, inputSliceSize, givenPurchaseItems);

    set_purchaseItemRetrieveRepository_findAllForSellerBetweenDate(givenMockSlice);

    // when
    SliceResult<PurchaseItemDtoForSeller> sliceResult =
        target.retrieveAllForSellerByDate(
            inputSellerId, inputYear, inputMonth, inputSliceNumber, inputSliceSize);

    // then
    check_purchaseItemRetrieveRepository_findAllForSellerBetweenDate(
        inputSellerId, inputYear, inputMonth, inputSliceNumber, inputSliceSize, "createDate");
    checkSliceResultWithPurchaseItemDtoForSeller(
        inputSliceNumber, inputSliceSize, givenPurchaseItems, sliceResult);
  }

  @Test
  @DisplayName("retrieveRefundedAllForBuyer() : 정상흐름")
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
  public void retrieveRefundedAllForSeller_ok() {
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
  public void retrieveRefundedAllForSeller_alreadyDeletedBuyer() {
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

  public void set_memberFindService_findById(Member givenMember) {
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
  }

  public void set_purchaseItemRetrieveRepository_findRefundedAllForSeller(
      Slice<PurchaseItem> givenMockSlice) {
    when(mockPurchaseItemRetrieveRepository.findRefundedAllForSeller(anyLong(), any()))
        .thenReturn(givenMockSlice);
  }

  public void set_purchaseItemRetrieveRepository_findAllForSeller(
      Slice<PurchaseItem> givenMockSlice) {
    when(mockPurchaseItemRetrieveRepository.findAllForSeller(anyLong(), any()))
        .thenReturn(givenMockSlice);
  }

  public void set_purchaseItemRetrieveRepository_findAllForSellerBetweenDate(
      Slice<PurchaseItem> givenMockSlice) {
    when(mockPurchaseItemRetrieveRepository.findAllForSellerBetweenDate(
            anyLong(), any(), any(), any()))
        .thenReturn(givenMockSlice);
  }

  public void set_memberFindService_findAllByIds(List<Member> givenBuyerList) {
    when(mockMemberFindService.findAllByIds(anyList())).thenReturn(givenBuyerList);
  }

  private void set_productFindService_findById(Product givenProduct) {
    when(mockProductFindService.findById(any())).thenReturn(Optional.of(givenProduct));
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

  public void check_purchaseItemRetrieveRepository_findAllForSellerBetweenDate(
      long givenSellerId,
      int year,
      int month,
      int givenSliceNum,
      int givenSliceSize,
      String givenSortField) {
    ArgumentCaptor<Long> sellerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<LocalDateTime> startDataCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    ArgumentCaptor<LocalDateTime> endDateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseItemRetrieveRepository, times(1))
        .findAllForSellerBetweenDate(
            sellerIdCaptor.capture(),
            startDataCaptor.capture(),
            endDateCaptor.capture(),
            pageRequestCaptor.capture());

    assertEquals(givenSellerId, sellerIdCaptor.getValue());

    LocalDateTime expectedStartDate = LocalDateTime.of(year, month, 1, 0, 0);
    LocalDateTime expectedEndDate;
    if (month + 1 > 12) expectedEndDate = LocalDateTime.of(year + 1, 1, 1, 0, 0);
    else expectedEndDate = LocalDateTime.of(year, month + 1, 1, 0, 0);
    assertEquals(expectedStartDate, startDataCaptor.getValue());
    assertEquals(expectedEndDate, endDateCaptor.getValue());

    PageRequest realPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, realPageRequest.getPageNumber());
    assertEquals(givenSliceSize, realPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC, realPageRequest.getSort().getOrderFor(givenSortField).getDirection());
    assertEquals(
        givenSortField, realPageRequest.getSort().getOrderFor(givenSortField).getProperty());
  }

  public void check_purchaseItemRetrieveRepository_findAllForSeller(
      long givenProductId, int givenSliceNum, int givenSliceSize, String givenSortField) {
    ArgumentCaptor<Long> productIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockPurchaseItemRetrieveRepository, times(1))
        .findAllForSeller(productIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductId, productIdCaptor.getValue());

    PageRequest realPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, realPageRequest.getPageNumber());
    assertEquals(givenSliceSize, realPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC, realPageRequest.getSort().getOrderFor(givenSortField).getDirection());
    assertEquals(
        givenSortField, realPageRequest.getSort().getOrderFor(givenSortField).getProperty());
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

  public List<Member> setBuyerList(List<Long> idList) {
    return idList.stream().map(id -> MemberBuilder.makeMember(id, LoginType.NAVER)).toList();
  }

  public List<PurchaseItem> setPurchaseItems(List<Long> purchaseItemIds, Product givenProduct) {
    List<PurchaseItem> purchaseItemList =
        purchaseItemIds.stream()
            .map(id -> PurchaseItemBuilder.makePurchaseItem(id, givenProduct))
            .toList();
    Purchase givenPurchase = PurchaseBuilder.makPurchaseItem(1234L, purchaseItemList);
    return purchaseItemList;
  }

  public PurchaseItem setPurchaseItemWithDeletedBuyer(
      long deletedBuyerId, long purchaseItemId, Member givenSeller) {
    Member givenDeletedBuyer = MemberBuilder.makeMember(deletedBuyerId, LoginType.NAVER);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(purchaseItemId, givenSeller);
    Purchase givenPurchase =
        PurchaseBuilder.makeCompleteStatePurchase(
            1234L, givenDeletedBuyer, new ArrayList<>(List.of(givenPurchaseItem)));
    return givenPurchaseItem;
  }

  public List<PurchaseItem> setTwoPurchaseItemsPerBuyer(
      List<Member> givenBuyerList, Member givenSeller) {
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

  public void checkSliceResultWithPurchaseItemDtoForSeller(
      int givenSliceNum,
      int givenSliceSize,
      List<PurchaseItem> givenPurchaseItems,
      SliceResult<PurchaseItemDtoForSeller> sliceResult) {
    assertEquals(givenSliceNum, sliceResult.getCurrentSliceNumber());
    assertEquals(givenSliceSize, sliceResult.getSliceSize());

    sliceResult
        .getContentList()
        .forEach(
            dto -> {
              PurchaseItem givenPurchaseItem =
                  givenPurchaseItems.stream()
                      .filter(item -> item.getId().equals(dto.getPurchaseItemId()))
                      .findFirst()
                      .get();
              checkPurchaseItemDtoForSeller(givenPurchaseItem, dto);
            });
  }

  public void checkPurchaseItemDtoForSeller(
      PurchaseItem expectedPurchaseItem, PurchaseItemDtoForSeller realTarget) {
    Purchase expectedPurchase = expectedPurchaseItem.getPurchase();
    ProductDataForPurchase expectedProductData =
        JsonUtil.convertJsonToObject(
            expectedPurchaseItem.getProductData(), ProductDataForPurchase.class);

    assertEquals(expectedPurchase.getBuyerId(), realTarget.getBuyerId());
    checkDeliveryInfo(expectedPurchase.getDeliveryInfo(), realTarget.getDeliveryInfo());
    assertEquals(expectedPurchase.getCreateDate(), realTarget.getDateTime());
    assertEquals(expectedPurchaseItem.getId(), realTarget.getPurchaseItemId());
    assertEquals(expectedPurchaseItem.getProductId(), realTarget.getProductId());
    if (realTarget.getSelectedSingleOption() != null)
      checkProductSingleOption(
          expectedProductData.getSingleOption(), realTarget.getSelectedSingleOption());
    if (realTarget.getSelectedMultiOptions() != null
        && !realTarget.getSelectedMultiOptions().isEmpty())
      checkProductMultiOptions(
          expectedProductData.getMultiOptions(), realTarget.getSelectedMultiOptions());
    assertEquals(expectedProductData.getPrice(), realTarget.getPrice());
    assertEquals(expectedProductData.getDiscountAmount(), realTarget.getDiscountAmount());
    assertEquals(expectedProductData.getDiscountRate(), realTarget.getDiscountRate());
    assertEquals(expectedPurchaseItem.getFinalPrice(), realTarget.getFinalPrice());
    assertEquals(expectedPurchaseItem.getIsRefund(), realTarget.isRefund());
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
    checkDeliveryInfo(expectedPurchase.getDeliveryInfo(), realTarget.getDeliveryInfo());
    assertEquals(expectedPurchase.getCreateDate(), realTarget.getPurchaseDateTime());
    assertEquals(expectedPurchaseItem.getId(), realTarget.getPurchaseItemId());
    assertEquals(expectedProductData.getProductId(), realTarget.getProductId());
    assertEquals(expectedProductData.getSellerId(), realTarget.getSellerId());
    assertEquals(expectedProductData.getSellerName(), realTarget.getSellerName());
    assertEquals(expectedProductData.getProductName(), realTarget.getProductName());
    assertEquals(expectedProductData.getProductTypeName(), realTarget.getProductTypeName());
    if (realTarget.getSelectedSingleOption() != null)
      checkProductSingleOption(
          expectedProductData.getSingleOption(), realTarget.getSelectedSingleOption());
    if (realTarget.getSelectedMultiOptions() != null
        && !realTarget.getSelectedMultiOptions().isEmpty())
      checkProductMultiOptions(
          expectedProductData.getMultiOptions(), realTarget.getSelectedMultiOptions());
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
    checkDeliveryInfo(expectedPurchase.getDeliveryInfo(), realTarget.getDeliveryInfo());
    assertEquals(expectedPurchase.getCreateDate(), realTarget.getPurchaseDateTime());
    assertEquals(expectedPurchaseItem.getId(), realTarget.getPurchaseItemId());
    assertEquals(expectedProductData.getProductId(), realTarget.getProductId());
    assertEquals(expectedProductData.getSellerId(), realTarget.getSellerId());
    assertEquals(expectedProductData.getSellerName(), realTarget.getSellerName());
    assertEquals(expectedProductData.getProductName(), realTarget.getProductName());
    assertEquals(expectedProductData.getProductTypeName(), realTarget.getProductTypeName());
    if (realTarget.getSelectedSingleOption() != null)
      checkProductSingleOption(
          expectedProductData.getSingleOption(), realTarget.getSelectedSingleOption());
    if (realTarget.getSelectedMultiOptions() != null
        && !realTarget.getSelectedMultiOptions().isEmpty())
      checkProductMultiOptions(
          expectedProductData.getMultiOptions(), realTarget.getSelectedMultiOptions());
    assertEquals(expectedProductData.getPrice(), realTarget.getPrice());
    assertEquals(expectedProductData.getDiscountAmount(), realTarget.getDiscountAmount());
    assertEquals(expectedProductData.getDiscountRate(), realTarget.getDiscountRate());
    assertEquals(expectedPurchaseItem.getFinalPrice(), realTarget.getFinalPrice());
    assertEquals(expectedPurchaseItem.getIsRefund(), realTarget.isRefund());
    assertEquals(expectedPurchaseItem.getFinalRefundState(), realTarget.getRefundState());
  }

  public void checkDeliveryInfo(DeliveryInfo expectDeliveryInfo, DeliveryDto realDeliveryInfo) {
    assertEquals(expectDeliveryInfo.getSenderName(), realDeliveryInfo.getSenderName());
    assertEquals(expectDeliveryInfo.getSenderAddress(), realDeliveryInfo.getSenderAddress());
    assertEquals(expectDeliveryInfo.getSenderPostCode(), realDeliveryInfo.getSenderPostCode());
    assertEquals(expectDeliveryInfo.getSenderTel(), realDeliveryInfo.getSenderTel());
  }

  public void checkProductSingleOption(
      ProductOptionDto expectedOption, ProductOptionDto realOption) {
    assertEquals(expectedOption.getOptionId(), realOption.getOptionId());
    assertEquals(expectedOption.getOptionName(), realOption.getOptionName());
    assertEquals(expectedOption.getPriceChangeAmount(), realOption.getPriceChangeAmount());
  }

  public void checkProductMultiOptions(
      List<ProductOptionDto> expectedOptions, List<ProductOptionDto> realOptions) {
    assertArrayEquals(
        expectedOptions.stream().map(ProductOptionDto::getOptionId).toArray(),
        realOptions.stream().map(ProductOptionDto::getOptionId).toArray());
  }
}
