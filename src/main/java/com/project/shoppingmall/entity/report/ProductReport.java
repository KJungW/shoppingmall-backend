package com.project.shoppingmall.entity.report;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("PRODUCT_REPORT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductReport extends Report {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  @Builder
  public ProductReport(
      Member reporter, Member targetMember, String title, String description, Product product) {
    super(reporter, targetMember, title, description, false);
    if (reporter == null
        || targetMember == null
        || title.isEmpty()
        || description.isEmpty()
        || product == null) throw new ServerLogicError("ProductReport를 생성할때 필수값을 넣어주지 않았습니다.");
    this.product = product;
  }
}
