package com.project.shoppingmall.test_entity.review;

import com.project.shoppingmall.dto.review.ReviewUpdateData;
import com.project.shoppingmall.testutil.TestUtil;
import org.springframework.web.multipart.MultipartFile;

public class ReviewUpdateDataBuilder {
  public static ReviewUpdateData.ReviewUpdateDataBuilder fullData() {
    MultipartFile givenMockFile =
        TestUtil.loadTestFile("reviewSampleImage.png", "static/reviewSampleImage.png");
    return ReviewUpdateData.builder()
        .writerId(30L)
        .reviewID(40L)
        .score(5)
        .title("test review title")
        .description("test review description")
        .reviewImage(givenMockFile);
  }

  public static ReviewUpdateData make(long writerId, long reviewId) {
    return fullData().writerId(writerId).reviewID(reviewId).build();
  }

  public static ReviewUpdateData makeMinimum(long writerId, long reviewId) {
    return fullData()
        .writerId(writerId)
        .reviewID(reviewId)
        .description(null)
        .reviewImage(null)
        .build();
  }
}
