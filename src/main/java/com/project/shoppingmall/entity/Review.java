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
public class Review extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "WRITER_ID")
  private Member writer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  private Integer score;
  private String title;
  private Boolean isBan;
  private String reviewImageUrl;
  private String description;

  @Builder
  public Review(
      Member writer,
      Product product,
      String reviewImageUrl,
      Integer score,
      String title,
      String description) {

    if (writer == null
        || product == null
        || score == null
        || score < 0
        || score > 5
        || title == null
        || title.isBlank()) throw new ServerLogicError("Review를 빌더로 생성할때 필수값을 넣어주지 않았습니다.");
    this.writer = writer;
    this.product = product;
    this.score = score;
    this.title = title;
    this.isBan = false;

    if (reviewImageUrl == null || reviewImageUrl.isBlank()) this.reviewImageUrl = "";
    else this.reviewImageUrl = reviewImageUrl;

    if (description == null || description.isBlank()) this.description = "";
    else this.description = description;
  }
}
