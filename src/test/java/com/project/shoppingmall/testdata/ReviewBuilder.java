package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
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

  public static Review makeReview(Member reviewer, Product product) throws IOException {
    return ReviewBuilder.fullData().writer(reviewer).product(product).build();
  }
}
