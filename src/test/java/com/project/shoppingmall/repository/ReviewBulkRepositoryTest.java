package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.*;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class ReviewBulkRepositoryTest {
  @Autowired private ReviewBulkRepository target;
  @Autowired private EntityManager em;

  @BeforeEach
  public void beforeEach() throws IOException {
    // 판매자와 구매자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member buyer = MemberBuilder.fullData().build();
    em.persist(buyer);

    // 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);

    // 구매할 제품 생성
    Product targetProduct = ProductBuilder.makeNoBannedProduct(seller, type);
    em.persist(targetProduct);

    // 5개의 Complete상태의 Purchase 데이터 생성
    for (int i = 0; i < 5; i++) {
      List<PurchaseItem> purchaseItems = new ArrayList<>();
      // Purchase마다 3개의 PurchaseItem 생성
      for (int k = 0; k < 3; k++) {
        PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(targetProduct);
        purchaseItems.add(purchaseItem);

        // PurchaseItem마다 Review 생성
        Review review = ReviewBuilder.makeReview(buyer, targetProduct);
        ReflectionTestUtils.setField(review, "isBan", false);
        purchaseItem.registerReview(review);
        em.persist(review);
      }

      Purchase purchase = PurchaseBuilder.makeCompleteStatePurchase(buyer, purchaseItems);
      em.persist(purchase);
    }
  }

  @Test
  @DisplayName("banReviewsByWriterId() : 정상흐름")
  public void banReviewsByWriterId_ok() throws IOException {
    // given
    // - 새로운 판매자와 구매자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member buyer = MemberBuilder.fullData().build();
    em.persist(buyer);
    long givenWriterId = buyer.getId();

    // - 새로운 제품 타입 생성
    ProductType type = new ProductType("test$test1");
    em.persist(type);

    // - 새로운 구매할 제품 생성
    Product targetProduct = ProductBuilder.makeNoBannedProduct(seller, type);
    em.persist(targetProduct);

    // - 5개의 Complete상태의 Purchase 데이터 생성
    for (int i = 0; i < 5; i++) {
      List<PurchaseItem> purchaseItems = new ArrayList<>();
      // Purchase마다 3개의 PurchaseItem 생성
      for (int k = 0; k < 3; k++) {
        PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(targetProduct);
        purchaseItems.add(purchaseItem);

        // PurchaseItem마다 Review 생성
        Review review = ReviewBuilder.makeReview(buyer, targetProduct);
        ReflectionTestUtils.setField(review, "isBan", false);
        purchaseItem.registerReview(review);
        em.persist(review);
      }

      Purchase purchase = PurchaseBuilder.makeCompleteStatePurchase(buyer, purchaseItems);
      em.persist(purchase);
    }
    em.flush();
    em.clear();

    // - 인자세팅
    long inputWriterId = givenWriterId;
    boolean inputIsBan = true;
    ;

    // when
    int rowCount = target.banReviewsByWriterId(inputWriterId, inputIsBan);

    // target
    assertEquals(15, rowCount);

    String jpql = "select r From Review r " + "left join r.writer w " + "where w.id = :writerId ";
    List<Review> resultReviews =
        em.createQuery(jpql, Review.class).setParameter("writerId", givenWriterId).getResultList();
    assertEquals(15, resultReviews.size());
    resultReviews.forEach(review -> assertEquals(inputIsBan, review.getIsBan()));
  }

  @Test
  @DisplayName("banReviewsByWriterId() : 리뷰데이터 없음")
  public void banReviewsByWriterId_noReview() throws IOException {
    // given
    // - 새로운 판매자와 구매자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member buyer = MemberBuilder.fullData().build();
    em.persist(buyer);
    long givenWriterId = buyer.getId();

    // - 새로운 제품 타입 생성
    ProductType type = new ProductType("test$test1");
    em.persist(type);

    // - 새로운 구매할 제품 생성
    Product targetProduct = ProductBuilder.makeNoBannedProduct(seller, type);
    em.persist(targetProduct);

    // - 5개의 Complete상태의 Purchase 데이터 생성
    for (int i = 0; i < 5; i++) {
      List<PurchaseItem> purchaseItems = new ArrayList<>();
      // Purchase마다 3개의 PurchaseItem 생성
      for (int k = 0; k < 3; k++) {
        PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(targetProduct);
        purchaseItems.add(purchaseItem);
      }

      Purchase purchase = PurchaseBuilder.makeCompleteStatePurchase(buyer, purchaseItems);
      em.persist(purchase);
    }
    em.flush();
    em.clear();

    // - 인자세팅
    long inputWriterId = givenWriterId;
    boolean inputIsBan = true;
    ;

    // when
    int rowCount = target.banReviewsByWriterId(inputWriterId, inputIsBan);

    // target
    assertEquals(0, rowCount);

    String jpql = "select r From Review r " + "left join r.writer w " + "where w.id = :writerId ";
    List<Review> resultReviews =
        em.createQuery(jpql, Review.class).setParameter("writerId", givenWriterId).getResultList();
    assertEquals(0, resultReviews.size());
  }
}
