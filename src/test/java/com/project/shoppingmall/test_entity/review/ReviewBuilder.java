package com.project.shoppingmall.test_entity.review;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import org.springframework.test.util.ReflectionTestUtils;

public class ReviewBuilder {
  public static Review.ReviewBuilder fullData() {
    return Review.builder()
        .writer(MemberBuilder.makeMember(13012L))
        .product(ProductBuilder.makeProduct(5203L))
        .score(5)
        .title("testTitle")
        .reviewImageUri("testImageUri")
        .reviewImageDownloadUrl("testImageUrl")
        .description("testDescription");
  }

  public static Review makeReview(long id) {
    Review givenReview = fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", id);
    return givenReview;
  }

  public static Review makeReview(long id, Member writer, boolean isBan) {
    Review givenReview = fullData().writer(writer).build();
    ReflectionTestUtils.setField(givenReview, "id", id);
    ReflectionTestUtils.setField(givenReview, "isBan", isBan);
    return givenReview;
  }

  public static Review makeReview(long id, Member writer, Product product) {
    Review givenReview = ReviewBuilder.fullData().writer(writer).product(product).build();
    ReflectionTestUtils.setField(givenReview, "id", id);
    return givenReview;
  }
}
