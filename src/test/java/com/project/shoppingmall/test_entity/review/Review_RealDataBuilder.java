package com.project.shoppingmall.test_entity.review;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;

public class Review_RealDataBuilder {
  public static Review makeReview(Member reviewer, Product product) {
    return ReviewBuilder.fullData().writer(reviewer).product(product).build();
  }

  public static Review makeReview(Member reviewer, Product product, int score) {
    return ReviewBuilder.fullData().writer(reviewer).product(product).score(score).build();
  }
}
