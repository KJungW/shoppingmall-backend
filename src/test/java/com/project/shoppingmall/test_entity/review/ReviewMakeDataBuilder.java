package com.project.shoppingmall.test_entity.review;

import com.project.shoppingmall.dto.review.ReviewMakeData;
import com.project.shoppingmall.testutil.TestUtil;
import org.springframework.web.multipart.MultipartFile;

public class ReviewMakeDataBuilder {
  public static ReviewMakeData.ReviewMakeDataBuilder fullData() {
    MultipartFile givenMockFile =
        TestUtil.loadTestFile("reviewSampleImage.png", "static/reviewSampleImage.png");
    return ReviewMakeData.builder()
        .writerId(30L)
        .purchaseItemId(40L)
        .score(5)
        .title("test review title")
        .description("test review description")
        .reviewImage(givenMockFile);
  }

  public static ReviewMakeData make(long writerId, long purchaseItemId) {
    return fullData().writerId(writerId).purchaseItemId(purchaseItemId).build();
  }

  public static ReviewMakeData makeMinimum(long writerId, long purchaseItemId) {
    return fullData()
        .writerId(writerId)
        .purchaseItemId(purchaseItemId)
        .description(null)
        .reviewImage(null)
        .build();
  }
}
