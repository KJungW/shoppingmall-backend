package com.project.shoppingmall.service.basket_item;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.BasketItemRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.test_entity.basketitem.BasketItemBuilder;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
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
    long inputMemberId = 3L;
    List<Long> inputBasketItemList = new ArrayList<Long>(Arrays.asList(10L, 20L, 30L));

    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(30L);
    List<BasketItem> givenBasketItemLlist =
        inputBasketItemList.stream()
            .map(id -> BasketItemBuilder.makeBasketItem(id, givenMember, givenProduct))
            .toList();

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemRepository.findAllById(anyList())).thenReturn(givenBasketItemLlist);

    // when
    target.deleteBasketItemInController(inputMemberId, inputBasketItemList);

    // then
    check_basketItemRepository_deleteAllInBatch(inputMemberId, givenBasketItemLlist);
  }

  @Test
  @DisplayName("deleteBasketItemInController() : 다른 회원의 장바구니 아이템을 삭제하려는 경우")
  public void ddeleteBasketItemInController_OtherMemberBasketItemDelete() {
    // given
    long inputMemberId = 3L;
    List<Long> inputBasketItemList = new ArrayList<Long>(Arrays.asList(10L, 20L, 30L));

    long wrongMemberId = 20L;
    Member givenOtherMember = MemberBuilder.makeMember(wrongMemberId);
    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(30L);
    List<BasketItem> givenBasketItemLlist =
        inputBasketItemList.stream()
            .map(id -> BasketItemBuilder.makeBasketItem(id, givenOtherMember, givenProduct))
            .toList();

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemRepository.findAllById(anyList())).thenReturn(givenBasketItemLlist);

    // when then
    assertThrows(
        DataNotFound.class,
        () -> target.deleteBasketItemInController(inputMemberId, inputBasketItemList));
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

  public void check_basketItemRepository_deleteAllInBatch(
      long givenMemberId, List<BasketItem> givenBasketItemLlist) {
    ArgumentCaptor<List<BasketItem>> basketListCapture = ArgumentCaptor.forClass(List.class);
    verify(mockBasketItemRepository, times(1)).deleteAllInBatch(basketListCapture.capture());

    assertEquals(givenBasketItemLlist.size(), basketListCapture.getValue().size());
    List<BasketItem> capturedBaksetItemList = basketListCapture.getValue();
    for (int i = 0; i < capturedBaksetItemList.size(); i++) {
      assertEquals(givenMemberId, capturedBaksetItemList.get(i).getMember().getId());
      assertEquals(givenBasketItemLlist.get(i).getId(), capturedBaksetItemList.get(i).getId());
    }
  }
}
