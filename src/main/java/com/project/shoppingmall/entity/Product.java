package com.project.shoppingmall.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
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
  private List<ProductImage> productImages = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductContent> contents = new ArrayList<>();

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "SINGLE_OPTION_ID")
  private ProductSingleOption singleOption;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductMultipleOption> multipleOptions = new ArrayList<>();

  private String name;
  private Integer price;
  private Integer discountAmount;
  private Double discountRate;
  private Boolean isBan;
  private Double scoreAvg;

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
      Boolean isBan,
      Double scoreAvg) {
    this.seller = seller;
    this.productType = productType;
    this.name = name;
    this.price = price;
    this.discountAmount = discountAmount;
    this.discountRate = discountRate;
    this.isBan = isBan;
    this.scoreAvg = scoreAvg;
    updateSingleOption(singleOption);
    updateMultiOptions(multipleOptions);
    updateProductImages(productImages);
    updateContents(contents);
  }

  public void changeProductType(ProductType type) {
    this.productType = type;
  }

  public void changeProductName(String name) {
    this.name = name;
  }

  public void changePrice(Integer price, Integer discountAmount, Double discountRate) {
    this.price = price;
    this.discountAmount = discountAmount;
    this.discountRate = discountRate;
  }

  public void updateProductImages(List<ProductImage> productImages) {
    this.productImages.clear();
    for (ProductImage image : productImages) {
      this.productImages.add(image);
      image.updateProduct(this);
    }
  }

  public void updateContents(List<ProductContent> productContents) {
    this.contents.clear();
    for (ProductContent content : productContents) {
      this.contents.add(content);
      content.updateProduct(this);
    }
  }

  public void updateSingleOption(ProductSingleOption singleOption) {
    this.singleOption = singleOption;
  }

  public void updateMultiOptions(List<ProductMultipleOption> multipleOptions) {
    this.multipleOptions.clear();
    for (ProductMultipleOption option : multipleOptions) {
      this.multipleOptions.add(option);
      option.updateProduct(this);
    }
  }
}
