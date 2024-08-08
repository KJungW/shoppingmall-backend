package com.project.shoppingmall.dto.purchase;

import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.util.JsonUtil;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductDataForPurchase {
  private long productId;
  private long sellerId;
  private String sellerName;
  private String productName;
  private String productTypeName;
  private ProductOptionDto singleOption;
  private List<ProductOptionDto> multiOptions;
  private int price;
  private int discountAmount;
  private double discountRate;

  @Builder
  public ProductDataForPurchase(
      long productId,
      long sellerId,
      String sellerName,
      String productName,
      String productTypeName,
      ProductOptionDto singleOption,
      List<ProductOptionDto> multiOptions,
      int price,
      int discountAmount,
      double discountRate) {
    if (sellerName.isEmpty() || productName.isEmpty() || productTypeName.isEmpty()) {
      throw new ServerLogicError("ProductDataForPurchase를 빌더로 생성할때 필수값을 넣어주지 않았습니다.");
    }
    this.productId = productId;
    this.sellerId = sellerId;
    this.sellerName = sellerName;
    this.productName = productName;
    this.productTypeName = productTypeName;
    this.singleOption = singleOption;
    this.multiOptions = multiOptions;
    this.price = price;
    this.discountAmount = discountAmount;
    this.discountRate = discountRate;
  }

  public String makeJson() {
    return JsonUtil.convertObjectToJson(this);
  }
}
