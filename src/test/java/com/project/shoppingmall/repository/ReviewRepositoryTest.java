package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import com.project.shoppingmall.type.PurchaseStateType;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class ReviewRepositoryTest {
  @Autowired private ReviewRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;

  @Test
  @DisplayName("calcReviewScoresInProduct : 정상흐름")
  public void calcReviewScoresInProduct_ok() {
    // given
    long inputProductId;

    Product product = testDataMaker.saveProduct();
    Purchase purchase = testDataMaker.savePurchase(product, 5, PurchaseStateType.COMPLETE);
    List<Review> lowScoreReviewList =
        testDataMaker.saveReviewList(product, purchase.getPurchaseItems(), 0);
    List<Review> highScoreReviewList =
        testDataMaker.saveReviewList(product, purchase.getPurchaseItems(), 5);

    inputProductId = product.getId();

    em.flush();
    em.clear();

    // when
    ReviewScoresCalcResult result = target.calcReviewScoresInProduct(inputProductId);

    // then
    checkReviewScoresCalcResult(10, 2.5d, result);
  }

  @Test
  @DisplayName("calcReviewScoresInProduct : 리뷰가 하나도 존재하지 않을 경우")
  public void calcReviewScoresInProduct_noReview() {
    // given
    long inputProductId = 321412L;

    // when
    ReviewScoresCalcResult result = target.calcReviewScoresInProduct(inputProductId);

    // then
    checkReviewScoresCalcResult(0, 0, result);
  }

  public void checkReviewScoresCalcResult(int count, double avg, ReviewScoresCalcResult target) {
    assertEquals(count, target.getReviewCount());
    assertEquals(avg, target.getScoreAverage());
  }
}
