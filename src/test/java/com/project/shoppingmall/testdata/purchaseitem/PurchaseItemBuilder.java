package com.project.shoppingmall.testdata.purchaseitem;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.testdata.product.ProductDataForPurchaseBuilder;
import java.time.LocalDateTime;
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
    PurchaseItem purchaseItem =
        PurchaseItem.builder().productData(productData).finalPrice(10000).build();
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
    PurchaseItem purchaseItem =
        PurchaseItem.builder().productData(productData).finalPrice(10000).build();
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
}
