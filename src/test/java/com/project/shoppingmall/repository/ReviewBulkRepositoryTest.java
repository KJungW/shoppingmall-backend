package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

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
class ReviewBulkRepositoryTest {
  @Autowired private ReviewBulkRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;

  @Test
  @DisplayName("banReviewsByWriterId() : 정상흐름")
  public void banReviewsByWriterId_ok() {
    // given
    long inputWriterId;
    boolean inputIsBan = true;

    Product givenProduct = testDataMaker.saveProduct();
    Member givenBuyer = testDataMaker.saveMember();
    Purchase givenPurchase =
        testDataMaker.savePurchase(givenBuyer, givenProduct, 5, PurchaseStateType.COMPLETE);
    List<Review> givenReviews =
        testDataMaker.saveReviewList(givenBuyer, givenProduct, givenPurchase.getPurchaseItems());

    inputWriterId = givenBuyer.getId();
    em.flush();
    em.clear();

    // when
    int rowCount = target.banReviewsByWriterId(inputWriterId, inputIsBan);

    // target
    check_rowCount(5, rowCount);
    checkResult_inputWriterId(inputWriterId, inputIsBan);
  }

  @Test
  @DisplayName("banReviewsByWriterId() : 리뷰데이터 없음")
  public void banReviewsByWriterId_noReview() {
    long inputWriterId;
    boolean inputIsBan = true;

    Product givenProduct = testDataMaker.saveProduct();
    Member givenBuyer = testDataMaker.saveMember();
    Purchase givenPurchase =
        testDataMaker.savePurchase(givenBuyer, givenProduct, 5, PurchaseStateType.COMPLETE);

    inputWriterId = givenBuyer.getId();
    em.flush();
    em.clear();

    // when
    int rowCount = target.banReviewsByWriterId(inputWriterId, inputIsBan);

    // target
    check_rowCount(0, rowCount);
  }

  public void check_rowCount(long expectedRowCount, long resultRowCount) {
    assertEquals(expectedRowCount, resultRowCount);
  }

  public void checkResult_inputWriterId(long writerId, boolean isBan) {
    String jpql = "select r From Review r " + "left join r.writer w " + "where w.id = :writerId ";
    List<Review> resultReviews =
        em.createQuery(jpql, Review.class).setParameter("writerId", writerId).getResultList();
    resultReviews.forEach(review -> assertEquals(isBan, review.getIsBan()));
  }
}
