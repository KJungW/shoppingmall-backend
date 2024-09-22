package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;

public class ReviewBuilder {
  public static Review.ReviewBuilder fullData() {
    return Review.builder()
        .writer(MemberBuilder.fullData().build())
        .product(ProductBuilder.fullData().build())
        .score(5)
        .title("testTitle")
        .reviewImageUri("testImageUri")
        .reviewImageDownloadUrl("testImageUrl")
        .description("testDescription");
  }

  public static Review makeReview(Member reviewer, Product product) {
    return ReviewBuilder.fullData().writer(reviewer).product(product).build();
  }
}
