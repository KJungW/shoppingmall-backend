package com.project.shoppingmall.test_dto.purchase;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.basket.ProductOptionObjForBasket;
import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductMultipleOption;
import com.project.shoppingmall.entity.ProductSingleOption;
import com.project.shoppingmall.test_dto.product.ProductOptionDtoManager;
import com.project.shoppingmall.testutil.TestUtil;
import com.project.shoppingmall.util.JsonUtil;
import java.util.List;

public class ProductDataForPurchaseManager {
  public static void check(BasketItem basketItem, ProductDataForPurchase target) {
    Product product = basketItem.getProduct();
    assertEquals(product.getId(), target.getProductId());
    assertEquals(product.getSeller().getId(), target.getSellerId());
    assertEquals(product.getSeller().getNickName(), target.getSellerName());
    assertEquals(product.getName(), target.getProductName());
    assertEquals(product.getProductType().getTypeName(), target.getProductTypeName());
    checkOptionInProductData(basketItem, target);
    assertEquals(product.getPrice(), target.getPrice());
    assertEquals(product.getDiscountAmount(), target.getDiscountAmount());
    assertEquals(product.getDiscountRate(), target.getDiscountRate());
  }

  private static void checkOptionInProductData(
      BasketItem basketItem, ProductDataForPurchase target) {
    ProductOptionObjForBasket basketItemOptionObj =
        JsonUtil.convertJsonToObject(basketItem.getOptions(), ProductOptionObjForBasket.class);
    Product product = basketItem.getProduct();

    ProductSingleOption singleOptionInProduct =
        TestUtil.findSingleOptionInProduct(product, basketItemOptionObj.getSingleOptionId());
    List<ProductMultipleOption> multiOptionsInProduct =
        TestUtil.findMultiOptionsInProduct(product, basketItemOptionObj.getMultipleOptionId());

    ProductOptionDto expectedSingleOptionDto = new ProductOptionDto(singleOptionInProduct);
    List<ProductOptionDto> expectedMultiOptionDto =
        multiOptionsInProduct.stream().map(ProductOptionDto::new).toList();

    ProductOptionDtoManager.check(expectedSingleOptionDto, target.getSingleOption());
    ProductOptionDtoManager.check(expectedSingleOptionDto, target.getSingleOption());
  }
}
