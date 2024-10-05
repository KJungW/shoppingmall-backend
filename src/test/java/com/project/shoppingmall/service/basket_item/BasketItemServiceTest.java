package com.project.shoppingmall.service.basket_item;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.dto.basket.BasketItemMakeData;
import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.dto.basket.ProductOptionObjForBasket;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.*;
import com.project.shoppingmall.repository.BasketItemRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.test_entity.basketitem.BasketItemBuilder;
import com.project.shoppingmall.test_entity.basketitem.BasketItemMakeDataBuilder;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.product.ProductMultiOptionBuilder;
import com.project.shoppingmall.test_entity.product.ProductSingleOptionBuilder;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.ProductSaleType;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.io.IOException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class BasketItemServiceTest {
  private BasketItemService target;
  private BasketItemRepository mockBasketItemRepository;
  private MemberFindService mockMemberFindService;
  private ProductFindService mockProductFindService;

  @BeforeEach
  public void beforeEach() {
    mockBasketItemRepository = mock(BasketItemRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    mockProductFindService = mock(ProductFindService.class);
    target =
        new BasketItemService(
            mockBasketItemRepository, mockMemberFindService, mockProductFindService);
  }

  @Test
  @DisplayName("saveBasketItem() : 정상흐름")
  public void saveBasketItem_ok() {
    // given
    BasketItemMakeData inputMakeData = setBasketItemMakeData(20L, 40L, 10L, List.of(15L, 25L));

    Member givenMember = MemberBuilder.makeMember(inputMakeData.getMemberId());
    Member givenSeller = MemberBuilder.makeMember(123213L);
    Product givenProduct =
        ProductBuilder.makeProductWithProductIdList(
            inputMakeData.getProductId(),
            givenSeller,
            List.of(10L, 20L, 30L),
            List.of(15L, 25L, 35L));

    set_memberFindService_findById(givenMember);
    set_productFindService_findById(givenProduct);

    // when
    BasketItem resultBasketItem = target.saveBasketItem(inputMakeData);

    // then
    check_basketItem(inputMakeData, resultBasketItem);
  }

  @Test
  @DisplayName("saveBasketItem() : 벤처리된 회원이 장바구니 아이템을 생성하려고 시도")
  public void saveBasketItem_bannedMember() {
    // given
    BasketItemMakeData inputMakeData = setBasketItemMakeData(20L, 40L, 10L, List.of(15L, 25L));

    Member givenMember = MemberBuilder.makeMember(inputMakeData.getMemberId(), LoginType.NAVER);
    ReflectionTestUtils.setField(givenMember, "isBan", true);
    Product givenProduct = ProductBuilder.makeProduct(inputMakeData.getProductId());

    set_memberFindService_findById(givenMember);
    set_productFindService_findById(givenProduct);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockProductFindService.findById(any())).thenReturn(Optional.of(givenProduct));

    // when then
    assertThrows(
        CannotSaveBasketItemBecauseMemberBan.class, () -> target.saveBasketItem(inputMakeData));
  }

  @Test
  @DisplayName("saveBasketItem() : 벤처리된 제품을 장바구니에 넣으려고 시도")
  public void saveBasketItem_bannedProduct() {
    // given
    BasketItemMakeData inputMakeData = setBasketItemMakeData(20L, 40L, 10L, List.of(15L, 25L));

    Member givenMember = MemberBuilder.makeMember(inputMakeData.getMemberId(), LoginType.NAVER);
    Product givenProduct = ProductBuilder.makeProduct(inputMakeData.getProductId());
    ReflectionTestUtils.setField(givenProduct, "isBan", true);

    set_memberFindService_findById(givenMember);
    set_productFindService_findById(givenProduct);

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
    when(mockProductFindService.findById(any())).thenReturn(Optional.of(givenProduct));

    // when then
    assertThrows(AddBannedProductInBasket.class, () -> target.saveBasketItem(inputMakeData));
  }

  @Test
  @DisplayName("saveBasketItem() : 판매중단된 제품을 장바구니에 넣으려고 시도")
  public void saveBasketItem_discontinuedProduct() {
    // given
    BasketItemMakeData inputMakeData = setBasketItemMakeData(20L, 40L, 10L, List.of(15L, 25L));

    Member givenMember = MemberBuilder.makeMember(inputMakeData.getMemberId());
    Member givenSeller = MemberBuilder.makeMember(123213L);
    Product givenProduct =
        ProductBuilder.makeProductWithProductIdList(
            inputMakeData.getProductId(),
            givenSeller,
            List.of(10L, 20L, 30L),
            List.of(15L, 25L, 35L));
    ReflectionTestUtils.setField(givenProduct, "saleState", ProductSaleType.DISCONTINUED);

    set_memberFindService_findById(givenMember);
    set_productFindService_findById(givenProduct);

    // when then
    assertThrows(AddDiscontinuedProductInBasket.class, () -> target.saveBasketItem(inputMakeData));
  }

  @Test
  @DisplayName("saveBasketItem() : 제품에 대한 옵션 선택을 하지 않았을 경우")
  public void saveBasketItem_noOption() {
    // given
    BasketItemMakeData inputMakeData = setBasketItemMakeData(20L, 40L);

    Member givenMember = MemberBuilder.makeMember(inputMakeData.getMemberId());
    Member givenSeller = MemberBuilder.makeMember(123213L);
    Product givenProduct =
        ProductBuilder.makeProductWithProductIdList(
            inputMakeData.getProductId(),
            givenSeller,
            List.of(10L, 20L, 30L),
            List.of(15L, 25L, 35L));

    set_memberFindService_findById(givenMember);
    set_productFindService_findById(givenProduct);

    // when
    BasketItem resultBasketItem = target.saveBasketItem(inputMakeData);

    // then
    check_basketItem_noOption(inputMakeData, resultBasketItem);
  }

  @Test
  @DisplayName("saveBasketItem() : 제품에 대한 단일옵션이 유효하지 않은 경우")
  public void saveBasketItem_incorrectSingleOption() {
    // given
    BasketItemMakeData inputMakeData = setBasketItemMakeData(20L, 40L, 1000L, List.of(15L));

    Member givenMember = MemberBuilder.makeMember(inputMakeData.getMemberId());
    Member givenSeller = MemberBuilder.makeMember(123213L);
    Product givenProduct =
        ProductBuilder.makeProductWithProductIdList(
            inputMakeData.getProductId(),
            givenSeller,
            List.of(10L, 20L, 30L),
            List.of(15L, 25L, 35L));

    set_memberFindService_findById(givenMember);
    set_productFindService_findById(givenProduct);

    // when then
    assertThrows(DataNotFound.class, () -> target.saveBasketItem(inputMakeData));
  }

  @Test
  @DisplayName("saveBasketItem() : 제품에 대한 다중 옵션이 유효하지 않은 경우")
  public void saveBasketItem_incorrectMultiOption() {
    // given
    BasketItemMakeData inputMakeData = setBasketItemMakeData(20L, 40L, 10L, List.of(1000L));

    Member givenMember = MemberBuilder.makeMember(inputMakeData.getMemberId());
    Member givenSeller = MemberBuilder.makeMember(123213L);
    Product givenProduct =
        ProductBuilder.makeProductWithProductIdList(
            inputMakeData.getProductId(),
            givenSeller,
            List.of(10L, 20L, 30L),
            List.of(15L, 25L, 35L));

    set_memberFindService_findById(givenMember);
    set_productFindService_findById(givenProduct);

    // when then
    assertThrows(DataNotFound.class, () -> target.saveBasketItem(inputMakeData));
  }

  @Test
  @DisplayName("saveBasketItem() : 자기자신의 제품을 장바구니 아이템을 넣으려는 경우")
  public void saveBasketItem_byProductSeller() {
    // given
    BasketItemMakeData inputMakeData = setBasketItemMakeData(20L, 40L, 10L, List.of(1000L));

    Member givenMember = MemberBuilder.makeMember(inputMakeData.getMemberId());
    Product givenProduct =
        ProductBuilder.makeProductWithProductIdList(
            inputMakeData.getProductId(),
            givenMember,
            List.of(10L, 20L, 30L),
            List.of(15L, 25L, 35L));

    set_memberFindService_findById(givenMember);
    set_productFindService_findById(givenProduct);

    // when then
    assertThrows(
        CannotSaveBasketItemByOwnProduct.class, () -> target.saveBasketItem(inputMakeData));
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션과 다중옵션이 모두 유효할때")
  public void calculateBasketItemPrice_ok() {
    // given
    Member givenOwner = MemberBuilder.makeMember(30L, LoginType.NAVER);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(10L, 20L, 30L), 1000);
    List<ProductMultipleOption> givenMultipleOptions =
        ProductMultiOptionBuilder.makeList(List.of(15L, 25L, 35L), 1000);
    Product givenProduct =
        setProduct(10L, 20L, 5000, 1000, 20d, givenSingleOptions, givenMultipleOptions);
    BasketItem inputBasketItem =
        BasketItemBuilder.makeBasketItem(16L, givenOwner, givenProduct, 10L, List.of(15L, 25L));

    // when
    BasketItemPriceCalcResult calcResult = target.calculateBasketItemPrice(inputBasketItem);

    // then
    check_basketItemPriceCalcResult(inputBasketItem, calcResult);
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션과 다중옵션이 모두 비어있을때")
  public void calculateBasketItemPrice_BlankSingleAndMulti() throws IOException {
    // given
    Member givenOwner = MemberBuilder.makeMember(30L, LoginType.NAVER);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(10L, 20L, 30L), 1000);
    List<ProductMultipleOption> givenMultipleOptions =
        ProductMultiOptionBuilder.makeList(List.of(15L, 25L, 35L), 1000);
    Product givenProduct =
        setProduct(10L, 20L, 5000, 1000, 20d, givenSingleOptions, givenMultipleOptions);
    BasketItem inputBasketItem = BasketItemBuilder.makeBasketItem(16L, givenOwner, givenProduct);

    // when
    BasketItemPriceCalcResult calcResult = target.calculateBasketItemPrice(inputBasketItem);

    // then
    check_basketItemPriceCalcResult_noOption(inputBasketItem, calcResult);
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션은 비어있고, 다중옵션은 유효할때")
  public void calculateBasketItemPrice_blankSingle() {
    // given
    Member givenOwner = MemberBuilder.makeMember(30L, LoginType.NAVER);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(10L, 20L, 30L), 1000);
    List<ProductMultipleOption> givenMultipleOptions =
        ProductMultiOptionBuilder.makeList(List.of(15L, 25L, 35L), 1000);
    Product givenProduct =
        setProduct(10L, 20L, 5000, 1000, 20d, givenSingleOptions, givenMultipleOptions);
    BasketItem inputBasketItem =
        BasketItemBuilder.makeBasketItem(16L, givenOwner, givenProduct, List.of(15L, 25L));

    // when
    BasketItemPriceCalcResult calcResult = target.calculateBasketItemPrice(inputBasketItem);

    // then
    check_basketItemPriceCalcResult_noSingleOption(inputBasketItem, calcResult);
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션이 유효하고, 다중옵션이 비어있을때")
  public void calculateBasketItemPrice_blankMulti() {
    // given
    Member givenOwner = MemberBuilder.makeMember(30L, LoginType.NAVER);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(10L, 20L, 30L), 1000);
    List<ProductMultipleOption> givenMultipleOptions =
        ProductMultiOptionBuilder.makeList(List.of(15L, 25L, 35L), 1000);
    Product givenProduct =
        setProduct(10L, 20L, 5000, 1000, 20d, givenSingleOptions, givenMultipleOptions);
    BasketItem inputBasketItem =
        BasketItemBuilder.makeBasketItem(16L, givenOwner, givenProduct, 10L);

    // when
    BasketItemPriceCalcResult calcResult = target.calculateBasketItemPrice(inputBasketItem);

    // then
    check_basketItemPriceCalcResult_noMultiOptions(inputBasketItem, calcResult);
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 단일옵션이 유효하지 않을때")
  public void calculateBasketItemPrice_IncorrectSingle() {
    // given
    Member givenOwner = MemberBuilder.makeMember(30L, LoginType.NAVER);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(10L, 20L, 30L), 1000);
    List<ProductMultipleOption> givenMultipleOptions =
        ProductMultiOptionBuilder.makeList(List.of(15L, 25L, 35L), 1000);
    Product givenProduct =
        setProduct(10L, 20L, 5000, 1000, 20d, givenSingleOptions, givenMultipleOptions);
    BasketItem inputBasketItem =
        BasketItemBuilder.makeBasketItem(16L, givenOwner, givenProduct, 10000L, List.of(15L, 25L));

    // when
    BasketItemPriceCalcResult calcResult = target.calculateBasketItemPrice(inputBasketItem);

    // then
    check_basketItemPriceCalcResult_incorrectOption(inputBasketItem, calcResult);
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 다중옵션이 유효하지 않을때")
  public void calculateBasketItemPrice_IncorrectMulti() {
    // given
    Member givenOwner = MemberBuilder.makeMember(30L, LoginType.NAVER);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(10L, 20L, 30L), 1000);
    List<ProductMultipleOption> givenMultipleOptions =
        ProductMultiOptionBuilder.makeList(List.of(15L, 25L, 35L), 1000);
    Product givenProduct =
        setProduct(10L, 20L, 5000, 1000, 20d, givenSingleOptions, givenMultipleOptions);
    BasketItem inputBasketItem =
        BasketItemBuilder.makeBasketItem(16L, givenOwner, givenProduct, 10L, List.of(10000L, 25L));

    // when
    BasketItemPriceCalcResult calcResult = target.calculateBasketItemPrice(inputBasketItem);

    // then
    check_basketItemPriceCalcResult_incorrectOption(inputBasketItem, calcResult);
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 장바구니 아이템의 타겟 제품이 밴처리 되었을 경우")
  public void calculateBasketItemPrice_bannedProduct() {
    // given
    Member givenOwner = MemberBuilder.makeMember(30L, LoginType.NAVER);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(10L, 20L, 30L), 1000);
    List<ProductMultipleOption> givenMultipleOptions =
        ProductMultiOptionBuilder.makeList(List.of(15L, 25L, 35L), 1000);
    Product givenProduct =
        setProduct(10L, 20L, 5000, 1000, 20d, givenSingleOptions, givenMultipleOptions);
    ReflectionTestUtils.setField(givenProduct, "isBan", true);
    BasketItem inputBasketItem =
        BasketItemBuilder.makeBasketItem(16L, givenOwner, givenProduct, 10L, List.of(15L, 25L));

    // when
    BasketItemPriceCalcResult calcResult = target.calculateBasketItemPrice(inputBasketItem);

    // then
    check_basketItemPriceCalcResult_incorrectOption(inputBasketItem, calcResult);
  }

  @Test
  @DisplayName("calculateBasketItemPrice() : 장바구니 아이템의 타겟 제품이 판매중단 되었을 경우")
  public void calculateBasketItemPrice_discontinuedProduct() {
    // given
    Member givenOwner = MemberBuilder.makeMember(30L, LoginType.NAVER);
    List<ProductSingleOption> givenSingleOptions =
        ProductSingleOptionBuilder.makeList(List.of(10L, 20L, 30L), 1000);
    List<ProductMultipleOption> givenMultipleOptions =
        ProductMultiOptionBuilder.makeList(List.of(15L, 25L, 35L), 1000);
    Product givenProduct =
        setProduct(10L, 20L, 5000, 1000, 20d, givenSingleOptions, givenMultipleOptions);
    ReflectionTestUtils.setField(givenProduct, "saleState", ProductSaleType.DISCONTINUED);
    BasketItem inputBasketItem =
        BasketItemBuilder.makeBasketItem(16L, givenOwner, givenProduct, 10L, List.of(15L, 25L));

    // when
    BasketItemPriceCalcResult calcResult = target.calculateBasketItemPrice(inputBasketItem);

    // then
    check_basketItemPriceCalcResult_incorrectOption(inputBasketItem, calcResult);
  }

  private Product setProduct(
      long givenProductId,
      long givenProductSellerId,
      int givenProductPrice,
      int givenDiscountAmount,
      double givenDiscountRate,
      List<ProductSingleOption> givenSingleOption,
      List<ProductMultipleOption> givenMultiOption) {
    Member givenSeller = MemberBuilder.makeMember(givenProductSellerId, LoginType.NAVER);
    return ProductBuilder.makeProduct(
        givenProductId,
        givenSeller,
        givenProductPrice,
        givenDiscountAmount,
        givenDiscountRate,
        givenSingleOption,
        givenMultiOption);
  }

  private BasketItemMakeData setBasketItemMakeData(
      long givenOwnerId, long givenProductId, long singleOptionId, List<Long> multiOptionIds) {
    return BasketItemMakeDataBuilder.makeBasketItem(
        givenOwnerId, givenProductId, singleOptionId, multiOptionIds);
  }

  private BasketItemMakeData setBasketItemMakeData(long givenOwnerId, long givenProductId) {
    return BasketItemMakeDataBuilder.makeBasketItem(givenOwnerId, givenProductId);
  }

  private void check_basketItem(BasketItemMakeData givenMakeData, BasketItem targetBasketItem) {
    assertEquals(givenMakeData.getMemberId(), targetBasketItem.getMember().getId());
    assertEquals(givenMakeData.getProductId(), targetBasketItem.getProduct().getId());
    ProductOptionObjForBasket optionInResult =
        JsonUtil.convertJsonToObject(
            targetBasketItem.getOptions(), ProductOptionObjForBasket.class);
    assertEquals(givenMakeData.getSingleOptionId(), optionInResult.getSingleOptionId());
    assertArrayEquals(
        givenMakeData.getMultipleOptionId().toArray(),
        optionInResult.getMultipleOptionId().toArray());
  }

  private void check_basketItem_noOption(
      BasketItemMakeData givenMakeData, BasketItem targetBasketItem) {
    assertEquals(givenMakeData.getMemberId(), targetBasketItem.getMember().getId());
    assertEquals(givenMakeData.getProductId(), targetBasketItem.getProduct().getId());
    ProductOptionObjForBasket optionInResult =
        JsonUtil.convertJsonToObject(
            targetBasketItem.getOptions(), ProductOptionObjForBasket.class);
    assertNull(optionInResult.getSingleOptionId());
    assertTrue(optionInResult.getMultipleOptionId().isEmpty());
  }

  private void check_basketItemPriceCalcResult(
      BasketItem givenBasketItem, BasketItemPriceCalcResult calcResult) {
    ProductOptionObjForBasket givenOptionInBasketItem =
        JsonUtil.convertJsonToObject(givenBasketItem.getOptions(), ProductOptionObjForBasket.class);
    int expectedCalcPrice = calcBasketItemPrice(givenBasketItem);
    int realCalcPrice = calcResult.getPrice();
    assertEquals(expectedCalcPrice, realCalcPrice);
    assertTrue(calcResult.isOptionAvailable());
    assertEquals(
        givenOptionInBasketItem.getSingleOptionId(), calcResult.getSingleOption().getOptionId());
    List<Long> expectedMultiOptionIds = givenOptionInBasketItem.getMultipleOptionId();
    List<Long> realMultiOptionIds =
        calcResult.getMultipleOptions().stream().map(ProductOptionDto::getOptionId).toList();
    assertArrayEquals(expectedMultiOptionIds.toArray(), realMultiOptionIds.toArray());
  }

  private void check_basketItemPriceCalcResult_noOption(
      BasketItem givenBasketItem, BasketItemPriceCalcResult calcResult) {
    int expectedCalcPrice = calcBasketItemPrice(givenBasketItem);
    int realCalcPrice = calcResult.getPrice();
    assertEquals(expectedCalcPrice, realCalcPrice);
    assertTrue(calcResult.isOptionAvailable());
    assertNull(calcResult.getSingleOption());
    assertTrue(calcResult.getMultipleOptions().isEmpty());
  }

  private void check_basketItemPriceCalcResult_noSingleOption(
      BasketItem givenBasketItem, BasketItemPriceCalcResult calcResult) {
    ProductOptionObjForBasket givenOptionInBasketItem =
        JsonUtil.convertJsonToObject(givenBasketItem.getOptions(), ProductOptionObjForBasket.class);
    int expectedCalcPrice = calcBasketItemPrice(givenBasketItem);
    int realCalcPrice = calcResult.getPrice();
    assertEquals(expectedCalcPrice, realCalcPrice);
    assertTrue(calcResult.isOptionAvailable());
    assertNull(calcResult.getSingleOption());
    List<Long> expectedMultiOptionIds = givenOptionInBasketItem.getMultipleOptionId();
    List<Long> realMultiOptionIds =
        calcResult.getMultipleOptions().stream().map(ProductOptionDto::getOptionId).toList();
    assertArrayEquals(expectedMultiOptionIds.toArray(), realMultiOptionIds.toArray());
  }

  private void check_basketItemPriceCalcResult_noMultiOptions(
      BasketItem givenBasketItem, BasketItemPriceCalcResult calcResult) {
    ProductOptionObjForBasket givenOptionInBasketItem =
        JsonUtil.convertJsonToObject(givenBasketItem.getOptions(), ProductOptionObjForBasket.class);
    int expectedCalcPrice = calcBasketItemPrice(givenBasketItem);
    int realCalcPrice = calcResult.getPrice();
    assertEquals(expectedCalcPrice, realCalcPrice);
    assertTrue(calcResult.isOptionAvailable());
    assertEquals(
        givenOptionInBasketItem.getSingleOptionId(), calcResult.getSingleOption().getOptionId());
    assertTrue(calcResult.getMultipleOptions().isEmpty());
  }

  private void check_basketItemPriceCalcResult_incorrectOption(
      BasketItem givenBasketItem, BasketItemPriceCalcResult calcResult) {
    int expectedCalcPrice = calcBasketItemNoOptionPrice(givenBasketItem);
    int realCalcPrice = calcResult.getPrice();
    assertEquals(expectedCalcPrice, realCalcPrice);
    assertFalse(calcResult.isOptionAvailable());
    assertNull(calcResult.getSingleOption());
    assertTrue(calcResult.getMultipleOptions().isEmpty());
  }

  private int calcBasketItemNoOptionPrice(BasketItem givenBasketItem) {
    Product targetProduct = givenBasketItem.getProduct();
    return PriceCalculateUtil.calculatePrice(
        targetProduct.getPrice(),
        targetProduct.getDiscountAmount(),
        targetProduct.getDiscountRate());
  }

  private int calcBasketItemPrice(BasketItem givenBasketItem) {
    Product targetProduct = givenBasketItem.getProduct();
    ProductOptionObjForBasket optionInBasketItem =
        JsonUtil.convertJsonToObject(givenBasketItem.getOptions(), ProductOptionObjForBasket.class);

    int noOptionPrice = calcBasketItemNoOptionPrice(givenBasketItem);

    List<Integer> optionPriceList = new ArrayList<>();
    if (optionInBasketItem.getSingleOptionId() != null) {
      Integer singleOptionPrice =
          targetProduct.getSingleOptions().stream()
              .filter(
                  singleOption ->
                      singleOption.getId().equals(optionInBasketItem.getSingleOptionId()))
              .findFirst()
              .get()
              .getPriceChangeAmount();
      optionPriceList.add(singleOptionPrice);
    }
    if (optionInBasketItem.getMultipleOptionId() != null
        && !optionInBasketItem.getMultipleOptionId().isEmpty()) {
      List<Integer> multiOptionPriceList =
          optionInBasketItem.getMultipleOptionId().stream()
              .map(
                  optionId ->
                      targetProduct.getMultipleOptions().stream()
                          .filter(multipleOption -> multipleOption.getId().equals(optionId))
                          .findFirst()
                          .get()
                          .getPriceChangeAmount())
              .toList();
      optionPriceList.addAll(multiOptionPriceList);
    }

    return PriceCalculateUtil.addOptionPrice(noOptionPrice, optionPriceList);
  }

  private void set_productFindService_findById(Product givenProduct) {
    when(mockProductFindService.findById(any())).thenReturn(Optional.of(givenProduct));
  }

  private void set_memberFindService_findById(Member givenMember) {
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));
  }
}
