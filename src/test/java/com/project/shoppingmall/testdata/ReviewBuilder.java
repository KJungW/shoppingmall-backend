package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Review;
import java.io.IOException;

public class ReviewBuilder {
  public static Review.ReviewBuilder fullData() throws IOException {
    return Review.builder()
        .writer(MemberBuilder.fullData().build())
        .product(ProductBuilder.fullData().build())
        .score(5)
        .title("testTitle")
        .reviewImageUri("testImageUri")
        .reviewImageDownloadUrl("testImageUrl")
        .description("testDescription");
  }
}
