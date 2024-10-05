package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
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
class ProductReportRetrieveRepositoryTest {
  @Autowired private ProductReportRetrieveRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;
  private PersistenceUnitUtil emUtil;

  @BeforeEach
  public void beforeEach() {
    emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
  }

  @Test
  @DisplayName("findUnprocessedProductReport() : 정상흐름 - 첫번째 페이지")
  public void findUnprocessedProductReport_ok_firstPage() {
    // given
    long inputProductTypeId;
    PageRequest inputPageRequest =
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createDate"));

    int givenProductCount = 15;
    Member seller = testDataMaker.saveMember();
    ProductType productType = testDataMaker.saveProductType("test$test");
    List<Product> productList =
        testDataMaker.saveProductList(givenProductCount, seller, productType, false);

    Member reporter = testDataMaker.saveMember();
    List<ProductReport> notProcessedReportList =
        makeProductReportByProduct(productList, reporter, ReportResultType.WAITING_PROCESSED);
    List<ProductReport> processedReportList =
        makeProductReportByProduct(productList, reporter, ReportResultType.NO_ACTION);

    inputProductTypeId = productType.getId();
    em.flush();
    em.clear();

    // when
    Slice<ProductReport> result =
        target.findUnprocessedProductReport(inputProductTypeId, inputPageRequest);

    // target
    SliceManager.checkOnlyPageData(inputPageRequest, true, false, true, false, result);
    SliceManager.checkContentSize(inputPageRequest.getPageSize(), result);
    checkFetchLoad_findUnprocessedProductReport(result);
    checkWhere_findUnprocessedProductReport(inputProductTypeId, result);
  }

  @Test
  @DisplayName("findUnprocessedProductReport() : 정상흐름 - 마지막 페이지")
  public void findUnprocessedProductReport_ok_lastPage() {
    // given
    long inputProductTypeId;
    PageRequest inputPageRequest =
        PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createDate"));

    int givenProductCount = 15;
    Member seller = testDataMaker.saveMember();
    ProductType productType = testDataMaker.saveProductType("test$test");
    List<Product> productList =
        testDataMaker.saveProductList(givenProductCount, seller, productType, false);

    Member reporter = testDataMaker.saveMember();
    List<ProductReport> notProcessedReportList =
        makeProductReportByProduct(productList, reporter, ReportResultType.WAITING_PROCESSED);
    List<ProductReport> processedReportList =
        makeProductReportByProduct(productList, reporter, ReportResultType.NO_ACTION);

    inputProductTypeId = productType.getId();
    em.flush();
    em.clear();

    // when
    Slice<ProductReport> result =
        target.findUnprocessedProductReport(inputProductTypeId, inputPageRequest);

    // target
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(5, result);
    checkFetchLoad_findUnprocessedProductReport(result);
    checkWhere_findUnprocessedProductReport(inputProductTypeId, result);
  }

  @Test
  @DisplayName("findProductReportsByProductSeller() : 정상흐름 - 첫번째 페이지")
  public void findProductReportsByProductSeller_ok_firstPage() {
    // given
    long inputSellerId;
    PageRequest inputPageRequest =
        PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createDate"));

    int givenProductCount = 10;
    Member seller = testDataMaker.saveMember();
    ProductType productType = testDataMaker.saveProductType("test$test");
    List<Product> productList =
        testDataMaker.saveProductList(givenProductCount, seller, productType, false);

    Member reporter = testDataMaker.saveMember();
    List<ProductReport> notProcessedReportList =
        makeProductReportByProduct(productList, reporter, ReportResultType.WAITING_PROCESSED);
    List<ProductReport> processedReportList =
        makeProductReportByProduct(productList, reporter, ReportResultType.NO_ACTION);

    inputSellerId = seller.getId();
    em.flush();
    em.clear();

    // when
    Slice<ProductReport> result =
        target.findProductReportsByProductSeller(inputSellerId, inputPageRequest);

    // target
    SliceManager.checkOnlyPageData(inputPageRequest, true, false, true, false, result);
    SliceManager.checkContentSize(15, result);
    checkFetchLoad_findProductReportsByProductSeller(result);
    checkWhere_findProductReportsByProductSeller(inputSellerId, result);
  }

  @Test
  @DisplayName("findProductReportsByProductSeller() : 정상흐름 - 마지막 페이지")
  public void findProductReportsByProductSeller_ok_lastPage() {
    // given
    long inputSellerId;
    PageRequest inputPageRequest =
        PageRequest.of(1, 15, Sort.by(Sort.Direction.DESC, "createDate"));

    int givenProductCount = 10;
    Member seller = testDataMaker.saveMember();
    ProductType productType = testDataMaker.saveProductType("test$test");
    List<Product> productList =
        testDataMaker.saveProductList(givenProductCount, seller, productType, false);

    Member reporter = testDataMaker.saveMember();
    List<ProductReport> notProcessedReportList =
        makeProductReportByProduct(productList, reporter, ReportResultType.WAITING_PROCESSED);
    List<ProductReport> processedReportList =
        makeProductReportByProduct(productList, reporter, ReportResultType.NO_ACTION);

    inputSellerId = seller.getId();
    em.flush();
    em.clear();

    // when
    Slice<ProductReport> result =
        target.findProductReportsByProductSeller(inputSellerId, inputPageRequest);

    // target
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(5, result);
    checkFetchLoad_findProductReportsByProductSeller(result);
    checkWhere_findProductReportsByProductSeller(inputSellerId, result);
  }

  public List<ProductReport> makeProductReportByProduct(
      List<Product> productList, Member reporter, ReportResultType state) {
    return productList.stream()
        .map(product -> testDataMaker.saveProductReport(reporter, product, state))
        .toList();
  }

  public void checkFetchLoad_findUnprocessedProductReport(Slice<ProductReport> target) {
    target
        .getContent()
        .forEach(
            productReport -> {
              assertTrue(emUtil.isLoaded(productReport, "reporter"));
              assertTrue(emUtil.isLoaded(productReport, "product"));
              assertTrue(emUtil.isLoaded(productReport.getProduct(), "seller"));
              assertTrue(emUtil.isLoaded(productReport.getProduct(), "productType"));
            });
  }

  public void checkWhere_findUnprocessedProductReport(
      long productTypeId, Slice<ProductReport> target) {
    target
        .getContent()
        .forEach(
            productReport -> {
              assertEquals(productTypeId, productReport.getProduct().getProductType().getId());
              assertFalse(productReport.getIsProcessedComplete());
            });
  }

  public void checkFetchLoad_findProductReportsByProductSeller(Slice<ProductReport> target) {
    // - fetch 로딩 검증
    target
        .getContent()
        .forEach(
            productReport -> {
              assertTrue(emUtil.isLoaded(productReport, "reporter"));
              assertTrue(emUtil.isLoaded(productReport, "product"));
              assertTrue(emUtil.isLoaded(productReport.getProduct(), "seller"));
              assertTrue(emUtil.isLoaded(productReport.getProduct(), "productType"));
            });
  }

  public void checkWhere_findProductReportsByProductSeller(
      long sellerId, Slice<ProductReport> target) {
    target
        .getContent()
        .forEach(
            productReport -> {
              assertEquals(sellerId, productReport.getProduct().getSeller().getId());
            });
  }
}
