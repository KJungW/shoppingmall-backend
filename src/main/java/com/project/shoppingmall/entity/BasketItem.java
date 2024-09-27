package com.project.shoppingmall.entity;

import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BasketItem extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MEMBER_ID")
  private Member member;

  @JoinColumn(name = "PRODUCT_ID")
  @ManyToOne(fetch = FetchType.LAZY)
  private Product product;

  @Column(columnDefinition = "JSON")
  private String options;

  @Builder
  public BasketItem(Member member, Product product, String options) {
    if (member == null || product == null) {
      throw new ServerLogicError("BasketItem은 member와 product에 필수로 값이 들어가야합니다.");
    }
    this.member = member;
    this.product = product;
    this.options = options;
  }
}
