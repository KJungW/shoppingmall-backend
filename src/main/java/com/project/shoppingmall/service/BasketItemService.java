package com.project.shoppingmall.service;

import com.project.shoppingmall.dto.basket.BasketItemMakeData;
import com.project.shoppingmall.dto.basket.ProductOptionObjForBasket;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.BasketItemRepository;
import com.project.shoppingmall.util.JsonUtil;
import java.util.List;
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
