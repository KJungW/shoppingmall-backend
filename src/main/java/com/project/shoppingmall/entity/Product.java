package com.project.shoppingmall.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
  @Id @GeneratedValue private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SELLER_ID")
  private Member seller;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_TYPE_ID")
  private ProductType productType;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductImage> productImages;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductContent> contents;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "SINGLE_OPTION_ID")
  private ProductSingleOption singleOption;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductMultipleOption> multipleOptions;

  private String name;
  private Integer price;
  private Integer discountAmount;
  private Double discountRate;
  private Boolean isBan;

  @Builder
  public Product(
      Member seller,
      ProductType productType,
      List<ProductImage> productImages,
      List<ProductContent> contents,
      ProductSingleOption singleOption,
      List<ProductMultipleOption> multipleOptions,
      String name,
      Integer price,
      Integer discountAmount,
      Double discountRate,
      Boolean isBan) {
    this.seller = seller;
    this.productType = productType;
    this.productImages = productImages;
    this.contents = contents;
    this.singleOption = singleOption;
    this.multipleOptions = multipleOptions;
    this.name = name;
    this.price = price;
    this.discountAmount = discountAmount;
    this.discountRate = discountRate;
    this.isBan = isBan;
  }
}
