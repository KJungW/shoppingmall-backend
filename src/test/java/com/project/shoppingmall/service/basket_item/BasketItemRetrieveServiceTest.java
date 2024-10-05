package com.project.shoppingmall.service.basket_item;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.basket.BasketDto;
import com.project.shoppingmall.dto.basket.BasketItemDto;
import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.BasketItemRetrieveRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.test_dto.basket_item.BasketDtoManager;
import com.project.shoppingmall.test_dto.basket_item.BasketItemDtoManager;
import com.project.shoppingmall.test_dto.basket_item.BasketItemPriceCalcResultManager;
import com.project.shoppingmall.test_entity.basketitem.BasketItemBuilder;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.product.ProductMultiOptionBuilder;
import com.project.shoppingmall.test_entity.product.ProductSingleOptionBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    long inputMemberId = 10L;
    long inputBasketItemId = 20L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct =
        setProductWithOption(40L, List.of(10L, 20L, 30L), List.of(10L, 20L, 30L));
    BasketItem givenBasketItem =
        BasketItemBuilder.makeBasketItem(
            inputBasketItemId, givenMember, givenProduct, 30L, List.of(10L, 20L));
    BasketItemPriceCalcResult givenPriceCalcResult =
        BasketItemPriceCalcResultManager.make(true, givenBasketItem, givenProduct);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemRetrieveRepository.retrieveBasketItemDetail(anyLong()))
        .thenReturn(Optional.of(givenBasketItem));
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(givenPriceCalcResult);

    // when
    BasketItemDto result = target.getBasketItemDetail(inputMemberId, inputBasketItemId);

    // then
    BasketItemDtoManager.check(givenBasketItem, givenPriceCalcResult, result);
  }

  @Test
  @DisplayName("getBasketItemDetail() : 다른 회원의 장바구니 아이템 조회 시도")
  public void getBasketItemDetail_OtherMemberBasketItem() {
    // given
    long inputMemberId = 10L;
    long inputBasketItemId = 20L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Member givenOtherMember = MemberBuilder.makeMember(50L);
    Product givenProduct =
        setProductWithOption(40L, List.of(10L, 20L, 30L), List.of(10L, 20L, 30L));
    BasketItem givenBasketItem =
        BasketItemBuilder.makeBasketItem(
            inputBasketItemId, givenOtherMember, givenProduct, 30L, List.of(10L, 20L));
    BasketItemPriceCalcResult givenPriceCalcResult =
        BasketItemPriceCalcResultManager.make(true, givenBasketItem, givenProduct);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemRetrieveRepository.retrieveBasketItemDetail(anyLong()))
        .thenReturn(Optional.of(givenBasketItem));
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(givenPriceCalcResult);

    // when
    assertThrows(
        DataNotFound.class, () -> target.getBasketItemDetail(inputMemberId, inputBasketItemId));
  }

  @Test
  @DisplayName("getBasket() : 정상흐름")
  public void getBasket_ok() {
    // given
    // - 인자 세팅
    Long inputMemberId = 10L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct =
        setProductWithOption(40L, List.of(10L, 20L, 30L), List.of(10L, 20L, 30L));
    List<BasketItem> givenBasketItems =
        setBasketItemList(List.of(1L, 2L, 3L), givenMember, givenProduct);
    BasketItemPriceCalcResult givenPriceCalcResult =
        BasketItemPriceCalcResultManager.make(true, givenBasketItems.get(0), givenProduct);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemRetrieveRepository.retrieveBasketByMemberId(anyLong()))
        .thenReturn(givenBasketItems);
    when(mockBasketItemService.calculateBasketItemPrice(any())).thenReturn(givenPriceCalcResult);

    // when
    BasketDto result = target.getBasket(inputMemberId);

    // then;
    BasketDtoManager.check(givenBasketItems, givenPriceCalcResult, result);
  }

  @Test
  @DisplayName("getBasket() : 장바구니에 아이템이 없을 경우")
  public void getBasket_NoItem() {
    // given
    Long inputMemberId = 10L;

    Member givenMember = MemberBuilder.makeMember(inputMemberId);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemRetrieveRepository.retrieveBasketByMemberId(anyLong()))
        .thenReturn(new ArrayList<>());

    // when
    target.getBasket(inputMemberId);

    // then
    verify(mockBasketItemService, times(0)).calculateBasketItemPrice(any());
  }

  public Product setProductWithOption(
      long productId, List<Long> singleOptionIdList, List<Long> multiOptionIdList) {
    List<ProductSingleOption> singleOptions =
        singleOptionIdList.stream().map(ProductSingleOptionBuilder::make).toList();
    List<ProductMultipleOption> multiOptions =
        multiOptionIdList.stream().map(ProductMultiOptionBuilder::make).toList();
    return ProductBuilder.makeProduct(productId, singleOptions, multiOptions);
  }

  public List<BasketItem> setBasketItemList(List<Long> idList, Member owner, Product product) {
    long selectedSingleOptionId = product.getSingleOptions().get(0).getId();
    long selectedMultiOptionId = product.getMultipleOptions().get(0).getId();
    return idList.stream()
        .map(
            id ->
                BasketItemBuilder.makeBasketItem(
                    id, owner, product, selectedSingleOptionId, List.of(selectedMultiOptionId)))
        .toList();
  }
}
