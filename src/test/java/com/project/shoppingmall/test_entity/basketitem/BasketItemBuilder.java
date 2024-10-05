package com.project.shoppingmall.test_entity.basketitem;

import com.project.shoppingmall.dto.basket.ProductOptionObjForBasket;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.util.JsonUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class BasketItemBuilder {

  public static BasketItem.BasketItemBuilder fullData() {
    return BasketItem.builder()
        .member(MemberBuilder.fullData().build())
        .product(ProductBuilder.fullData().build())
        .options("testOptionsJson");
  }

  public static BasketItem makeBasketItem(
      long id, Member owner, Product product, long singleOptionId, List<Long> multiOptionIdList) {
    String rightOptionJson =
        JsonUtil.convertObjectToJson(
            new ProductOptionObjForBasket(singleOptionId, multiOptionIdList));
    BasketItem basketItem =
        BasketItem.builder().member(owner).product(product).options(rightOptionJson).build();
    ReflectionTestUtils.setField(basketItem, "id", id);
    return basketItem;
  }

  public static BasketItem makeBasketItem(
      long id, Member owner, Product product, List<Long> multiOptionIdList) {
    String rightOptionJson =
        JsonUtil.convertObjectToJson(new ProductOptionObjForBasket(null, multiOptionIdList));
    BasketItem basketItem =
        BasketItem.builder().member(owner).product(product).options(rightOptionJson).build();
    ReflectionTestUtils.setField(basketItem, "id", id);
    return basketItem;
  }

  public static BasketItem makeBasketItem(
      long id, Member owner, Product product, long singleOptionId) {
    String rightOptionJson =
        JsonUtil.convertObjectToJson(new ProductOptionObjForBasket(singleOptionId, List.of()));
    BasketItem basketItem =
        BasketItem.builder().member(owner).product(product).options(rightOptionJson).build();
    ReflectionTestUtils.setField(basketItem, "id", id);
    return basketItem;
  }

  public static BasketItem makeBasketItem(long id, Member owner, Product product) {
    String rightOptionJson =
        JsonUtil.convertObjectToJson(new ProductOptionObjForBasket(null, new ArrayList<>()));
    BasketItem basketItem =
        BasketItem.builder().member(owner).product(product).options(rightOptionJson).build();
    ReflectionTestUtils.setField(basketItem, "id", id);
    return basketItem;
  }

  public static BasketItem makeBasketItem(long id, Member owner) {
    String optionJson =
        JsonUtil.convertObjectToJson(new ProductOptionObjForBasket(null, new ArrayList<>()));
    Product givenProduct = ProductBuilder.makeProduct(60432L);
    BasketItem basketItem =
        BasketItem.builder().member(owner).product(givenProduct).options(optionJson).build();
    ReflectionTestUtils.setField(basketItem, "id", id);
    return basketItem;
  }

  public static List<BasketItem> makeBasketItemList(
      List<Long> idList, Member owner, Product product) {
    return idList.stream().map(id -> makeBasketItem(id, owner, product)).toList();
  }
}
