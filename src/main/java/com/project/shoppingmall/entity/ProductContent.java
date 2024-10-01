package com.project.shoppingmall.entity;

import com.project.shoppingmall.type.BlockType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductContent extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  @Enumerated(EnumType.STRING)
  private BlockType type;

  @Column(columnDefinition = "JSON")
  private String content;

  @Builder
  public ProductContent(Product product, BlockType type, String content) {
    this.product = product;
    this.type = type;
    this.content = content;
  }

  public void updateProduct(Product product) {
    this.product = product;
  }
}
