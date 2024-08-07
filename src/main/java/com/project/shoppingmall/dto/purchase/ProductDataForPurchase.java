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

  private String productName;
  private String productTypeName;
  private ProductOptionDto singleOption;
  private List<ProductOptionDto> multiOptions;
  private Integer price;
  private Integer discountAmount;
  private Double discountRate;

  @Builder
  public ProductDataForPurchase(
      String productName,
      String productTypeName,
      ProductOptionDto singleOption,
      List<ProductOptionDto> multiOptions,
      Integer price,
      Integer discountAmount,
      Double discountRate) {
    if (productName.isEmpty()
        || productTypeName.isEmpty()
        || singleOption == null
        || multiOptions.isEmpty()
        || price <= 0
        || discountAmount == null
        || discountRate == null) {
      throw new ServerLogicError("ProductDataForPurchase를 빌더로 생성할때 필수값을 넣어주지 않았습니다.");
    }
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
