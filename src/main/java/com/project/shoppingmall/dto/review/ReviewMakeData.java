package com.project.shoppingmall.dto.review;

import com.project.shoppingmall.exception.ServerLogicError;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class ReviewMakeData {
  private Long writerId;
  private Long purchaseItemId;
  private Integer score;
  private String title;
  private String description;
  private MultipartFile reviewImage;

  @Builder
  public ReviewMakeData(
      Long writerId,
      Long purchaseItemId,
      Integer score,
      String title,
      String description,
      MultipartFile reviewImage) {
    if (writerId == null
        || purchaseItemId == null
        || score == null
        || score < 0
        || score > 5
        || title == null
        || title.isBlank()) throw new ServerLogicError("ReviewMakeData를 빌더로 생성할때 필수값을 넣어주지 않았습니다.");
    this.writerId = writerId;
    this.purchaseItemId = purchaseItemId;
    this.score = score;
    this.title = title;
    if (description == null || description.isBlank()) this.description = "";
    else this.description = description;
    this.reviewImage = reviewImage;
  }
}
