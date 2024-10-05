package com.project.shoppingmall.test_dto.basket_item;

import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.dto.basket.ProductOptionObjForBasket;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductMultipleOption;
import com.project.shoppingmall.entity.ProductSingleOption;
import com.project.shoppingmall.testutil.TestUtil;
import com.project.shoppingmall.util.JsonUtil;
import java.util.List;

public class BasketItemPriceCalcResultManager {
  public static BasketItemPriceCalcResult make(int price, boolean isAvailable) {
    return new BasketItemPriceCalcResult(price, isAvailable);
  }

  public static BasketItemPriceCalcResult make(
      boolean isAvailable, BasketItem basketItem, Product product) {
    ProductOptionObjForBasket optionObj =
        JsonUtil.convertJsonToObject(basketItem.getOptions(), ProductOptionObjForBasket.class);

    ProductSingleOption singleOption =
        TestUtil.findSingleOptionInProduct(product, optionObj.getSingleOptionId());
    List<ProductMultipleOption> multiOptions =
        TestUtil.findMultiOptionsInProduct(product, optionObj.getMultipleOptionId());

    ProductOptionDto singleOptionDto = new ProductOptionDto(singleOption);
    List<ProductOptionDto> multiOptionDtoList =
        multiOptions.stream().map(ProductOptionDto::new).toList();

    int finalPrice = TestUtil.calcProductPrice(product, singleOptionDto, multiOptionDtoList);
    return new BasketItemPriceCalcResult(
        finalPrice, isAvailable, singleOptionDto, multiOptionDtoList);
  }
}
