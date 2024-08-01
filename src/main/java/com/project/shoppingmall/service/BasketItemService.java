package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.basket.BasketItemMakeData;
import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.dto.basket.ProductOptionObjForBasket;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.BasketItemRepository;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasketItemService {
  private final BasketItemRepository basketItemRepository;
  private final MemberService memberService;
  private final ProductService productService;

  @Transactional
  public BasketItem saveBasketItem(BasketItemMakeData basketItemMakeData) {
    Member member =
        memberService
            .findById(basketItemMakeData.getMemberId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 멤버가 존재하지 않습니다."));
    Product product =
        productService
            .findById(basketItemMakeData.getProductId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 제품이 존재하지 않습니다."));

    validateSingleOption(basketItemMakeData.getSingleOptionId(), product);
    validateMultiOption(basketItemMakeData.getMultipleOptionId(), product);

    ProductOptionObjForBasket optionObj =
        new ProductOptionObjForBasket(
            basketItemMakeData.getSingleOptionId(), basketItemMakeData.getMultipleOptionId());
    String optionObjJson = JsonUtil.convertObjectToJson(optionObj);

    BasketItem basketItem =
        BasketItem.builder().member(member).product(product).options(optionObjJson).build();
    basketItemRepository.save(basketItem);
    return basketItem;
  }

  @Transactional
  public void deleteBasketItem(Long memberId, List<Long> basketItemIdList) {
    Member member =
        memberService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 멤버가 존재하지 않습니다."));
    List<BasketItem> findAllResult = basketItemRepository.findAllById(basketItemIdList);
    validateInvalidBasketIdInput(basketItemIdList, findAllResult);
    validateMemberIsBasketItemOwner(member.getId(), findAllResult);
    basketItemRepository.deleteAllInBatch(findAllResult);
  }

  public BasketItemPriceCalcResult calculateBasketItemPrice(BasketItem basketItem) {
    Product product = basketItem.getProduct();
    Integer noOptionPrice =
        PriceCalculateUtil.calculatePrice(
            product.getPrice(), product.getDiscountAmount(), product.getDiscountRate());

    ProductOptionObjForBasket optionObj =
        JsonUtil.convertJsonToObject(basketItem.getOptions(), ProductOptionObjForBasket.class);
    if (validateOptionIsNull(optionObj)) return new BasketItemPriceCalcResult(noOptionPrice, true);

    Optional<ProductSingleOption> singleOption =
        findSingleOptionInBasketItem(optionObj.getSingleOptionId(), product.getSingleOptions());
    if (singleOption.isEmpty()) return new BasketItemPriceCalcResult(noOptionPrice, false);

    List<ProductMultipleOption> multipleOptions =
        findMultipleOptionInBasketItem(
            optionObj.getMultipleOptionId(), product.getMultipleOptions());
    if (multipleOptions.isEmpty()) return new BasketItemPriceCalcResult(noOptionPrice, false);

    Integer finalPrice = calculateFinalPrice(noOptionPrice, singleOption.get(), multipleOptions);
    return new BasketItemPriceCalcResult(finalPrice, true, singleOption.get(), multipleOptions);
  }

  private boolean validateOptionIsNull(ProductOptionObjForBasket optionObj) {
    if (optionObj.getSingleOptionId() == null && optionObj.getMultipleOptionId().isEmpty())
      return true;
    else return false;
  }

  private Optional<ProductSingleOption> findSingleOptionInBasketItem(
      Long optionId, List<ProductSingleOption> optionList) {
    if (optionId == null) return Optional.empty();

    return optionList.stream().filter(option -> optionId.equals(option.getId())).findFirst();
  }

  private List<ProductMultipleOption> findMultipleOptionInBasketItem(
      List<Long> optionIdList, List<ProductMultipleOption> optionList) {
    List<ProductMultipleOption> multipleOptions = new ArrayList<>();
    for (Long optionId : optionIdList) {
      Optional<ProductMultipleOption> multiOption =
          optionList.stream().filter(option -> option.getId().equals(optionId)).findFirst();
      multiOption.ifPresent(multipleOptions::add);
    }
    return multipleOptions;
  }

  private Integer calculateFinalPrice(
      int originPrice,
      ProductSingleOption singleOption,
      List<ProductMultipleOption> multipleOptions) {
    List<Integer> optoinPriceList =
        multipleOptions.stream()
            .map(ProductMultipleOption::getPriceChangeAmount)
            .collect(Collectors.toList());
    optoinPriceList.add(singleOption.getPriceChangeAmount());
    return PriceCalculateUtil.addOptionPrice(originPrice, optoinPriceList);
  }

  private void validateSingleOption(Long optionId, Product product) {
    if (optionId == null) return;
    product.getSingleOptions().stream()
        .filter(option -> option.getId().equals(optionId))
        .findFirst()
        .orElseThrow(() -> new DataNotFound("제품의 단일옵션 ID 입력값이 유효하지 않습니다."));
  }

  private void validateMultiOption(List<Long> optionList, Product product) {
    if (optionList.isEmpty()) return;
    for (Long optionId : optionList) {
      product.getMultipleOptions().stream()
          .filter(option -> option.getId().equals(optionId))
          .findFirst()
          .orElseThrow(() -> new DataNotFound("제품의 다중옵션 ID 입력값이 유효하지 않습니다."));
    }
  }

  public void validateInvalidBasketIdInput(
      List<Long> inputBasketItemIdList, List<BasketItem> finaAllResult) {
    if (finaAllResult.size() != inputBasketItemIdList.size()) {
      throw new DataNotFound("ID에 해당하는 장바구니 아이템이 존재하지 않습니다.");
    }
  }

  private void validateMemberIsBasketItemOwner(Long memberId, List<BasketItem> basketItems) {
    int invalidBasketItemCount =
        basketItems.stream()
            .filter(basketItem -> !memberId.equals(basketItem.getMember().getId()))
            .toList()
            .size();
    if (invalidBasketItemCount != 0) throw new DataNotFound("장바구니 아이템들이 유효하지 않습니다");
  }
}
