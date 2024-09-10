package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.BasketItemRetrieveRepository;
import com.project.shoppingmall.service.basket_item.BasketItemRetrieveService;
import com.project.shoppingmall.service.basket_item.BasketItemService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.testdata.BasketItemBuilder;
import com.project.shoppingmall.testdata.MemberBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class BasketItemRetrieveServiceTest {
  private BasketItemRetrieveService target;
  private BasketItemService mockBasketItemService;
  private MemberFindService mockMemberFindService;
  private BasketItemRetrieveRepository mockBasketItemRetrieveRepository;

  @BeforeEach
  public void beforeEach() {
    mockBasketItemService = mock(BasketItemService.class);
    mockMemberFindService = mock(MemberFindService.class);
    mockBasketItemRetrieveRepository = mock(BasketItemRetrieveRepository.class);
    target =
        new BasketItemRetrieveService(
            mockBasketItemService, mockMemberFindService, mockBasketItemRetrieveRepository);
  }

  @Test
  @DisplayName("getBasketItemDetail() : 정상흐름")
  public void getBasketItemDetail_ok() {
    // given
    // - 인자 세팅
    Long rightMemberId = 10L;
    Long rightBasketId = 20L;

    // - memberService.findById 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - basketItemRetrieveRepository.retrieveBasketItemDetail() 세팅
    BasketItem givenBasketItem = BasketItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBasketItem, "id", rightBasketId);
    ReflectionTestUtils.setField(givenBasketItem.getMember(), "id", rightMemberId);
    when(mockBasketItemRetrieveRepository.retrieveBasketItemDetail(anyLong()))
        .thenReturn(Optional.of(givenBasketItem));

    // - basketItemService.calculateBasketItemPrice() 세팅
    when(mockBasketItemService.calculateBasketItemPrice(any()))
        .thenReturn(mock(BasketItemPriceCalcResult.class));

    // when
    target.getBasketItemDetail(rightMemberId, rightBasketId);

    // then
    ArgumentCaptor<BasketItem> basketItemCaptor = ArgumentCaptor.forClass(BasketItem.class);
    verify(mockBasketItemService, times(1)).calculateBasketItemPrice(basketItemCaptor.capture());
    BasketItem basketItemArg = basketItemCaptor.getValue();
    assertEquals(rightBasketId, basketItemArg.getId());
    assertEquals(rightMemberId, basketItemArg.getMember().getId());
  }

  @Test
  @DisplayName("getBasketItemDetail() : 다른 회원의 장바구니 아이템 조회 시도")
  public void getBasketItemDetail_OtherMemberBasketItem() {
    // given
    // - 인자 세팅
    Long rightMemberId = 10L;
    Long rightBasketId = 20L;
    Long wrongBasketMemberId = 30L;

    // - memberService.findById 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - basketItemRetrieveRepository.retrieveBasketItemDetail() 세팅
    BasketItem givenBasketItem = BasketItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenBasketItem, "id", rightBasketId);
    ReflectionTestUtils.setField(givenBasketItem.getMember(), "id", wrongBasketMemberId);
    when(mockBasketItemRetrieveRepository.retrieveBasketItemDetail(anyLong()))
        .thenReturn(Optional.of(givenBasketItem));

    // when
    assertThrows(
        DataNotFound.class, () -> target.getBasketItemDetail(rightMemberId, rightBasketId));
  }

  @Test
  @DisplayName("getBasket() : 정상흐름")
  public void getBasket_ok() {
    // given
    // - 인자 세팅
    Long rightMemberId = 10L;

    // - memberService.findById 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - basketItemRetrieveRepository.retrieveBasketByMemberId() 세팅
    int givenBasketItemSize = 5;
    List<BasketItem> givenBasketItems = new ArrayList<>();
    for (int i = 0; i < givenBasketItemSize; i++) {
      BasketItem givenBasketItem = BasketItemBuilder.fullData().build();
      ReflectionTestUtils.setField(givenBasketItem, "id", (long) i);
      ReflectionTestUtils.setField(givenBasketItem.getMember(), "id", rightMemberId);
      givenBasketItems.add(givenBasketItem);
    }
    when(mockBasketItemRetrieveRepository.retrieveBasketByMemberId(anyLong()))
        .thenReturn(givenBasketItems);

    // - basketItemService.calculateBasketItemPrice() 세팅
    when(mockBasketItemService.calculateBasketItemPrice(any()))
        .thenReturn(mock(BasketItemPriceCalcResult.class));

    // when
    target.getBasket(rightMemberId);

    // then
    ArgumentCaptor<BasketItem> basketItemCaptor = ArgumentCaptor.forClass(BasketItem.class);
    verify(mockBasketItemService, times(givenBasketItemSize))
        .calculateBasketItemPrice(basketItemCaptor.capture());
    List<BasketItem> basketItemsArg = basketItemCaptor.getAllValues();

    List<Long> expectedBasketItemIds = givenBasketItems.stream().map(BasketItem::getId).toList();
    List<Long> argBasketItemIds = basketItemsArg.stream().map(BasketItem::getId).toList();
    assertArrayEquals(expectedBasketItemIds.toArray(), argBasketItemIds.toArray());

    List<BasketItem> incorrectMemberBasketItems =
        basketItemsArg.stream()
            .filter(basketItem -> !basketItem.getMember().getId().equals(rightMemberId))
            .toList();
    assertTrue(incorrectMemberBasketItems.isEmpty());
  }

  @Test
  @DisplayName("getBasket() : 장바구니에 아이템이 없을 경우")
  public void getBasket_NoItem() {
    // given
    // - 인자 세팅
    Long rightMemberId = 10L;

    // - memberService.findById 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - basketItemRetrieveRepository.retrieveBasketByMemberId() 세팅
    when(mockBasketItemRetrieveRepository.retrieveBasketByMemberId(anyLong()))
        .thenReturn(new ArrayList<>());

    // when
    target.getBasket(rightMemberId);

    // then
    verify(mockBasketItemService, times(0)).calculateBasketItemPrice(any());
  }
}
