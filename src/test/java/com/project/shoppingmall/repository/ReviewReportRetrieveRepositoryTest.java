package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.testdata.*;
import com.project.shoppingmall.type.PurchaseStateType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class ReviewReportRetrieveRepositoryTest {
  @Autowired private ReviewReportRetrieveRepository target;
  @Autowired private EntityManager em;
  private PersistenceUnitUtil emUtil;

  @BeforeEach
  public void beforeEach() throws IOException {
    emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();

    // 판매자와 구매자 그리고 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member buyer = MemberBuilder.fullData().build();
    em.persist(buyer);
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

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
        purchaseItem.registerReview(review);
        em.persist(review);

        // Review마다 처리되지 않은 신고데이터 생성
        ReviewReport noProcessedReport =
            ReviewReportBuilder.makeNoProcessedReviewReport(reporter, review);
        em.persist(noProcessedReport);

        // Review마다 처리완료된 신고데이터 생성
        ReviewReport processedReport =
            ReviewReportBuilder.makeProcessedReviewReport(reporter, review);
        em.persist(processedReport);
      }

      Purchase purchase =
          PurchaseBuilder.makePurchase(buyer, purchaseItems, PurchaseStateType.COMPLETE);
      em.persist(purchase);
    }
  }

  @Test
  @DisplayName("findUnprocessedReviewReportReport() : 정상흐름 - 첫번째 페이지")
  public void findUnprocessedReviewReportReport_ok_firstPage() throws IOException {
    // given
    // - 새로운 판매자와 구매자 그리고 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member buyer = MemberBuilder.fullData().build();
    em.persist(buyer);
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // - 새로운 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);
    long givenProductTypeId = type.getId();

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
        purchaseItem.registerReview(review);
        em.persist(review);

        // Review마다 처리되지 않은 신고데이터 생성
        ReviewReport noProcessedReport =
            ReviewReportBuilder.makeNoProcessedReviewReport(reporter, review);
        em.persist(noProcessedReport);

        // Review마다 처리완료된 신고데이터 생성
        ReviewReport processedReport =
            ReviewReportBuilder.makeProcessedReviewReport(reporter, review);
        em.persist(processedReport);
      }

      Purchase purchase =
          PurchaseBuilder.makePurchase(buyer, purchaseItems, PurchaseStateType.COMPLETE);
      em.persist(purchase);
    }
    em.flush();
    em.clear();

    // - 인자세팅
    long inputProductTypeId = givenProductTypeId;
    int inputSliceNum = 0;
    int inputSliceSize = 10;
    PageRequest inputPageRequest =
        PageRequest.of(inputSliceNum, inputSliceSize, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<ReviewReport> sliceResult =
        target.findUnprocessedReviewReportReport(inputProductTypeId, inputPageRequest);

    // target
    // - 페이지 검증
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    List<ReviewReport> resultReviewReport = sliceResult.getContent();
    assertEquals(10, resultReviewReport.size());

    // - fetch 로딩 검증
    resultReviewReport.forEach(
        reviewReport -> {
          assertTrue(emUtil.isLoaded(reviewReport, "reporter"));
          assertTrue(emUtil.isLoaded(reviewReport, "review"));
          assertTrue(emUtil.isLoaded(reviewReport.getReview().getWriter(), "writer"));
        });

    // - where 조건 검증
    resultReviewReport.forEach(
        reviewReport -> {
          assertEquals(
              givenProductTypeId, reviewReport.getReview().getProduct().getProductType().getId());
        });
    resultReviewReport.forEach(
        reviewReport -> {
          assertFalse(reviewReport.isProcessedComplete());
        });
  }

  @Test
  @DisplayName("findUnprocessedReviewReportReport() : 정상흐름 - 마지막 페이지")
  public void findUnprocessedReviewReportReport_ok_lastPage() throws IOException {
    // given
    // - 새로운 판매자와 구매자 그리고 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member buyer = MemberBuilder.fullData().build();
    em.persist(buyer);
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // - 새로운 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);
    long givenProductTypeId = type.getId();

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
        purchaseItem.registerReview(review);
        em.persist(review);

        // Review마다 처리되지 않은 신고데이터 생성
        ReviewReport noProcessedReport =
            ReviewReportBuilder.makeNoProcessedReviewReport(reporter, review);
        em.persist(noProcessedReport);

        // Review마다 처리완료된 신고데이터 생성
        ReviewReport processedReport =
            ReviewReportBuilder.makeProcessedReviewReport(reporter, review);
        em.persist(processedReport);
      }

      Purchase purchase =
          PurchaseBuilder.makePurchase(buyer, purchaseItems, PurchaseStateType.COMPLETE);
      em.persist(purchase);
    }
    em.flush();
    em.clear();

    // - 인자세팅
    long inputProductTypeId = givenProductTypeId;
    int inputSliceNum = 1;
    int inputSliceSize = 10;
    PageRequest inputPageRequest =
        PageRequest.of(inputSliceNum, inputSliceSize, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<ReviewReport> sliceResult =
        target.findUnprocessedReviewReportReport(inputProductTypeId, inputPageRequest);

    // target
    // - 페이지 검증
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    List<ReviewReport> resultReviewReport = sliceResult.getContent();
    assertEquals(5, resultReviewReport.size());

    // - fetch 로딩 검증
    resultReviewReport.forEach(
        reviewReport -> {
          assertTrue(emUtil.isLoaded(reviewReport, "reporter"));
          assertTrue(emUtil.isLoaded(reviewReport, "review"));
          assertTrue(emUtil.isLoaded(reviewReport.getReview().getWriter(), "writer"));
        });

    // - where 조건 검증
    resultReviewReport.forEach(
        reviewReport -> {
          assertEquals(
              givenProductTypeId, reviewReport.getReview().getProduct().getProductType().getId());
        });
    resultReviewReport.forEach(
        reviewReport -> {
          assertFalse(reviewReport.isProcessedComplete());
        });
  }

  @Test
  @DisplayName("findReviewReportsByReviewWriter() : 정상흐름 - 첫번째 페이지")
  public void findReviewReportsByReviewWriter_ok_firstPage() throws IOException {
    // given
    // - 새로운 판매자와 구매자 그리고 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member buyer = MemberBuilder.fullData().build();
    em.persist(buyer);
    long givenReviewWriterId = buyer.getId();
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // - 새로운 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);

    // - 새로운 구매할 제품 생성
    Product targetProduct = ProductBuilder.makeNoBannedProduct(seller, type);
    em.persist(targetProduct);

    List<PurchaseItem> purchaseItems = new ArrayList<>();
    // 10개의 PurchaseItem 생성
    for (int k = 0; k < 10; k++) {
      PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(targetProduct);
      purchaseItems.add(purchaseItem);

      // PurchaseItem마다 Review 생성 (총 10개)
      Review review = ReviewBuilder.makeReview(buyer, targetProduct);
      purchaseItem.registerReview(review);
      em.persist(review);

      // Review마다 처리되지 않은 신고데이터 생성 (총 10개)
      ReviewReport noProcessedReport =
          ReviewReportBuilder.makeNoProcessedReviewReport(reporter, review);
      em.persist(noProcessedReport);

      // Review마다 처리완료된 신고데이터 생성 (총 10개)
      ReviewReport processedReport =
          ReviewReportBuilder.makeProcessedReviewReport(reporter, review);
      em.persist(processedReport);
    }

    // 생성한 PurchaseItem으로 Purchase 생성
    Purchase purchase =
        PurchaseBuilder.makePurchase(buyer, purchaseItems, PurchaseStateType.COMPLETE);
    em.persist(purchase);

    em.flush();
    em.clear();

    // - 인자세팅
    long inputReviewWriterId = givenReviewWriterId;
    int inputSliceNum = 0;
    int inputSliceSize = 15;
    PageRequest inputPageRequest =
        PageRequest.of(inputSliceNum, inputSliceSize, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<ReviewReport> sliceResult =
        target.findReviewReportsByReviewWriter(inputReviewWriterId, inputPageRequest);

    // target
    // - 페이지 검증
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    List<ReviewReport> resultReviewReport = sliceResult.getContent();
    assertEquals(15, resultReviewReport.size());

    // - fetch 로딩 검증
    resultReviewReport.forEach(
        reviewReport -> {
          assertTrue(emUtil.isLoaded(reviewReport, "reporter"));
          assertTrue(emUtil.isLoaded(reviewReport, "review"));
          assertTrue(emUtil.isLoaded(reviewReport.getReview().getWriter(), "writer"));
        });

    // - where 조건 검증
    resultReviewReport.forEach(
        reviewReport -> {
          assertEquals(givenReviewWriterId, reviewReport.getReview().getWriter().getId());
        });
  }

  @Test
  @DisplayName("findReviewReportsByReviewWriter() : 정상흐름 - 마지막 페이지")
  public void findReviewReportsByReviewWriter_ok_lastPage() throws IOException {
    // given
    // - 새로운 판매자와 구매자 그리고 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member buyer = MemberBuilder.fullData().build();
    em.persist(buyer);
    long givenReviewWriterId = buyer.getId();
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // - 새로운 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);

    // - 새로운 구매할 제품 생성
    Product targetProduct = ProductBuilder.makeNoBannedProduct(seller, type);
    em.persist(targetProduct);

    List<PurchaseItem> purchaseItems = new ArrayList<>();
    // 10개의 PurchaseItem 생성
    for (int k = 0; k < 10; k++) {
      PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(targetProduct);
      purchaseItems.add(purchaseItem);

      // PurchaseItem마다 Review 생성 (총 10개)
      Review review = ReviewBuilder.makeReview(buyer, targetProduct);
      purchaseItem.registerReview(review);
      em.persist(review);

      // Review마다 처리되지 않은 신고데이터 생성 (총 10개)
      ReviewReport noProcessedReport =
          ReviewReportBuilder.makeNoProcessedReviewReport(reporter, review);
      em.persist(noProcessedReport);

      // Review마다 처리완료된 신고데이터 생성 (총 10개)
      ReviewReport processedReport =
          ReviewReportBuilder.makeProcessedReviewReport(reporter, review);
      em.persist(processedReport);
    }

    // 생성한 PurchaseItem으로 Purchase 생성
    Purchase purchase =
        PurchaseBuilder.makePurchase(buyer, purchaseItems, PurchaseStateType.COMPLETE);
    em.persist(purchase);

    em.flush();
    em.clear();

    // - 인자세팅
    long inputReviewWriterId = givenReviewWriterId;
    int inputSliceNum = 1;
    int inputSliceSize = 15;
    PageRequest inputPageRequest =
        PageRequest.of(inputSliceNum, inputSliceSize, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<ReviewReport> sliceResult =
        target.findReviewReportsByReviewWriter(inputReviewWriterId, inputPageRequest);

    // target
    // - 페이지 검증
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    List<ReviewReport> resultReviewReport = sliceResult.getContent();
    assertEquals(5, resultReviewReport.size());

    // - fetch 로딩 검증
    resultReviewReport.forEach(
        reviewReport -> {
          assertTrue(emUtil.isLoaded(reviewReport, "reporter"));
          assertTrue(emUtil.isLoaded(reviewReport, "review"));
          assertTrue(emUtil.isLoaded(reviewReport.getReview().getWriter(), "writer"));
        });

    // - where 조건 검증
    resultReviewReport.forEach(
        reviewReport -> {
          assertEquals(givenReviewWriterId, reviewReport.getReview().getWriter().getId());
        });
  }
}
