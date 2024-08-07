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
import com.project.shoppingmall.type.PurchaseStateType;
import com.siot.IamportRestClient.IamportClient;
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
}
