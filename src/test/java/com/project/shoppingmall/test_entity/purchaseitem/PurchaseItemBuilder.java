package com.project.shoppingmall.test_entity.purchaseitem;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.test_entity.product.ProductDataForPurchaseBuilder;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class PurchaseItemBuilder {
  public static PurchaseItem.PurchaseItemBuilder fullData() {
    ProductDataForPurchase productData =
        ProductDataForPurchase.builder()
            .productId(10L)
            .sellerId(20L)
            .sellerName("test name")
            .productName("test product")
            .productTypeName("test product type")
            .price(1000)
            .discountAmount(100)
            .discountRate(10d)
            .build();
    return PurchaseItem.builder().productData(productData).finalPrice(10000);
  }

  public static PurchaseItem makePurchaseItem(long id) {
    PurchaseItem purchaseItem = fullData().build();
    ReflectionTestUtils.setField(purchaseItem, "id", id);
    return purchaseItem;
  }

  public static PurchaseItem makePurchaseItem(long id, Member seller) {
    ProductDataForPurchase productData =
        ProductDataForPurchase.builder()
            .productId(10L)
            .sellerId(seller.getId())
            .sellerName(seller.getNickName())
            .productName("test product")
            .productTypeName("test product type")
            .price(1000)
            .discountAmount(100)
            .discountRate(10d)
            .build();
    PurchaseItem purchaseItem = fullData().productData(productData).finalPrice(10000).build();
    ReflectionTestUtils.setField(purchaseItem, "id", id);
    return purchaseItem;
  }

  public static PurchaseItem makePurchaseItem(long id, Member seller, LocalDateTime createDate) {
    ProductDataForPurchase productData =
        ProductDataForPurchase.builder()
            .productId(10L)
            .sellerId(seller.getId())
            .sellerName(seller.getNickName())
            .productName("test product")
            .productTypeName("test product type")
            .price(1000)
            .discountAmount(100)
            .discountRate(10d)
            .build();
    PurchaseItem purchaseItem = fullData().productData(productData).finalPrice(10000).build();
    ReflectionTestUtils.setField(purchaseItem, "id", id);
    ReflectionTestUtils.setField(purchaseItem, "createDate", createDate);
    return purchaseItem;
  }

  public static PurchaseItem makePurchaseItem(long id, Product product) {
    ProductDataForPurchase productData =
        ProductDataForPurchaseBuilder.makeProductDataForPurchase(product);
    PurchaseItem purchaseItem =
        PurchaseItem.builder().productData(productData).finalPrice(10000).build();
    ReflectionTestUtils.setField(purchaseItem, "id", id);
    return purchaseItem;
  }

  public static PurchaseItem makePurchaseItem(long id, Product product, LocalDateTime createDate) {
    ProductDataForPurchase productData =
        ProductDataForPurchaseBuilder.makeProductDataForPurchase(product);
    PurchaseItem purchaseItem =
        PurchaseItem.builder().productData(productData).finalPrice(10000).build();
    ReflectionTestUtils.setField(purchaseItem, "id", id);
    ReflectionTestUtils.setField(purchaseItem, "createDate", createDate);
    return purchaseItem;
  }

  public static PurchaseItem makePurchaseItem(long id, Product product, Review review) {
    ProductDataForPurchase productData =
        ProductDataForPurchaseBuilder.makeProductDataForPurchase(product);
    PurchaseItem purchaseItem =
        PurchaseItem.builder().productData(productData).finalPrice(10000).build();
    ReflectionTestUtils.setField(purchaseItem, "id", id);
    purchaseItem.registerReview(review);
    return purchaseItem;
  }

  public static List<PurchaseItem> makePurchaseItemList(List<Long> idList, Product product) {
    return idList.stream().map(id -> makePurchaseItem(id, product)).toList();
  }

  public static List<PurchaseItem> makePurchaseItemList(
      List<Long> idList, Product product, LocalDateTime createDate) {
    return idList.stream().map(id -> makePurchaseItem(id, product, createDate)).toList();
  }
}
