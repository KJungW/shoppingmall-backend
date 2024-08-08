package com.project.shoppingmall.dto.purchase;

import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.util.JsonUtil;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseItemDto {
  private Long purchaseItemId;
  private long productId;
  private long sellerId;
  private String sellerName;
  private String productName;
  private String productTypeName;
  private ProductOptionDto selectedSingleOption;
  private List<ProductOptionDto> selectedMultiOptions;
  private int price;
  private int discountAmount;
  private double discountRate;
  private int finalPrice;

  public PurchaseItemDto(PurchaseItem purchaseItem) {
    ProductDataForPurchase productData =
        JsonUtil.convertJsonToObject(purchaseItem.getProductData(), ProductDataForPurchase.class);
    this.purchaseItemId = purchaseItem.getId();
    this.productId = productData.getProductId();
    this.sellerId = productData.getSellerId();
    this.sellerName = productData.getSellerName();
    this.productName = productData.getProductName();
    this.productTypeName = productData.getProductTypeName();
    this.selectedSingleOption = productData.getSingleOption();
    this.selectedMultiOptions = productData.getMultiOptions();
    this.price = productData.getPrice();
    this.discountAmount = productData.getDiscountAmount();
    this.discountRate = productData.getDiscountRate();
    this.finalPrice = purchaseItem.getFinalPrice();
  }
}
