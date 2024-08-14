package com.project.shoppingmall.dto.review;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class ReviewUpdateData {
  private Long writerId;
  private Long reviewID;
  private Integer score;
  private String title;
  private String description;
  private MultipartFile reviewImage;

  @Builder
  public ReviewUpdateData(
      Long writerId,
      Long reviewID,
      Integer score,
      String title,
      String description,
      MultipartFile reviewImage) {
    this.writerId = writerId;
    this.reviewID = reviewID;
    this.score = score;
    this.title = title;
    this.description = description;
    this.reviewImage = reviewImage;
  }
}
