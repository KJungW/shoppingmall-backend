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
  private String reviewImageUri;
  private String reviewImageDownloadUrl;
  private String description;

  @Builder
  public Review(
      Member writer,
      Product product,
      String reviewImageUri,
      String reviewImageDownloadUrl,
      Integer score,
      String title,
      String description) {

    if (writer == null || product == null)
      throw new ServerLogicError("Review를 빌더로 생성할때 필수값을 넣어주지 않았습니다.");

    this.writer = writer;
    this.product = product;
    updateScore(score);
    updateTitle(title);
    updateIsBan(false);
    updateReviewImage(reviewImageUri, reviewImageDownloadUrl);
    updateDescription(description);
  }

  public void updateScore(Integer score) {
    if (score == null || score < 0 || score > 5)
      throw new ServerLogicError("Review의 score에 0~5 범위 밖의 값이 입력되었습니다.");
    this.score = score;
  }

  public void updateTitle(String title) {
    if (title == null || title.isBlank())
      throw new ServerLogicError("Review의 title에 비어있는 입력되었습니다.");
    this.title = title;
  }

  public void updateReviewImage(String imageUri, String downloadUrl) {
    if (imageUri == null || imageUri.isBlank() || downloadUrl == null || downloadUrl.isBlank()) {
      this.reviewImageUri = "";
      this.reviewImageDownloadUrl = "";
      return;
    }
    this.reviewImageUri = imageUri;
    this.reviewImageDownloadUrl = downloadUrl;
  }

  public void updateDescription(String description) {
    if (description == null || description.isBlank()) this.description = "";
    else this.description = description;
  }

  public void updateIsBan(Boolean isBan) {
    if (isBan == null) throw new ServerLogicError("Review의 isBan에 비어있는 입력되었습니다.");
    this.isBan = isBan;
  }
}
