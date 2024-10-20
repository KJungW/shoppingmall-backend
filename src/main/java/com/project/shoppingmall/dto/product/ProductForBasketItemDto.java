package com.project.shoppingmall.dto.product;

import com.project.shoppingmall.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductForBasketItemDto {
  private Long productId;
  private Long sellerId;
  private String sellerNickname;
  private Long productTypeId;
  private String productTypeName;
  private String firstProductImgDownloadUrl;
  private String name;
  private Integer price;
  private Integer discountAmount;
  private Double discountRate;
  private Boolean isBan;
  private Double scoreAvg;

  public ProductForBasketItemDto(Product product) {
    this.productId = product.getId();
    this.sellerId = product.getSeller().getId();
    this.sellerNickname = product.getSeller().getNickName();
    this.productTypeId = product.getProductType().getId();
    this.productTypeName = product.getProductType().getTypeName();
    this.firstProductImgDownloadUrl = product.getProductImages().get(0).getDownLoadUrl();
    this.name = product.getName();
    this.price = product.getPrice();
    this.discountAmount = product.getDiscountAmount();
    this.discountRate = product.getDiscountRate();
    this.isBan = product.getIsBan();
    this.scoreAvg = product.getScoreAvg();
  }
}
