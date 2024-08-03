package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.basket.BasketItemMakeData;
import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.dto.basket.ProductOptionObjForBasket;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.BasketItemRepository;
import com.project.shoppingmall.testdata.*;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class BasketItemServiceTest {
  private BasketItemService target;
  private BasketItemRepository mockBasketItemRepository;
  private MemberService mockMemberService;
  private ProductService mockProductService;

  @BeforeEach
  public void beforeEach() {
    mockBasketItemRepository = mock(BasketItemRepository.class);
    mockMemberService = mock(MemberService.class);
    mockProductService = mock(ProductService.class);
    target = new BasketItemService(mockBasketItemRepository, mockMemberService, mockProductService);
  }

  @Test
  @DisplayName("saveBasketItem() : 정상흐름")
  public void saveBasketItem_ok() throws IOException {
    // given
    Long givenMemberId = 62L;
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);

    ArrayList<ProductSingleOption> givenSingleOption =
        new ArrayList<>(
            Arrays.asList(
                ProductSingleOptionBuilder.fullData().build(),
                ProductSingleOptionBuilder.fullData().build()));
    ArrayList<ProductMultipleOption> givenMultiOption =
        new ArrayList<>(
            Arrays.asList(
                ProductMultiOptionBuilder.fullData().build(),
                ProductMultiOptionBuilder.fullData().build(),
                ProductMultiOptionBuilder.fullData().build()));
    Product givenProduct =
        ProductBuilder.fullData()
            .singleOptions(givenSingleOption)
            .multipleOptions(givenMultiOption)
            .build();
    Long givenProductId = 30L;
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    Long givenSingleOptionId = 3L;
    ReflectionTestUtils.setField(givenProduct.getSingleOptions().get(0), "id", givenSingleOptionId);
    ReflectionTestUtils.setField(givenProduct.getSingleOptions().get(1), "id", 4L);
    List<Long> givenMultiOptionId = new ArrayList<>(Arrays.asList(10L, 20L, 30L));
    ReflectionTestUtils.setField(
        givenProduct.getMultipleOptions().get(0), "id", givenMultiOptionId.get(0));
    ReflectionTestUtils.setField(
        givenProduct.getMultipleOptions().get(1), "id", givenMultiOptionId.get(1));
    ReflectionTestUtils.setField(
        givenProduct.getMultipleOptions().get(2), "id", givenMultiOptionId.get(2));

    BasketItemMakeData givenMakeData =
        BasketItemMakeDataBuilder.fullData()
            .memberId(givenMemberId)
            .productId(givenProductId)
            .singleOptionId(givenSingleOptionId)
            .multipleOptionId(givenMultiOptionId)
            .build();

    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockProductService.findById(any())).thenReturn(Optional.of(givenProduct));

    // when
    BasketItem result = target.saveBasketItem(givenMakeData);

    // then
    assertEquals(givenMakeData.getMemberId(), result.getMember().getId());
    assertEquals(givenMakeData.getProductId(), result.getProduct().getId());
    ProductOptionObjForBasket optionInResult =
        JsonUtil.convertJsonToObject(result.getOptions(), ProductOptionObjForBasket.class);
    assertEquals(givenMakeData.getSingleOptionId(), optionInResult.getSingleOptionId());
    assertArrayEquals(
        givenMakeData.getMultipleOptionId().toArray(),
        optionInResult.getMultipleOptionId().toArray());
  }

  @Test
  @DisplayName("saveBasketItem() : 제품에 대한 옵션 선택을 하지 않았을 경우")
  public void saveBasketItem_NoOption() throws IOException {
    // given
    Long givenMemberId = 62L;
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    Long givenProductId = 30L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);

    BasketItemMakeData givenMakeData =
        BasketItemMakeDataBuilder.fullData()
            .memberId(givenMemberId)
            .productId(givenProductId)
            .singleOptionId(null)
            .multipleOptionId(null)
            .build();

    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockProductService.findById(any())).thenReturn(Optional.of(givenProduct));

    // when
    BasketItem result = target.saveBasketItem(givenMakeData);

    // then
    assertEquals(givenMakeData.getMemberId(), result.getMember().getId());
    assertEquals(givenMakeData.getProductId(), result.getProduct().getId());
    ProductOptionObjForBasket optionInResult =
        JsonUtil.convertJsonToObject(result.getOptions(), ProductOptionObjForBasket.class);
    assertNull(optionInResult.getSingleOptionId());
    assertEquals(0, optionInResult.getMultipleOptionId().size());
  }

  @Test
  @DisplayName("saveBasketItem() : 제품에 대한 단일옵션이 유효하지 않은 경우")
  public void saveBasketItem_InvalidSingleOption() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    ArrayList<ProductSingleOption> givenSingleOption =
        new ArrayList<>(
            Arrays.asList(
                ProductSingleOptionBuilder.fullData().build(),
                ProductSingleOptionBuilder.fullData().build()));
    Product givenProduct = ProductBuilder.fullData().singleOptions(givenSingleOption).build();
    ReflectionTestUtils.setField(givenProduct.getSingleOptions().get(0), "id", 1L);
    ReflectionTestUtils.setField(givenProduct.getSingleOptions().get(1), "id", 2L);

    Long givenWrongSingleOptionId = 10L;
    BasketItemMakeData givenMakeData =
        BasketItemMakeDataBuilder.fullData()
            .memberId(1L)
            .productId(2L)
            .singleOptionId(givenWrongSingleOptionId)
            .multipleOptionId(new ArrayList<>(Arrays.asList(1L, 2L)))
            .build();

    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockProductService.findById(any())).thenReturn(Optional.of(givenProduct));

    assertThrows(DataNotFound.class, () -> target.saveBasketItem(givenMakeData));
  }

  @Test
  @DisplayName("saveBasketItem() : 제품에 대한 옵션이 유효하지 않은 경우")
  public void saveBasketItem_InvalidOption() throws IOException {
    Member givenMember = MemberBuilder.fullData().build();
    ArrayList<ProductMultipleOption> givenMultiOption =
        new ArrayList<>(
            Arrays.asList(
                ProductMultiOptionBuilder.fullData().build(),
                ProductMultiOptionBuilder.fullData().build(),
                ProductMultiOptionBuilder.fullData().build()));
    Product givenProduct = ProductBuilder.fullData().multipleOptions(givenMultiOption).build();
    ReflectionTestUtils.setField(givenProduct.getSingleOptions().get(0), "id", 1L);
    ReflectionTestUtils.setField(givenProduct.getMultipleOptions().get(0), "id", 1L);
    ReflectionTestUtils.setField(givenProduct.getMultipleOptions().get(1), "id", 2L);
    ReflectionTestUtils.setField(givenProduct.getMultipleOptions().get(2), "id", 3L);

    List<Long> givenWrongMultiOptionId = new ArrayList<>(Arrays.asList(10L, 20L));
    BasketItemMakeData givenMakeData =
        BasketItemMakeDataBuilder.fullData()
            .memberId(1L)
            .productId(2L)
            .singleOptionId(1L)
            .multipleOptionId(givenWrongMultiOptionId)
            .build();

    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockProductService.findById(any())).thenReturn(Optional.of(givenProduct));

    assertThrows(DataNotFound.class, () -> target.saveBasketItem(givenMakeData));
  }

  @Test
  @DisplayName("deleteBasketItem() : 정상흐름")
  public void deleteBasketItem_ok() throws IOException {
    // given
    Long givenMemberId = 3L;
    List<Long> givenBasketItemList = new ArrayList<Long>(Arrays.asList(10L, 20L, 30L));

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

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
    target.deleteBasketItem(givenMemberId, givenBasketItemList);

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
  @DisplayName("deleteBasketItem() : 다른 회원의 장바구니 아이템을 삭제하려는 경우")
  public void deleteBasketItem_OtherMemberBasketItemDelete() throws IOException {
    // given
    Long givenMemberId = 3L;
    Long wrongMemberId = 20L;
    List<Long> givenBasketItemList = new ArrayList<Long>(Arrays.asList(10L, 20L, 30L));

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

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
        DataNotFound.class, () -> target.deleteBasketItem(givenMemberId, givenBasketItemList));
  }

  @Test
  @DisplayName("deleteBasketItem() : 존재하지 않는 장바구니 아이템을 삭제하려는 경우")
  public void deleteBasketItem_InvalidBasketItemId() throws IOException {
    // given
    Long givenMemberId = 3L;
    List<Long> givenBasketItemList = new ArrayList<Long>(Arrays.asList(10L, 20L, 30L));

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockBasketItemRepository.findAllById(anyList())).thenReturn(new ArrayList<>());

    // when then
    assertThrows(
        DataNotFound.class, () -> target.deleteBasketItem(givenMemberId, givenBasketItemList));
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션과 다중옵션이 모두 유효할때")
  public void calculateBasketItemPrice_IncorrectSingleAndMulti() throws IOException {
    // given
    // - basketItem인자.product.singleOptions / multipleOptions 세팅
    List<Long> givenOptionIdList = Arrays.asList(10L, 20L, 30L);
    int givenOptionPrice = 1000;
    ArrayList<ProductSingleOption> givenSingleOptions =
        makeProductSingleMockData(givenOptionIdList, givenOptionPrice);
    ArrayList<ProductMultipleOption> givenMultipleOptions =
        makeProductMultiMockData(givenOptionIdList, givenOptionPrice);

    // - basketItem인자.product 세팅
    int givenProductPrice = 10000;
    int givenDiscountAmount = 1000;
    double givenDiscountRate = 20d;
    Product givenProduct =
        ProductBuilder.fullData()
            .price(givenProductPrice)
            .singleOptions(givenSingleOptions)
            .multipleOptions(givenMultipleOptions)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .build();

    // - basketItem인자 세팅
    Long rightSingleOptionId = 10L;
    List<Long> rightMultiOptionIdList = new ArrayList<>(Arrays.asList(10L, 20L));
    String rightOptionJson =
        JsonUtil.convertObjectToJson(
            new ProductOptionObjForBasket(rightSingleOptionId, rightMultiOptionIdList));
    BasketItem givenBasketItem =
        BasketItemBuilder.fullData().product(givenProduct).options(rightOptionJson).build();

    // when
    BasketItemPriceCalcResult result = target.calculateBasketItemPrice(givenBasketItem);

    // then
    int expectedPrice =
        PriceCalculateUtil.calculatePrice(givenProductPrice, givenDiscountAmount, givenDiscountRate)
            + givenOptionPrice
            + (rightMultiOptionIdList.size() * givenOptionPrice);
    assertEquals(expectedPrice, result.getPrice());
    assertTrue(result.isOptionAvailable());
    assertEquals(rightSingleOptionId, result.getSingleOption().getOptionId());
    List<Long> resultMultiOptionIdList =
        result.getMultipleOptions().stream().map(ProductOptionDto::getOptionId).toList();
    assertArrayEquals(rightMultiOptionIdList.toArray(), resultMultiOptionIdList.toArray());
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션과 다중옵션이 모두 비어있을때")
  public void calculateBasketItemPrice_BlankSingleAndMulti() throws IOException {
    // given
    // - basketItem인자.product.singleOptions / multipleOptions 세팅
    ArrayList<ProductSingleOption> givenSingleOptions =
        makeProductSingleMockData(Arrays.asList(10L, 20L, 30L), 1000);
    ArrayList<ProductMultipleOption> givenMultipleOptions =
        makeProductMultiMockData(Arrays.asList(10L, 20L, 30L), 1000);

    // - basketItem인자.product 세팅
    int givenProductPrice = 10000;
    int givenDiscountAmount = 1000;
    double givenDiscountRate = 20d;
    Product givenProduct =
        ProductBuilder.fullData()
            .price(givenProductPrice)
            .singleOptions(givenSingleOptions)
            .multipleOptions(givenMultipleOptions)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .build();

    // - basketItem인자 세팅
    String blankOptionJson = JsonUtil.convertObjectToJson(new ProductOptionObjForBasket());
    BasketItem givenBasketItem =
        BasketItemBuilder.fullData().product(givenProduct).options(blankOptionJson).build();

    // when
    BasketItemPriceCalcResult result = target.calculateBasketItemPrice(givenBasketItem);

    // then
    int expectedPrice =
        PriceCalculateUtil.calculatePrice(
            givenProductPrice, givenDiscountAmount, givenDiscountRate);
    assertEquals(expectedPrice, result.getPrice());
    assertTrue(result.isOptionAvailable());
    assertNull(result.getSingleOption());
    assertEquals(0, result.getMultipleOptions().size());
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션은 비어있고, 다중옵션은 유효할때")
  public void calculateBasketItemPrice_blankSingle() throws IOException {
    // given
    // - basketItem인자.product.singleOptions / multipleOptions 세팅
    List<Long> givenOptionIdList = Arrays.asList(10L, 20L, 30L);
    int givenOptionPrice = 1000;
    ArrayList<ProductSingleOption> givenSingleOptions =
        makeProductSingleMockData(givenOptionIdList, givenOptionPrice);
    ArrayList<ProductMultipleOption> givenMultipleOptions =
        makeProductMultiMockData(givenOptionIdList, givenOptionPrice);

    // - basketItem인자.product 세팅
    int givenProductPrice = 10000;
    int givenDiscountAmount = 1000;
    double givenDiscountRate = 20d;
    Product givenProduct =
        ProductBuilder.fullData()
            .price(givenProductPrice)
            .singleOptions(givenSingleOptions)
            .multipleOptions(givenMultipleOptions)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .build();

    // - basketItem인자 세팅
    List<Long> rightMultiOptionIdList = new ArrayList<>(Arrays.asList(10L, 20L));
    String rightOptionJson =
        JsonUtil.convertObjectToJson(new ProductOptionObjForBasket(null, rightMultiOptionIdList));
    BasketItem givenBasketItem =
        BasketItemBuilder.fullData().product(givenProduct).options(rightOptionJson).build();

    // when
    BasketItemPriceCalcResult result = target.calculateBasketItemPrice(givenBasketItem);

    // then
    int expectedPrice =
        PriceCalculateUtil.calculatePrice(givenProductPrice, givenDiscountAmount, givenDiscountRate)
            + (rightMultiOptionIdList.size() * givenOptionPrice);
    assertEquals(expectedPrice, result.getPrice());
    assertTrue(result.isOptionAvailable());
    assertNull(result.getSingleOption());
    List<Long> resultMultiOptionIdList =
        result.getMultipleOptions().stream().map(ProductOptionDto::getOptionId).toList();
    assertArrayEquals(rightMultiOptionIdList.toArray(), resultMultiOptionIdList.toArray());
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션이 유효하고, 다중옵션이 비어있을때")
  public void calculateBasketItemPrice_blankMulti() throws IOException {
    // given
    // - basketItem인자.product.singleOptions / multipleOptions 세팅
    List<Long> givenOptionIdList = Arrays.asList(10L, 20L, 30L);
    int givenOptionPrice = 1000;
    ArrayList<ProductSingleOption> givenSingleOptions =
        makeProductSingleMockData(givenOptionIdList, givenOptionPrice);
    ArrayList<ProductMultipleOption> givenMultipleOptions =
        makeProductMultiMockData(givenOptionIdList, givenOptionPrice);

    // - basketItem인자.product 세팅
    int givenProductPrice = 10000;
    int givenDiscountAmount = 1000;
    double givenDiscountRate = 20d;
    Product givenProduct =
        ProductBuilder.fullData()
            .price(givenProductPrice)
            .singleOptions(givenSingleOptions)
            .multipleOptions(givenMultipleOptions)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .build();

    // - basketItem인자 세팅
    Long rightSingleOptionId = 10L;
    List<Long> blankMultiOptionIdList = new ArrayList<>();
    String rightOptionJson =
        JsonUtil.convertObjectToJson(
            new ProductOptionObjForBasket(rightSingleOptionId, blankMultiOptionIdList));
    BasketItem givenBasketItem =
        BasketItemBuilder.fullData().product(givenProduct).options(rightOptionJson).build();

    // when
    BasketItemPriceCalcResult result = target.calculateBasketItemPrice(givenBasketItem);

    // then
    int expectedPrice =
        PriceCalculateUtil.calculatePrice(givenProductPrice, givenDiscountAmount, givenDiscountRate)
            + givenOptionPrice;
    assertEquals(expectedPrice, result.getPrice());
    assertTrue(result.isOptionAvailable());
    assertEquals(rightSingleOptionId, result.getSingleOption().getOptionId());
    assertEquals(0, result.getMultipleOptions().size());
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션이 유효하지 않을때")
  public void calculateBasketItemPrice_IncorrectSingle() throws IOException {
    // given
    // - basketItem인자.product.singleOptions / multipleOptions 세팅
    List<Long> givenOptionIdList = Arrays.asList(10L, 20L, 30L);
    int givenOptionPrice = 1000;
    ArrayList<ProductSingleOption> givenSingleOptions =
        makeProductSingleMockData(givenOptionIdList, givenOptionPrice);
    ArrayList<ProductMultipleOption> givenMultipleOptions =
        makeProductMultiMockData(givenOptionIdList, givenOptionPrice);

    // - basketItem인자.product 세팅
    int givenProductPrice = 10000;
    int givenDiscountAmount = 1000;
    double givenDiscountRate = 20d;
    Product givenProduct =
        ProductBuilder.fullData()
            .price(givenProductPrice)
            .singleOptions(givenSingleOptions)
            .multipleOptions(givenMultipleOptions)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .build();

    // - basketItem인자 세팅
    Long wrongSingleOptionId = 100L;
    List<Long> rightMultiOptionIdList = new ArrayList<>(Arrays.asList(10L, 20L));
    String rightOptionJson =
        JsonUtil.convertObjectToJson(
            new ProductOptionObjForBasket(wrongSingleOptionId, rightMultiOptionIdList));
    BasketItem givenBasketItem =
        BasketItemBuilder.fullData().product(givenProduct).options(rightOptionJson).build();

    // when
    BasketItemPriceCalcResult result = target.calculateBasketItemPrice(givenBasketItem);

    // then
    int expectedPrice =
        PriceCalculateUtil.calculatePrice(
            givenProductPrice, givenDiscountAmount, givenDiscountRate);
    assertEquals(expectedPrice, result.getPrice());
    assertFalse(result.isOptionAvailable());
    assertNull(result.getSingleOption());
    List<Long> resultMultiOptionIdList =
        result.getMultipleOptions().stream().map(ProductOptionDto::getOptionId).toList();
    assertEquals(0, result.getMultipleOptions().size());
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 다중옵션이 유효하지 않을때")
  public void calculateBasketItemPrice_ok() throws IOException {
    // given
    // - basketItem인자.product.singleOptions / multipleOptions 세팅
    List<Long> givenOptionIdList = Arrays.asList(10L, 20L, 30L);
    int givenOptionPrice = 1000;
    ArrayList<ProductSingleOption> givenSingleOptions =
        makeProductSingleMockData(givenOptionIdList, givenOptionPrice);
    ArrayList<ProductMultipleOption> givenMultipleOptions =
        makeProductMultiMockData(givenOptionIdList, givenOptionPrice);

    // - basketItem인자.product 세팅
    int givenProductPrice = 10000;
    int givenDiscountAmount = 1000;
    double givenDiscountRate = 20d;
    Product givenProduct =
        ProductBuilder.fullData()
            .price(givenProductPrice)
            .singleOptions(givenSingleOptions)
            .multipleOptions(givenMultipleOptions)
            .discountAmount(givenDiscountAmount)
            .discountRate(givenDiscountRate)
            .build();

    // - basketItem인자 세팅
    Long rightSingleOptionId = 10L;
    List<Long> wrongMultiOptionIdList = new ArrayList<>(Arrays.asList(10L, 100L));
    String rightOptionJson =
        JsonUtil.convertObjectToJson(
            new ProductOptionObjForBasket(rightSingleOptionId, wrongMultiOptionIdList));
    BasketItem givenBasketItem =
        BasketItemBuilder.fullData().product(givenProduct).options(rightOptionJson).build();

    // when
    BasketItemPriceCalcResult result = target.calculateBasketItemPrice(givenBasketItem);

    // then
    int expectedPrice =
        PriceCalculateUtil.calculatePrice(
            givenProductPrice, givenDiscountAmount, givenDiscountRate);
    assertEquals(expectedPrice, result.getPrice());
    assertFalse(result.isOptionAvailable());
    assertNull(result.getSingleOption());
    assertEquals(0, result.getMultipleOptions().size());
  }

  public ArrayList<ProductSingleOption> makeProductSingleMockData(
      List<Long> idList, int optionPrice) {
    ArrayList<ProductSingleOption> givenSingleOptions = new ArrayList<>();
    for (Long id : idList) {
      ProductSingleOption makedOption =
          ProductSingleOptionBuilder.fullData().priceChangeAmount(optionPrice).build();
      ReflectionTestUtils.setField(makedOption, "id", id);
      givenSingleOptions.add(makedOption);
    }
    return givenSingleOptions;
  }

  public ArrayList<ProductMultipleOption> makeProductMultiMockData(
      List<Long> idList, int optionPrice) {
    ArrayList<ProductMultipleOption> givenMultiOptions = new ArrayList<>();
    for (Long id : idList) {
      ProductMultipleOption makedOption =
          ProductMultiOptionBuilder.fullData().priceChangeAmount(optionPrice).build();
      ReflectionTestUtils.setField(makedOption, "id", id);
      givenMultiOptions.add(makedOption);
    }
    return givenMultiOptions;
  }
}
