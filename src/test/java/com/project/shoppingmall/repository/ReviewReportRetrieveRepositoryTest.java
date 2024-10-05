package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.type.ReportResultType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
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
  @Autowired private IntegrationTestDataMaker testDataMaker;
  private PersistenceUnitUtil emUtil;

  @BeforeEach
  public void beforeEach() {
    emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
  }

  @Test
  @DisplayName("findUnprocessedReviewReportReport() : 정상흐름 - 첫번째 페이지")
  public void findUnprocessedReviewReportReport_ok_firstPage() {
    // given
    long inputProductTypeId;
    PageRequest inputPageRequest = PageRequest.of(1, 4, Sort.by(Sort.Direction.DESC, "createDate"));

    Product product = testDataMaker.saveProduct();
    Purchase purchase = testDataMaker.savePurchase(product, 6, PurchaseStateType.COMPLETE);
    List<Review> reviewList = testDataMaker.saveReviewList(product, purchase.getPurchaseItems());
    List<ReviewReport> notProcessedReviewReports =
        testDataMaker.saveReviewReportList(reviewList, ReportResultType.WAITING_PROCESSED);
    List<ReviewReport> processedReviewReports =
        testDataMaker.saveReviewReportList(reviewList, ReportResultType.NO_ACTION);

    inputProductTypeId = product.getProductType().getId();

    em.flush();
    em.clear();

    // when
    Slice<ReviewReport> result =
        target.findUnprocessedReviewReportReport(inputProductTypeId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findUnprocessedReviewReportReport(result);
    checkWhere_findUnprocessedReviewReportReport(inputProductTypeId, result);
  }

  @Test
  @DisplayName("findReviewReportsByReviewWriter() : 정상흐름 - 첫번째 페이지")
  public void findReviewReportsByReviewWriter_ok_firstPage() {
    // given
    long inputReviewWriterId;
    PageRequest inputPageRequest = PageRequest.of(1, 6, Sort.by(Sort.Direction.DESC, "createDate"));

    Product product = testDataMaker.saveProduct();
    Purchase purchase = testDataMaker.savePurchase(product, 5, PurchaseStateType.COMPLETE);
    Member writer = testDataMaker.saveMember();
    List<Review> reviewList =
        testDataMaker.saveReviewList(writer, product, purchase.getPurchaseItems());
    List<ReviewReport> notProcessedReviewReports =
        testDataMaker.saveReviewReportList(reviewList, ReportResultType.WAITING_PROCESSED);
    List<ReviewReport> processedReviewReports =
        testDataMaker.saveReviewReportList(reviewList, ReportResultType.NO_ACTION);

    inputReviewWriterId = writer.getId();

    em.flush();
    em.clear();

    // when
    Slice<ReviewReport> result =
        target.findReviewReportsByReviewWriter(inputReviewWriterId, inputPageRequest);

    // target
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(4, result);
    checkFetchLoad_findReviewReportsByReviewWriter(result);
    checkWhere_findReviewReportsByReviewWriter(inputReviewWriterId, result);
  }

  public void checkFetchLoad_findUnprocessedReviewReportReport(Slice<ReviewReport> target) {
    target
        .getContent()
        .forEach(
            reviewReport -> {
              assertTrue(emUtil.isLoaded(reviewReport, "reporter"));
              assertTrue(emUtil.isLoaded(reviewReport, "review"));
              assertTrue(emUtil.isLoaded(reviewReport.getReview(), "writer"));
            });
  }

  public void checkWhere_findUnprocessedReviewReportReport(
      long productTypeId, Slice<ReviewReport> target) {
    target
        .getContent()
        .forEach(
            reviewReport -> {
              assertEquals(
                  productTypeId, reviewReport.getReview().getProduct().getProductType().getId());
              assertFalse(reviewReport.getIsProcessedComplete());
            });
  }

  public void checkFetchLoad_findReviewReportsByReviewWriter(Slice<ReviewReport> target) {
    target
        .getContent()
        .forEach(
            reviewReport -> {
              assertTrue(emUtil.isLoaded(reviewReport, "reporter"));
              assertTrue(emUtil.isLoaded(reviewReport, "review"));
              assertTrue(emUtil.isLoaded(reviewReport.getReview().getWriter(), "writer"));
            });
  }

  public void checkWhere_findReviewReportsByReviewWriter(
      long reviewWriterId, Slice<ReviewReport> target) {
    target
        .getContent()
        .forEach(
            reviewReport -> {
              assertEquals(reviewWriterId, reviewReport.getReview().getWriter().getId());
            });
  }
}
