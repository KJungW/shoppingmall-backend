package com.project.shoppingmall.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  private String imageUri;

  private String downLoadUrl;

  @Builder
  public ProductImage(Product product, String imageUri, String downLoadUrl) {
    this.product = product;
    this.imageUri = imageUri;
    this.downLoadUrl = downLoadUrl;
  }

  public void updateProduct(Product product) {
    this.product = product;
  }
}
