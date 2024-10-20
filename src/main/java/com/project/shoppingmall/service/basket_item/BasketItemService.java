package com.project.shoppingmall.service.basket_item;

import com.project.shoppingmall.dto.basket.*;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.*;
import com.project.shoppingmall.repository.BasketItemRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.type.ProductSaleType;
import com.project.shoppingmall.util.JsonUtil;
import com.project.shoppingmall.util.PriceCalculateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasketItemService {
  private final BasketItemRepository basketItemRepository;
  private final MemberFindService memberFindService;
  private final ProductFindService productFindService;

  @Transactional
  public BasketItem saveBasketItem(BasketItemMakeData basketItemMakeData) {
    Member member =
        memberFindService
            .findById(basketItemMakeData.getMemberId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 멤버가 존재하지 않습니다."));
    Product product =
        productFindService
            .findById(basketItemMakeData.getProductId())
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 제품이 존재하지 않습니다."));

    if (member.getIsBan())
      throw new CannotSaveBasketItemBecauseMemberBan("벤상태의 회원은 장바구니에 아이템을 추가할 수 없습니다.");
    if (product.getIsBan()) throw new AddBannedProductInBasket("벤처리된 제품을 장바구니에 넣으려고 시도하고 있습니다.");
    if (product.getSaleState().equals(ProductSaleType.DISCONTINUED))
      throw new AddDiscontinuedProductInBasket("판매중단된 제품을 장바구니에 넣으려고 시도하고 있습니다.");
    if (product.getSeller().getId().equals(member.getId()))
      throw new CannotSaveBasketItemByOwnProduct("자신이 등록한 제품을 장바구니에 넣을 수 없습니다.");
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

  public BasketItemPriceCalcResult calculateBasketItemPrice(BasketItem basketItem) {
    Product product = basketItem.getProduct();
    ProductOptionObjForBasket optionObj =
        JsonUtil.convertJsonToObject(basketItem.getOptions(), ProductOptionObjForBasket.class);
    int noOptionPrice =
        PriceCalculateUtil.calculatePrice(
            product.getPrice(), product.getDiscountAmount(), product.getDiscountRate());

    SingleOptionCalcResult singleOptionCalcResult =
        calcSingleOption(optionObj.getSingleOptionId(), product.getSingleOptions());
    MultiOptionsCalcResult multiOptionsCalcResult =
        multiOptionsCalcResult(optionObj.getMultipleOptionId(), product.getMultipleOptions());

    if (product.getIsBan() || product.getSaleState().equals(ProductSaleType.DISCONTINUED))
      return new BasketItemPriceCalcResult(noOptionPrice, false);

    if (!singleOptionCalcResult.isSingleOptionAvailable()
        || !multiOptionsCalcResult.isMultiOptionAvailable()) {
      return new BasketItemPriceCalcResult(noOptionPrice, false);
    } else {
      List<Integer> optionPriceList = new ArrayList<>();
      optionPriceList.addAll(multiOptionsCalcResult.getMultiOptionPrices());
      optionPriceList.add(singleOptionCalcResult.getSingleOptionPrice());
      int finalPrice = PriceCalculateUtil.addOptionPrice(noOptionPrice, optionPriceList);
      return new BasketItemPriceCalcResult(
          finalPrice,
          true,
          singleOptionCalcResult.getSingleOption(),
          multiOptionsCalcResult.getMultiOptions());
    }
  }

  public void validateMemberIsBasketItemOwner(Long memberId, List<BasketItem> basketItems) {
    basketItems.forEach(
        basketItem -> {
          if (!basketItem.getMember().getId().equals(memberId))
            throw new DataNotFound("장바구니 아이템들이 유효하지 않습니다");
        });
  }

  private SingleOptionCalcResult calcSingleOption(
      Long singleOptionId, List<ProductSingleOption> singleOptionsInProduct) {
    if (singleOptionId == null) {
      return new SingleOptionCalcResult(true, null);
    } else {
      Optional<ProductSingleOption> findSingleOption =
          singleOptionsInProduct.stream()
              .filter(option -> singleOptionId.equals(option.getId()))
              .findFirst();
      return findSingleOption
          .map(option -> new SingleOptionCalcResult(true, option))
          .orElseGet(() -> new SingleOptionCalcResult(false, null));
    }
  }

  private MultiOptionsCalcResult multiOptionsCalcResult(
      List<Long> multiOptionIds, List<ProductMultipleOption> multipleOptionsInProduct) {
    List<Integer> multiOptionPrices = new ArrayList<>();
    if (multiOptionIds.isEmpty()) {
      return new MultiOptionsCalcResult(true, new ArrayList<>());
    } else {
      List<ProductMultipleOption> multipleOptions =
          findMultipleOptionInBasketItem(multiOptionIds, multipleOptionsInProduct);
      if (multiOptionIds.size() == multipleOptions.size()) {
        return new MultiOptionsCalcResult(true, multipleOptions);
      } else {
        return new MultiOptionsCalcResult(false, new ArrayList<>());
      }
    }
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
}
