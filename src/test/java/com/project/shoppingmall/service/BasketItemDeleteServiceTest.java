package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.BasketItemRepository;
import com.project.shoppingmall.service.basket_item.BasketItemDeleteService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.testdata.basketitem.BasketItemBuilder;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class BasketItemDeleteServiceTest {
  private BasketItemDeleteService target;
  private BasketItemRepository mockBasketItemRepository;
  private MemberFindService mockMemberFindService;

  @BeforeEach
  public void beforeEach() {
    mockBasketItemRepository = mock(BasketItemRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    target = new BasketItemDeleteService(mockBasketItemRepository, mockMemberFindService);
  }

  @Test
  @DisplayName("deleteBasketItemInController() : 정상흐름")
  public void deleteBasketItemInController_ok() {
    // given
    Long givenMemberId = 3L;
    List<Long> givenBasketItemList = new ArrayList<Long>(Arrays.asList(10L, 20L, 30L));

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    List<BasketItem> givenBasketItemLlist =
        new ArrayList<BasketItem>(
            Arrays.asList(
                BasketItemBuilder.fullData().build(),
                BasketItemBuilder.fullData().build(),
                BasketItemBuilder.fullData().build()));
    for (int i = 0; i < givenBasketItemLlist.size(); i++) {
      ReflectionTestUtils.setField(givenBasketItemLlist.get(i), "id", givenBasketItemList.get(i));
      ReflectionTestUtils.setField(givenBasketItemLlist.get(i).getMember(), "id", givenMemberId);
    }
    when(mockBasketItemRepository.findAllById(anyList())).thenReturn(givenBasketItemLlist);

    // when
    target.deleteBasketItemInController(givenMemberId, givenBasketItemList);

    // then
    ArgumentCaptor<List<BasketItem>> basketListCapture = ArgumentCaptor.forClass(List.class);
    verify(mockBasketItemRepository, times(1)).deleteAllInBatch(basketListCapture.capture());

    assertEquals(givenBasketItemList.size(), basketListCapture.getValue().size());
    List<Long> capturedBasketIdList =
        basketListCapture.getValue().stream().map(BasketItem::getId).toList();
    assertArrayEquals(givenBasketItemList.toArray(), capturedBasketIdList.toArray());
    List<Long> capturedMemberIdList =
        basketListCapture.getValue().stream().map(BasketItem::getId).toList();
    for (Long capturedMemberId : capturedMemberIdList) {
      givenMemberId.equals(capturedMemberId);
    }
  }

  @Test
  @DisplayName("deleteBasketItemInController() : 다른 회원의 장바구니 아이템을 삭제하려는 경우")
  public void ddeleteBasketItemInController_OtherMemberBasketItemDelete() {
    // given
    Long givenMemberId = 3L;
    Long wrongMemberId = 20L;
    List<Long> givenBasketItemList = new ArrayList<Long>(Arrays.asList(10L, 20L, 30L));

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    List<BasketItem> givenBasketItemLlist =
        new ArrayList<BasketItem>(
            Arrays.asList(
                BasketItemBuilder.fullData().build(),
                BasketItemBuilder.fullData().build(),
                BasketItemBuilder.fullData().build()));
    for (int i = 0; i < givenBasketItemLlist.size(); i++) {
      ReflectionTestUtils.setField(givenBasketItemLlist.get(i), "id", givenBasketItemList.get(i));
      ReflectionTestUtils.setField(givenBasketItemLlist.get(i).getMember(), "id", wrongMemberId);
    }
    when(mockBasketItemRepository.findAllById(anyList())).thenReturn(givenBasketItemLlist);

    // when then
    assertThrows(
        DataNotFound.class,
        () -> target.deleteBasketItemInController(givenMemberId, givenBasketItemList));
  }

  @Test
  @DisplayName("deleteBasketItemInController() : 존재하지 않는 장바구니 아이템을 삭제하려는 경우")
  public void deleteBasketItemInController_InvalidBasketItemId() {
    // given
    Long givenMemberId = 3L;
    List<Long> givenBasketItemList = new ArrayList<Long>(Arrays.asList(10L, 20L, 30L));

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemRepository.findAllById(anyList())).thenReturn(new ArrayList<>());

    // when then
    assertThrows(
        DataNotFound.class,
        () -> target.deleteBasketItemInController(givenMemberId, givenBasketItemList));
  }
}
