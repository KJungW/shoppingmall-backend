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
public class ChatRoom {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "BUYER_ID")
  private Member buyer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SELLER_ID")
  private Member seller;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  @Builder
  public ChatRoom(Member buyer, Product product) {
    if (buyer == null) throw new ServerLogicError("ChatRoom의 buyer필드에 비어있는 값이 입력되었습니다.");
    if (product == null) throw new ServerLogicError("ChatRoom의 product필드에 비어있는 값이 입력되었습니다.");
    if (product.getSeller() == null)
      throw new ServerLogicError("ChatRoom의 seller필드에 비어있는 값이 입력되었습니다.");

    this.buyer = buyer;
    this.product = product;
    this.seller = product.getSeller();
  }

  public boolean checkMemberIsParticipant(Member member) {
    return this.getSeller().getId().equals(member.getId())
        || this.getBuyer().getId().equals(member.getId());
  }
}
