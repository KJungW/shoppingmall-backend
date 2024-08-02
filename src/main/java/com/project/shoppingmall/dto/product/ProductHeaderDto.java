package com.project.shoppingmall.dto.product;

import com.project.shoppingmall.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductHeaderDto {
  private Long productId;
  private Long sellerId;
  private String sellerName;
  private Long typeId;
  private String typeName;
  private String firstProductImageUrl;
  private String name;
  private Integer price;
  private Integer discountAmount;
  private Double discountRate;
  private Boolean isBan;
  private Double scoreAvg;
  private Integer finalPrice;

  public ProductHeaderDto(Product product) {
    this.productId = product.getId();
    this.sellerId = product.getSeller().getId();
    this.sellerName = product.getSeller().getNickName();
    this.typeId = product.getProductType().getId();
    this.typeName = product.getProductType().getTypeName();
    this.firstProductImageUrl = product.getProductImages().get(0).getDownLoadUrl();
    this.name = product.getName();
    this.price = product.getPrice();
    this.discountAmount = product.getDiscountAmount();
    this.discountRate = product.getDiscountRate();
    this.isBan = product.getIsBan();
    this.scoreAvg = product.getScoreAvg();
    this.finalPrice = product.getFinalPrice();
  }
}
