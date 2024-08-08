package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.dto.delivery.DeliveryDto;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.dto.purchase.PurchaseItemMakeData;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.PurchaseRepository;
import com.project.shoppingmall.testdata.BasketItemBuilder;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.PurchaseBuilder;
import com.project.shoppingmall.type.PaymentResultType;
import com.project.shoppingmall.type.PurchaseStateType;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class PurchaseServiceTest {
  private PurchaseService target;
  private MemberService mockMemberService;
  private BasketItemService mockBasketItemService;
  private PurchaseRepository mockPurchaseRepository;
  private IamportClient mockIamportClient;

  @BeforeEach
  public void beforeEach() {
    mockMemberService = mock(MemberService.class);
    mockBasketItemService = mock(BasketItemService.class);
    mockPurchaseRepository = mock(PurchaseRepository.class);
    mockIamportClient = mock(IamportClient.class);
    target =
        new PurchaseService(
            mockMemberService, mockBasketItemService, mockPurchaseRepository, mockIamportClient);
  }

  @Test
  @DisplayName("readyPurchase() : 정상흐름")
  public void readyPurchase_ok() {
    // given
    // - memberId 인자 세팅
    Long givenMemberId = 2L;

    // - purchaseItemMakeDataList 인자 세팅
    List<Long> givenBasketItemIdList = new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L));
    List<PurchaseItemMakeData> givenPurchaseItemMakeData = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      PurchaseItemMakeData makeData = new PurchaseItemMakeData(givenBasketItemIdList.get(i), 10000);
      givenPurchaseItemMakeData.add(makeData);
    }

    // - deliveryDto 인자 세팅
    DeliveryDto givenDeliveryDto =
        new DeliveryDto("Kim", "test도 test시 test아파트 000호", "10101", "010-000-0000");

    // - memberService.findAllById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - basketItemService.findAllById() 세팅
    List<BasketItem> givenBasketItemList = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      BasketItem givenBasketItem = BasketItemBuilder.fullData().build();
      ReflectionTestUtils.setField(givenBasketItem, "id", givenBasketItemIdList.get(i));
      ReflectionTestUtils.setField(givenBasketItem.getMember(), "id", givenMemberId);
      givenBasketItemList.add(givenBasketItem);
    }
    when(mockBasketItemService.findAllById(any())).thenReturn(givenBasketItemList);

    // - basketItemService.validateMemberIsBasketItemOwner() 세팅
    doNothing().when(mockBasketItemService).validateMemberIsBasketItemOwner(any(), any());

    // - basketItemService.calculateBasketItemPrice 세팅
    BasketItemPriceCalcResult givenCalcResult =
        new BasketItemPriceCalcResult(
            10000,
            true,
            new ProductOptionDto(1L, "싱글옵션1", 1000),
            new ArrayList<>(
                Arrays.asList(
                    new ProductOptionDto(1L, "싱글옵션1", 1000),
                    new ProductOptionDto(1L, "싱글옵션1", 1000),
                    new ProductOptionDto(1L, "싱글옵션1", 1000))));
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(givenCalcResult);

    // when
    Purchase purchase =
        target.readyPurchase(givenMemberId, givenPurchaseItemMakeData, givenDeliveryDto);

    // then
    assertEquals(givenMemberId, purchase.getBuyer().getId());
    assertNotNull(purchase.getPurchaseUid());
    assertNull(purchase.getPaymentUid());
    for (PurchaseItem item : purchase.getPurchaseItems()) {
      assertEquals(givenCalcResult.getPrice(), item.getFinalPrice());
    }
    assertEquals(PurchaseStateType.READY, purchase.getState());
    assertEquals(givenDeliveryDto.getSenderName(), purchase.getDeliveryInfo().getSenderName());
    assertEquals(
        givenDeliveryDto.getSenderAddress(), purchase.getDeliveryInfo().getSenderAddress());
    assertEquals(
        givenDeliveryDto.getSenderPostCode(), purchase.getDeliveryInfo().getSenderPostCode());
    assertEquals(givenDeliveryDto.getSenderTel(), purchase.getDeliveryInfo().getSenderTel());
    assertEquals(
        givenCalcResult.getPrice() * givenBasketItemIdList.size(), purchase.getTotalPrice());
  }

  @Test
  @DisplayName("readyPurchase() : 존재하지 않는 장바구니 아이템")
  public void readyPurchase_NoBasketItem() {
    // given
    // - memberId 인자 세팅
    Long givenMemberId = 2L;

    // - purchaseItemMakeDataList 인자 세팅
    List<Long> givenBasketItemIdList = new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L));
    List<PurchaseItemMakeData> givenPurchaseItemMakeData = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      PurchaseItemMakeData makeData = new PurchaseItemMakeData(givenBasketItemIdList.get(i), 10000);
      givenPurchaseItemMakeData.add(makeData);
    }

    // - deliveryDto 인자 세팅
    DeliveryDto givenDeliveryDto =
        new DeliveryDto("Kim", "test도 test시 test아파트 000호", "10101", "010-000-0000");

    // - memberService.findAllById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - basketItemService.findAllById() 세팅
    List<BasketItem> givenBasketItemList = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size() - 1; i++) {
      BasketItem givenBasketItem = BasketItemBuilder.fullData().build();
      ReflectionTestUtils.setField(givenBasketItem, "id", givenBasketItemIdList.get(i));
      ReflectionTestUtils.setField(givenBasketItem.getMember(), "id", givenMemberId);
      givenBasketItemList.add(givenBasketItem);
    }
    when(mockBasketItemService.findAllById(any())).thenReturn(givenBasketItemList);

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.readyPurchase(givenMemberId, givenPurchaseItemMakeData, givenDeliveryDto));
  }

  @Test
  @DisplayName("readyPurchase() : 다른사람의 장바구니 아이템")
  public void readyPurchase_otherMemberBasketItem() {
    // given
    // - memberId 인자 세팅
    Long givenMemberId = 2L;

    // - purchaseItemMakeDataList 인자 세팅
    List<Long> givenBasketItemIdList = new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L));
    List<PurchaseItemMakeData> givenPurchaseItemMakeData = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      PurchaseItemMakeData makeData = new PurchaseItemMakeData(givenBasketItemIdList.get(i), 10000);
      givenPurchaseItemMakeData.add(makeData);
    }

    // - deliveryDto 인자 세팅
    DeliveryDto givenDeliveryDto =
        new DeliveryDto("Kim", "test도 test시 test아파트 000호", "10101", "010-000-0000");

    // - memberService.findAllById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - basketItemService.findAllById() 세팅
    List<BasketItem> givenBasketItemList = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      BasketItem givenBasketItem = BasketItemBuilder.fullData().build();
      ReflectionTestUtils.setField(givenBasketItem, "id", givenBasketItemIdList.get(i));
      ReflectionTestUtils.setField(givenBasketItem.getMember(), "id", givenMemberId);
      givenBasketItemList.add(givenBasketItem);
    }
    when(mockBasketItemService.findAllById(any())).thenReturn(givenBasketItemList);

    // - basketItemService.validateMemberIsBasketItemOwner() 세팅
    doThrow(new DataNotFound("장바구니 아이템들이 유효하지 않습니다"))
        .when(mockBasketItemService)
        .validateMemberIsBasketItemOwner(any(), any());

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.readyPurchase(givenMemberId, givenPurchaseItemMakeData, givenDeliveryDto));
  }

  @Test
  @DisplayName("readyPurchase() : 유효하지 않은 장바구니 옵션값")
  public void readyPurchase_IncorrectBasketItemOption() {
    // given
    // - memberId 인자 세팅
    Long givenMemberId = 2L;

    // - purchaseItemMakeDataList 인자 세팅
    List<Long> givenBasketItemIdList = new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L));
    List<PurchaseItemMakeData> givenPurchaseItemMakeData = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      PurchaseItemMakeData makeData = new PurchaseItemMakeData(givenBasketItemIdList.get(i), 10000);
      givenPurchaseItemMakeData.add(makeData);
    }

    // - deliveryDto 인자 세팅
    DeliveryDto givenDeliveryDto =
        new DeliveryDto("Kim", "test도 test시 test아파트 000호", "10101", "010-000-0000");

    // - memberService.findAllById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - basketItemService.findAllById() 세팅
    List<BasketItem> givenBasketItemList = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      BasketItem givenBasketItem = BasketItemBuilder.fullData().build();
      ReflectionTestUtils.setField(givenBasketItem, "id", givenBasketItemIdList.get(i));
      ReflectionTestUtils.setField(givenBasketItem.getMember(), "id", givenMemberId);
      givenBasketItemList.add(givenBasketItem);
    }
    when(mockBasketItemService.findAllById(any())).thenReturn(givenBasketItemList);

    // - basketItemService.validateMemberIsBasketItemOwner() 세팅
    doNothing().when(mockBasketItemService).validateMemberIsBasketItemOwner(any(), any());

    // - basketItemService.calculateBasketItemPrice 세팅
    Boolean givenWrongOptionAvailableValue = false;
    BasketItemPriceCalcResult givenCalcResult =
        new BasketItemPriceCalcResult(
            10000,
            givenWrongOptionAvailableValue,
            new ProductOptionDto(1L, "싱글옵션1", 1000),
            new ArrayList<>(
                Arrays.asList(
                    new ProductOptionDto(1L, "싱글옵션1", 1000),
                    new ProductOptionDto(1L, "싱글옵션1", 1000),
                    new ProductOptionDto(1L, "싱글옵션1", 1000))));
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(givenCalcResult);

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.readyPurchase(givenMemberId, givenPurchaseItemMakeData, givenDeliveryDto));
  }

  @Test
  @DisplayName("readyPurchase() : 예상가격과 실제 구매가격이 다름")
  public void readyPurchase_wrongExpectedPrice() {
    // given
    // - memberId 인자 세팅
    Long givenMemberId = 2L;

    // - purchaseItemMakeDataList 인자 세팅
    List<Long> givenBasketItemIdList = new ArrayList<>(Arrays.asList(1L, 2L, 3L, 4L));
    List<PurchaseItemMakeData> givenPurchaseItemMakeData = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      PurchaseItemMakeData makeData = new PurchaseItemMakeData(givenBasketItemIdList.get(i), 10000);
      givenPurchaseItemMakeData.add(makeData);
    }

    // - deliveryDto 인자 세팅
    DeliveryDto givenDeliveryDto =
        new DeliveryDto("Kim", "test도 test시 test아파트 000호", "10101", "010-000-0000");

    // - memberService.findAllById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - basketItemService.findAllById() 세팅
    List<BasketItem> givenBasketItemList = new ArrayList<>();
    for (int i = 0; i < givenBasketItemIdList.size(); i++) {
      BasketItem givenBasketItem = BasketItemBuilder.fullData().build();
      ReflectionTestUtils.setField(givenBasketItem, "id", givenBasketItemIdList.get(i));
      ReflectionTestUtils.setField(givenBasketItem.getMember(), "id", givenMemberId);
      givenBasketItemList.add(givenBasketItem);
    }
    when(mockBasketItemService.findAllById(any())).thenReturn(givenBasketItemList);

    // - basketItemService.validateMemberIsBasketItemOwner() 세팅
    doNothing().when(mockBasketItemService).validateMemberIsBasketItemOwner(any(), any());

    // - basketItemService.calculateBasketItemPrice 세팅
    Integer WrongPrice = 5000;
    BasketItemPriceCalcResult givenCalcResult =
        new BasketItemPriceCalcResult(
            WrongPrice,
            false,
            new ProductOptionDto(1L, "싱글옵션1", 1000),
            new ArrayList<>(
                Arrays.asList(
                    new ProductOptionDto(1L, "싱글옵션1", 1000),
                    new ProductOptionDto(1L, "싱글옵션1", 1000),
                    new ProductOptionDto(1L, "싱글옵션1", 1000))));
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(givenCalcResult);

    // when
    assertThrows(
        DataNotFound.class,
        () -> target.readyPurchase(givenMemberId, givenPurchaseItemMakeData, givenDeliveryDto));
  }

  @Test
  @DisplayName("completePurchase() : 정상흐름")
  public void completePurchase_ok() throws IOException {
    // given
    // - 인자세팅
    String givenPurchaseUid = "testPurchaseUid123123";
    String givenPaymentUid = "imp-12301519243012";

    int givenPrice = 40000;
    Purchase givenPurchase = PurchaseBuilder.fullData().totalPrice(givenPrice).build();
    ReflectionTestUtils.setField(givenPurchase, "purchaseUid", givenPurchaseUid);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.READY);
    when(mockPurchaseRepository.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));

    // - iamportClient.paymentByImpUid() 세팅
    IamportResponse mockPaymentResponse = mock(IamportResponse.class);
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(mockPaymentResponse);

    // - iamportClient.paymentByImpUid(paymentUid).getResponse() 세팅
    Payment mockRealPaymentData = mock(Payment.class);
    when(mockRealPaymentData.getStatus()).thenReturn("paid");
    when(mockRealPaymentData.getAmount()).thenReturn(BigDecimal.valueOf(givenPrice));
    when(mockPaymentResponse.getResponse()).thenReturn(mockRealPaymentData);

    // when
    PaymentResultType paymentResult = target.completePurchase(givenPurchaseUid, givenPaymentUid);

    // then
    assertEquals(PaymentResultType.COMPLETE, paymentResult);
    assertEquals(PurchaseStateType.COMPLETE, givenPurchase.getState());
    assertEquals(givenPaymentUid, givenPurchase.getPaymentUid());
  }

  @Test
  @DisplayName("completePurchase() : 구매UID에 해당하는 구매 데이터가 없음")
  public void completePurchase_noPurchase() {
    // given
    String givenPurchaseUid = "testPurchaseUid123123";
    String givenPaymentUid = "imp-12301519243012";

    // when
    when(mockPurchaseRepository.findByPurchaseUid(any())).thenReturn(Optional.empty());

    // when
    assertThrows(
        DataNotFound.class, () -> target.completePurchase(givenPurchaseUid, givenPaymentUid));
  }

  @Test
  @DisplayName("completePurchase() : 이미 처리완료된 구매 데이터에 대한 완료요청")
  public void completePurchase_alreadyProcessedPurchase() throws IOException {
    // given
    // - 인자세팅
    String givenPurchaseUid = "testPurchaseUid123123";
    String givenPaymentUid = "imp-12301519243012";

    int givenPrice = 40000;
    Purchase givenPurchase = PurchaseBuilder.fullData().totalPrice(givenPrice).build();
    ReflectionTestUtils.setField(givenPurchase, "purchaseUid", givenPurchaseUid);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.COMPLETE);
    when(mockPurchaseRepository.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));

    // when
    PaymentResultType paymentResult = target.completePurchase(givenPurchaseUid, givenPaymentUid);

    // then
    assertEquals(PaymentResultType.ALREADY_PROCESSED, paymentResult);
  }

  @Test
  @DisplayName("completePurchase() : 조회된 결제정보에서 경제상태가 'paid'가 아님")
  public void completePurchase_notPaid() throws IOException {
    // given
    // - 인자세팅
    String givenPurchaseUid = "testPurchaseUid123123";
    String givenPaymentUid = "imp-12301519243012";

    int givenPrice = 40000;
    Purchase givenPurchase = PurchaseBuilder.fullData().totalPrice(givenPrice).build();
    ReflectionTestUtils.setField(givenPurchase, "purchaseUid", givenPurchaseUid);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.READY);
    when(mockPurchaseRepository.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));

    // - iamportClient.paymentByImpUid() 세팅
    IamportResponse mockPaymentResponse = mock(IamportResponse.class);
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(mockPaymentResponse);

    // - iamportClient.paymentByImpUid(paymentUid).getResponse() 세팅
    Payment mockRealPaymentData = mock(Payment.class);
    when(mockRealPaymentData.getStatus()).thenReturn("cancel");
    when(mockRealPaymentData.getAmount()).thenReturn(BigDecimal.valueOf(givenPrice));
    when(mockPaymentResponse.getResponse()).thenReturn(mockRealPaymentData);

    // when
    PaymentResultType paymentResult = target.completePurchase(givenPurchaseUid, givenPaymentUid);

    // then
    assertEquals(PaymentResultType.FAIL_OR_CANCEL, paymentResult);
    assertEquals(PurchaseStateType.FAIL, givenPurchase.getState());
    assertEquals(givenPaymentUid, givenPurchase.getPaymentUid());
  }

  @Test
  @DisplayName("completePurchase() : 가격변조가 감지됨")
  public void completePurchase_detectPriceTampering() throws IOException {
    // given
    // - 인자세팅
    String givenPurchaseUid = "testPurchaseUid123123";
    String givenPaymentUid = "imp-12301519243012";

    int givenPrice = 40000;
    int wrongPrice = 30000;
    Purchase givenPurchase = PurchaseBuilder.fullData().totalPrice(givenPrice).build();
    ReflectionTestUtils.setField(givenPurchase, "purchaseUid", givenPurchaseUid);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.READY);
    when(mockPurchaseRepository.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));

    // - iamportClient.paymentByImpUid() 세팅
    IamportResponse mockPaymentResponse = mock(IamportResponse.class);
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(mockPaymentResponse);

    // - iamportClient.paymentByImpUid(paymentUid).getResponse() 세팅
    Payment mockRealPaymentData = mock(Payment.class);
    when(mockRealPaymentData.getStatus()).thenReturn("paid");
    when(mockRealPaymentData.getAmount()).thenReturn(BigDecimal.valueOf(wrongPrice));
    when(mockPaymentResponse.getResponse()).thenReturn(mockRealPaymentData);

    // when
    PaymentResultType paymentResult = target.completePurchase(givenPurchaseUid, givenPaymentUid);

    // then
    assertEquals(PaymentResultType.DETECTION_PRICE_TAMPERING, paymentResult);
    assertEquals(PurchaseStateType.DETECT_PRICE_TAMPERING, givenPurchase.getState());
    assertEquals(givenPaymentUid, givenPurchase.getPaymentUid());
  }

  @Test
  @DisplayName("completePurchase() : 잘못된 결제 UID")
  public void completePurchase_wrongPaymentUid() throws IOException {
    // given
    // - 인자세팅
    String givenPurchaseUid = "testPurchaseUid123123";
    String givenPaymentUid = "imp-12301519243012";

    int givenPrice = 40000;
    Purchase givenPurchase = PurchaseBuilder.fullData().totalPrice(givenPrice).build();
    ReflectionTestUtils.setField(givenPurchase, "purchaseUid", givenPurchaseUid);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.READY);
    when(mockPurchaseRepository.findByPurchaseUid(any())).thenReturn(Optional.of(givenPurchase));

    // - iamportClient.paymentByImpUid() 세팅
    when(mockIamportClient.paymentByImpUid(any())).thenReturn(null);

    // when
    assertThrows(
        DataNotFound.class, () -> target.completePurchase(givenPurchaseUid, givenPaymentUid));
  }
}
