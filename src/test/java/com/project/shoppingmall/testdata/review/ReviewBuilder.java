package com.project.shoppingmall.testdata.review;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.testdata.product.ProductBuilder;
import org.springframework.test.util.ReflectionTestUtils;

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

  public static Review makeReview(long id, Member reviewer, Product product) {
    Review givenReview = ReviewBuilder.fullData().writer(reviewer).product(product).build();
    ReflectionTestUtils.setField(givenReview, "id", id);
    return givenReview;
  }
}
