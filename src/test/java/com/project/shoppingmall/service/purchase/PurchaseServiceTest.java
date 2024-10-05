package com.project.shoppingmall.service.purchase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.purchase.PurchaseItemMakeData;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.CannotPurchaseBecauseMemberBan;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseRepository;
import com.project.shoppingmall.service.basket_item.BasketItemFindService;
import com.project.shoppingmall.service.basket_item.BasketItemService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.refund.RefundService;
import com.project.shoppingmall.test_dto.basket_item.BasketItemPriceCalcResultManager;
import com.project.shoppingmall.test_dto.delivery.DeliveryDtoManager;
import com.project.shoppingmall.test_entity.basketitem.BasketItemBuilder;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.product.ProductMultiOptionBuilder;
import com.project.shoppingmall.test_entity.product.ProductSingleOptionBuilder;
import com.project.shoppingmall.test_entity.purchase.PurchaseBuilder;
import com.project.shoppingmall.test_entity.purchase.PurchaseChecker;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemMakeDataBuilder;
import com.project.shoppingmall.type.PaymentResultType;
import com.project.shoppingmall.type.PurchaseStateType;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PurchaseServiceTest {
  private PurchaseService target;
  private MemberFindService mockMemberFindService;
  private BasketItemService mockBasketItemService;
  private BasketItemFindService mockBasketItemFindService;
  private PurchaseRepository mockPurchaseRepository;
  private PurchaseFindService mockPurchaseFindService;
  private IamportClient mockIamportClient;
  private RefundService mockrefundService;

  @BeforeEach
  public void beforeEach() {
    mockMemberFindService = mock(MemberFindService.class);
    mockBasketItemService = mock(BasketItemService.class);
    mockBasketItemFindService = mock(BasketItemFindService.class);
    mockPurchaseRepository = mock(PurchaseRepository.class);
    mockPurchaseFindService = mock(PurchaseFindService.class);
    mockIamportClient = mock(IamportClient.class);
    mockrefundService = mock(RefundService.class);

    target =
        new PurchaseService(
            mockMemberFindService,
            mockBasketItemService,
            mockBasketItemFindService,
            mockPurchaseRepository,
            mockPurchaseFindService,
            mockIamportClient,
            mockrefundService);
  }

  @Test
  @DisplayName("readyPurchase() : 정상흐름")
  public void readyPurchase_ok() {
    // given
    Long inputMemberId = 2L;
    List<PurchaseItemMakeData> inputPurchaseItemMakeData =
        PurchaseItemMakeDataBuilder.make(List.of(1L, 2L, 3L), List.of(3800, 3800, 3800));
    DeliveryDto inputDeliveryDto = DeliveryDtoManager.make();

    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = makeProduct();
    List<BasketItem> givenBasketItemList =
        makeBasketItemList(
            inputPurchaseItemMakeData, givenMember, givenProduct, 1L, List.of(1L, 2L));
    BasketItemPriceCalcResult priceCalcResult =
        BasketItemPriceCalcResultManager.make(true, givenBasketItemList.get(0), givenProduct);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemFindService.findAllById(any())).thenReturn(givenBasketItemList);
    doNothing().when(mockBasketItemService).validateMemberIsBasketItemOwner(any(), any());
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(priceCalcResult);

    // when
    Purchase result =
        target.readyPurchase(inputMemberId, inputPurchaseItemMakeData, inputDeliveryDto);

    // then
    PurchaseChecker.check(
        givenMember,
        inputPurchaseItemMakeData,
        givenBasketItemList,
        PurchaseStateType.READY,
        inputDeliveryDto,
        result);
  }

  @Test
  @DisplayName("readyPurchase() : 존재하지 않는 장바구니 아이템")
  public void readyPurchase_NoBasketItem() {
    Long inputMemberId = 2L;
    List<PurchaseItemMakeData> inputPurchaseItemMakeData =
        PurchaseItemMakeDataBuilder.make(List.of(1L, 2L, 3L), List.of(3800, 3800, 3800));
    DeliveryDto inputDeliveryDto = DeliveryDtoManager.make();

    Member givenMember = MemberBuilder.makeMember(inputMemberId);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemFindService.findAllById(any())).thenReturn(new ArrayList<>());

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.readyPurchase(inputMemberId, inputPurchaseItemMakeData, inputDeliveryDto));
  }

  @Test
  @DisplayName("readyPurchase() : 다른사람의 장바구니 아이템")
  public void readyPurchase_otherMemberBasketItem() {
    // given
    Long inputMemberId = 2L;
    List<PurchaseItemMakeData> inputPurchaseItemMakeData =
        PurchaseItemMakeDataBuilder.make(List.of(1L, 2L, 3L), List.of(3800, 3800, 3800));
    DeliveryDto inputDeliveryDto = DeliveryDtoManager.make();

    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Member otherMember = MemberBuilder.makeMember(1231L);
    Product givenProduct = makeProduct();
    List<BasketItem> givenBasketItemList =
        makeBasketItemList(
            inputPurchaseItemMakeData, otherMember, givenProduct, 1L, List.of(1L, 2L));

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemFindService.findAllById(any())).thenReturn(givenBasketItemList);
    doThrow(new DataNotFound("장바구니 아이템들이 유효하지 않습니다"))
        .when(mockBasketItemService)
        .validateMemberIsBasketItemOwner(any(), any());

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.readyPurchase(inputMemberId, inputPurchaseItemMakeData, inputDeliveryDto));
  }

  @Test
  @DisplayName("readyPurchase() : 유효하지 않은 장바구니 옵션값")
  public void readyPurchase_IncorrectBasketItemOption() {
    Long inputMemberId = 2L;
    List<PurchaseItemMakeData> inputPurchaseItemMakeData =
        PurchaseItemMakeDataBuilder.make(List.of(1L, 2L, 3L), List.of(3800, 3800, 3800));
    DeliveryDto inputDeliveryDto = DeliveryDtoManager.make();

    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = makeProduct();
    List<BasketItem> givenBasketItemList =
        makeBasketItemList(
            inputPurchaseItemMakeData, givenMember, givenProduct, 1L, List.of(5L, 2L));
    BasketItemPriceCalcResult priceCalcResult = BasketItemPriceCalcResultManager.make(3800, false);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemFindService.findAllById(any())).thenReturn(givenBasketItemList);
    doNothing().when(mockBasketItemService).validateMemberIsBasketItemOwner(any(), any());
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(priceCalcResult);
    // when
    assertThrows(
        DataNotFound.class,
        () -> target.readyPurchase(inputMemberId, inputPurchaseItemMakeData, inputDeliveryDto));
  }

  @Test
  @DisplayName("readyPurchase() : 예상가격과 실제 구매가격이 다름")
  public void readyPurchase_wrongExpectedPrice() {
    // given
    Long inputMemberId = 2L;
    List<PurchaseItemMakeData> inputPurchaseItemMakeData =
        PurchaseItemMakeDataBuilder.make(List.of(1L, 2L, 3L), List.of(3800, 3800, 3800));
    DeliveryDto inputDeliveryDto = DeliveryDtoManager.make();

    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = makeProduct();
    List<BasketItem> givenBasketItemList =
        makeBasketItemList(
            inputPurchaseItemMakeData, givenMember, givenProduct, 1L, List.of(1L, 2L));
    BasketItemPriceCalcResult priceCalcResult = BasketItemPriceCalcResultManager.make(5000, true);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemFindService.findAllById(any())).thenReturn(givenBasketItemList);
    doNothing().when(mockBasketItemService).validateMemberIsBasketItemOwner(any(), any());
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(priceCalcResult);

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.readyPurchase(inputMemberId, inputPurchaseItemMakeData, inputDeliveryDto));
  }

  @Test
  @DisplayName("readyPurchase() : 벤상태의 회원이 제품을 구매하려고 시도함")
  public void readyPurchase_bannedMember() {
    // given
    Long inputMemberId = 2L;
    List<PurchaseItemMakeData> inputPurchaseItemMakeData =
        PurchaseItemMakeDataBuilder.make(List.of(1L, 2L, 3L), List.of(3800, 3800, 3800));
    DeliveryDto inputDeliveryDto = DeliveryDtoManager.make();

    Member givenMember = MemberBuilder.makeMember(inputMemberId, true);
    Product givenProduct = makeProduct();
    List<BasketItem> givenBasketItemList =
        makeBasketItemList(
            inputPurchaseItemMakeData, givenMember, givenProduct, 1L, List.of(1L, 2L));
    BasketItemPriceCalcResult priceCalcResult =
        BasketItemPriceCalcResultManager.make(true, givenBasketItemList.get(0), givenProduct);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemFindService.findAllById(any())).thenReturn(givenBasketItemList);
    doNothing().when(mockBasketItemService).validateMemberIsBasketItemOwner(any(), any());
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(priceCalcResult);

    // when
    assertThrows(
        CannotPurchaseBecauseMemberBan.class,
        () -> target.readyPurchase(inputMemberId, inputPurchaseItemMakeData, inputDeliveryDto));
  }

  @Test
  @DisplayName("completePurchase() : 정상흐름")
  public void completePurchase_ok() {
    // given
    String inputPurchaseUid = "testPurchaseUid123123";
    String inputPaymentUid = "imp-12301519243012";

    int givenTotalPrice = 5000;
    Member givenBuyer = MemberBuilder.makeMember(102L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(132L, givenBuyer);
    Purchase givenPurchase =
        makePurchase(
            givenBuyer,
            givenPurchaseItem,
            inputPurchaseUid,
            PurchaseStateType.READY,
            givenTotalPrice);
    IamportResponse mockIamportResponse = makeMockIamPortResponse("paid", givenTotalPrice);

    when(mockPurchaseFindService.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(mockIamportResponse);

    // when
    PaymentResultType paymentResult = target.completePurchase(inputPurchaseUid, inputPaymentUid);

    // then
    assertEquals(PaymentResultType.COMPLETE, paymentResult);
    assertEquals(PurchaseStateType.COMPLETE, givenPurchase.getState());
    assertEquals(inputPaymentUid, givenPurchase.getPaymentUid());
  }

  @Test
  @DisplayName("completePurchase() : 구매UID에 해당하는 구매 데이터가 없음")
  public void completePurchase_noPurchase() {
    // given
    String inputPurchaseUid = "testPurchaseUid123123";
    String inputPaymentUid = "imp-12301519243012";

    // when
    when(mockPurchaseFindService.findByPurchaseUid(any())).thenReturn(Optional.empty());

    // when
    assertThrows(
        DataNotFound.class, () -> target.completePurchase(inputPurchaseUid, inputPaymentUid));
  }

  @Test
  @DisplayName("completePurchase() : 이미 처리완료된 구매 데이터에 대한 완료요청")
  public void completePurchase_alreadyProcessedPurchase() {
    // given
    String inputPurchaseUid = "testPurchaseUid123123";
    String inputPaymentUid = "imp-12301519243012";

    int givenTotalPrice = 5000;
    Member givenBuyer = MemberBuilder.makeMember(102L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(132L, givenBuyer);
    Purchase givenPurchase =
        makePurchase(
            givenBuyer,
            givenPurchaseItem,
            inputPurchaseUid,
            PurchaseStateType.COMPLETE,
            givenTotalPrice);
    IamportResponse mockIamportResponse = makeMockIamPortResponse("paid", givenTotalPrice);

    when(mockPurchaseFindService.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(mockIamportResponse);

    // when
    PaymentResultType paymentResult = target.completePurchase(inputPurchaseUid, inputPaymentUid);

    // then
    assertEquals(PaymentResultType.ALREADY_PROCESSED, paymentResult);
  }

  @Test
  @DisplayName("completePurchase() : 조회된 결제정보에서 경제상태가 'paid'가 아님")
  public void completePurchase_notPaid() {
    // given
    String inputPurchaseUid = "testPurchaseUid123123";
    String inputPaymentUid = "imp-12301519243012";

    int givenTotalPrice = 5000;
    Member givenBuyer = MemberBuilder.makeMember(102L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(132L, givenBuyer);
    Purchase givenPurchase =
        makePurchase(
            givenBuyer,
            givenPurchaseItem,
            inputPurchaseUid,
            PurchaseStateType.READY,
            givenTotalPrice);
    IamportResponse mockIamportResponse = makeMockIamPortResponse("not paid", givenTotalPrice);

    when(mockPurchaseFindService.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(mockIamportResponse);

    // when
    PaymentResultType paymentResult = target.completePurchase(inputPurchaseUid, inputPaymentUid);

    // then
    assertEquals(PaymentResultType.FAIL_OR_CANCEL, paymentResult);
    assertEquals(PurchaseStateType.FAIL, givenPurchase.getState());
    assertEquals(inputPaymentUid, givenPurchase.getPaymentUid());
  }

  @Test
  @DisplayName("completePurchase() : 가격변조가 감지됨")
  public void completePurchase_detectPriceTampering() {
    // given
    String inputPurchaseUid = "testPurchaseUid123123";
    String inputPaymentUid = "imp-12301519243012";

    int givenTotalPrice = 5000;
    Member givenBuyer = MemberBuilder.makeMember(102L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(132L, givenBuyer);
    Purchase givenPurchase =
        makePurchase(
            givenBuyer,
            givenPurchaseItem,
            inputPurchaseUid,
            PurchaseStateType.READY,
            givenTotalPrice);
    IamportResponse mockIamportResponse = makeMockIamPortResponse("paid", givenTotalPrice + 10000);

    when(mockPurchaseFindService.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(mockIamportResponse);

    // when
    PaymentResultType paymentResult = target.completePurchase(inputPurchaseUid, inputPaymentUid);

    // then
    assertEquals(PaymentResultType.DETECTION_PRICE_TAMPERING, paymentResult);
    assertEquals(PurchaseStateType.DETECT_PRICE_TAMPERING, givenPurchase.getState());
    assertEquals(inputPaymentUid, givenPurchase.getPaymentUid());
  }

  @Test
  @DisplayName("completePurchase() : 잘못된 결제 UID")
  public void completePurchase_wrongPaymentUid() {
    // given
    String inputPurchaseUid = "testPurchaseUid123123";
    String inputPaymentUid = "imp-12301519243012";

    int givenTotalPrice = 5000;
    Member givenBuyer = MemberBuilder.makeMember(102L);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(132L, givenBuyer);
    Purchase givenPurchase =
        makePurchase(
            givenBuyer,
            givenPurchaseItem,
            inputPurchaseUid,
            PurchaseStateType.READY,
            givenTotalPrice);

    when(mockPurchaseFindService.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(null);

    // when
    assertThrows(
        DataNotFound.class, () -> target.completePurchase(inputPurchaseUid, inputPaymentUid));
  }

  public Product makeProduct() {
    Member givenSeller = MemberBuilder.makeMember(14324L);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(1L, 2L, 3L), 1000);
    List<ProductMultipleOption> givenMultiOptions =
        ProductMultiOptionBuilder.makeList(List.of(1L, 2L, 3L), 1000);
    Product givenProduct =
        ProductBuilder.makeProduct(21321L, givenSeller, givenSingleOptions, givenMultiOptions);
    givenProduct.changePrice(1000, 100, 10D);
    return givenProduct;
  }

  public List<BasketItem> makeBasketItemList(
      List<PurchaseItemMakeData> makeData,
      Member buyer,
      Product product,
      long singleOptionId,
      List<Long> multiOptionIds) {
    List<Long> basketItemIdList =
        makeData.stream().map(PurchaseItemMakeData::getBasketItemId).toList();
    return basketItemIdList.stream()
        .map(
            id ->
                BasketItemBuilder.makeBasketItem(
                    id, buyer, product, singleOptionId, multiOptionIds))
        .toList();
  }

  public Purchase makePurchase(
      Member buyer,
      PurchaseItem purchaseItem,
      String purchaseUid,
      PurchaseStateType state,
      int totalPrice) {
    Purchase givenPurchase =
        PurchaseBuilder.makePurchase(12312L, buyer, List.of(purchaseItem), state);
    ReflectionTestUtils.setField(givenPurchase, "purchaseUid", purchaseUid);
    ReflectionTestUtils.setField(givenPurchase, "totalPrice", totalPrice);
    return givenPurchase;
  }

  public IamportResponse makeMockIamPortResponse(String status, int totalPrice) {
    IamportResponse mockPaymentResponse = mock(IamportResponse.class);
    Payment mockRealPaymentData = mock(Payment.class);
    when(mockRealPaymentData.getStatus()).thenReturn(status);
    when(mockRealPaymentData.getAmount()).thenReturn(BigDecimal.valueOf(totalPrice));
    when(mockPaymentResponse.getResponse()).thenReturn(mockRealPaymentData);
    return mockPaymentResponse;
  }
}
