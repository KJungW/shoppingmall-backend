package com.project.shoppingmall.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSingleOption extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  private String optionName;
  private Integer priceChangeAmount;

  @Builder
  public ProductSingleOption(Product product, String optionName, Integer priceChangeAmount) {
    this.product = product;
    this.optionName = optionName;
    this.priceChangeAmount = priceChangeAmount;
  }

  public void updateProduct(Product product) {
    this.product = product;
  }
}
