package com.project.shoppingmall.testdata;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

public class PurchaseItemBuilder {
  public static PurchaseItem.PurchaseItemBuilder fullData() throws IOException {
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

  public static PurchaseItem makePurchaseItem(Long id, Member seller) {
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

  public static PurchaseItem makePurchaseItem(Long id, Member seller, LocalDateTime createDate) {
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

  public static PurchaseItem makePurchaseItem(Product product) throws IOException {
    ProductDataForPurchase productOptionObj =
        ProductDataForPurchaseBuilder.fullData(product).build();
    return PurchaseItemBuilder.fullData()
        .productData(productOptionObj)
        .finalPrice(product.getFinalPrice())
        .build();
  }
}
