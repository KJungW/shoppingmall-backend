package com.project.shoppingmall.dto.basket;

import com.project.shoppingmall.exception.ServerLogicError;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BasketItemMakeData {
  private Long memberId;
  private Long productId;
  private Long singleOptionId;
  private List<Long> multipleOptionId = new ArrayList<>();

  @Builder
  public BasketItemMakeData(
      Long memberId, Long productId, Long singleOptionId, List<Long> multipleOptionId) {
    if (memberId == null || productId == null) {
      throw new ServerLogicError("BasketItemMakeData를 생성할때, memberId와 productId는 필수값입니다.");
    }
    this.memberId = memberId;
    this.productId = productId;
    this.singleOptionId = singleOptionId;
    if (multipleOptionId != null) {
      this.multipleOptionId = multipleOptionId;
    }
  }
}
