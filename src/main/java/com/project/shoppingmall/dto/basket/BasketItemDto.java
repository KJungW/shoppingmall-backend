package com.project.shoppingmall.dto.basket;

import com.project.shoppingmall.dto.product.ProductDto;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.entity.BasketItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasketItemDto {
  private Long basketItemId;
  private Long memberId;
  private ProductDto product;
  private ProductOptionDto singleOption;
  private List<ProductOptionDto> multiOptions;
  private Integer finalPrice;
  private Boolean isAvailable;

  public BasketItemDto(BasketItem basketItem, BasketItemPriceCalcResult calcResult) {
    this.basketItemId = basketItem.getId();
    this.memberId = basketItem.getMember().getId();
    this.product = new ProductDto(basketItem.getProduct());
    this.singleOption = calcResult.getSingleOption();
    this.multiOptions = calcResult.getMultipleOptions();
    this.finalPrice = calcResult.getPrice();
    this.isAvailable = calcResult.isOptionAvailable();
  }
}
