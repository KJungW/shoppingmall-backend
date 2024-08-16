package com.project.shoppingmall.entity;

import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.ProductSaleType;
import com.project.shoppingmall.util.PriceCalculateUtil;
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
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductSingleOption> singleOptions = new ArrayList<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProductMultipleOption> multipleOptions = new ArrayList<>();

  private String name;
  private Integer price;
  private Integer discountAmount;
  private Double discountRate;
  private Boolean isBan;
  private Double scoreAvg;
  private Integer finalPrice;

  @Enumerated(EnumType.STRING)
  private ProductSaleType saleState;

  @Builder
  public Product(
      Member seller,
      ProductType productType,
      List<ProductImage> productImages,
      List<ProductContent> contents,
      List<ProductSingleOption> singleOptions,
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
    changeSalesStateToOnSale();
    updateSingleOption(singleOptions);
    updateMultiOptions(multipleOptions);
    updateProductImages(productImages);
    updateContents(contents);
    CalcFinalPrice();
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
    this.finalPrice =
        PriceCalculateUtil.calculatePrice(this.price, this.discountAmount, this.discountRate);
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

  public void updateSingleOption(List<ProductSingleOption> singleOptions) {
    this.singleOptions.clear();
    for (ProductSingleOption option : singleOptions) {
      this.singleOptions.add(option);
      option.updateProduct(this);
    }
  }

  public void updateMultiOptions(List<ProductMultipleOption> multipleOptions) {
    this.multipleOptions.clear();
    for (ProductMultipleOption option : multipleOptions) {
      this.multipleOptions.add(option);
      option.updateProduct(this);
    }
  }

  public void CalcFinalPrice() {
    this.finalPrice =
        PriceCalculateUtil.calculatePrice(this.price, this.discountAmount, this.discountRate);
  }

  public void addScore(ReviewScoresCalcResult scoresCalcResult, double currentScore) {
    if (scoresCalcResult == null) throw new ServerLogicError("scoresCalcResult가 null 입니다.");
    double previousAvg = scoresCalcResult.getScoreAverage();
    long previousCnt = scoresCalcResult.getReviewCount();
    this.scoreAvg = (previousAvg * previousCnt + currentScore) / (previousCnt + 1);
  }

  public void refreshScore(ReviewScoresCalcResult scoresCalcResult) {
    if (scoresCalcResult == null) throw new ServerLogicError("scoresCalcResult가 null 입니다.");
    this.scoreAvg = scoresCalcResult.getScoreAverage();
  }

  public void changeSalesStateToOnSale() {
    this.saleState = ProductSaleType.ON_SALE;
  }

  public void changeSalesStateToDiscontinued() {
    this.saleState = ProductSaleType.DISCONTINUED;
  }
}
