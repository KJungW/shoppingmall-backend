package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.testdata.product.Product_RealDataBuilder;
import com.project.shoppingmall.testdata.report.ProductReport_RealDataBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.io.IOException;
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
  private PersistenceUnitUtil emUtil;

  @BeforeEach
  public void beforeEach() throws IOException {
    emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();

    // - 새로운 판매자와 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);

    // 30개의 제품 생성
    for (int i = 0; i < 30; i++) {
      Product targetProduct = Product_RealDataBuilder.makeProduct(seller, type);
      em.persist(targetProduct);

      // 제품마다 처리되지 않은 신고데이터 작성
      ProductReport noProcessedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, false);
      em.persist(noProcessedReport);

      // 제품마다 처리된 신고데이터 작성
      ProductReport processedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, true);
      em.persist(processedReport);
    }
  }

  @Test
  @DisplayName("findUnprocessedProductReport() : 정상흐름 - 첫번째 페이지")
  public void findUnprocessedProductReport_ok_firstPage() throws IOException {
    // given
    // - 새로운 판매자와 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // - 새로운 제품 타입 생성
    String givenProductTypeName = "test$test2";
    ProductType type = new ProductType(givenProductTypeName);
    em.persist(type);
    long givenProductTypeId = type.getId();

    // - 새로운 15개의 제품 생성
    for (int i = 0; i < 15; i++) {
      Product targetProduct = Product_RealDataBuilder.makeProduct(seller, type);
      em.persist(targetProduct);

      // 제품마다 처리되지 않은 신고데이터 작성
      ProductReport noProcessedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, false);
      em.persist(noProcessedReport);

      // 제품마다 처리된 신고데이터 작성
      ProductReport processedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, true);
      em.persist(processedReport);
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
    Slice<ProductReport> sliceResult =
        target.findUnprocessedProductReport(inputProductTypeId, inputPageRequest);

    // target
    // - 페이지 검증
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    List<ProductReport> resultProductReport = sliceResult.getContent();
    assertEquals(10, resultProductReport.size());

    // - fetch 로딩 검증
    resultProductReport.forEach(
        productReport -> {
          assertTrue(emUtil.isLoaded(productReport, "reporter"));
          assertTrue(emUtil.isLoaded(productReport, "product"));
          assertTrue(emUtil.isLoaded(productReport.getProduct(), "seller"));
          assertTrue(emUtil.isLoaded(productReport.getProduct(), "productType"));
        });

    // - where 조건 검증
    resultProductReport.forEach(
        productReport -> {
          assertEquals(givenProductTypeId, productReport.getProduct().getProductType().getId());
        });
    resultProductReport.forEach(
        productReport -> {
          assertFalse(productReport.getIsProcessedComplete());
        });
  }

  @Test
  @DisplayName("findUnprocessedProductReport() : 정상흐름 - 마지막 페이지")
  public void findUnprocessedProductReport_ok_lastPage() throws IOException {
    // given
    // - 새로운 판매자와 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // - 새로운 제품 타입 생성
    String givenProductTypeName = "test$test2";
    ProductType type = new ProductType(givenProductTypeName);
    em.persist(type);
    long givenProductTypeId = type.getId();

    // - 새로운 15개의 제품 생성
    for (int i = 0; i < 15; i++) {
      Product targetProduct = Product_RealDataBuilder.makeProduct(seller, type);
      em.persist(targetProduct);

      // 제품마다 처리되지 않은 신고데이터 작성
      ProductReport noProcessedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, false);
      em.persist(noProcessedReport);

      // 제품마다 처리된 신고데이터 작성
      ProductReport processedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, true);
      em.persist(processedReport);
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
    Slice<ProductReport> sliceResult =
        target.findUnprocessedProductReport(inputProductTypeId, inputPageRequest);

    // target
    // - 페이지 검증
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    List<ProductReport> resultProductReport = sliceResult.getContent();
    assertEquals(5, resultProductReport.size());

    // - fetch 로딩 검증
    resultProductReport.forEach(
        productReport -> {
          assertTrue(emUtil.isLoaded(productReport, "reporter"));
          assertTrue(emUtil.isLoaded(productReport, "product"));
          assertTrue(emUtil.isLoaded(productReport.getProduct(), "seller"));
          assertTrue(emUtil.isLoaded(productReport.getProduct(), "productType"));
        });

    // - where 조건 검증
    resultProductReport.forEach(
        productReport -> {
          assertEquals(givenProductTypeId, productReport.getProduct().getProductType().getId());
        });
    resultProductReport.forEach(
        productReport -> {
          assertFalse(productReport.getIsProcessedComplete());
        });
  }

  @Test
  @DisplayName("findProductReportsByProductSeller() : 정상흐름 - 첫번째 페이지")
  public void findProductReportsByProductSeller_ok_firstPage() throws IOException {
    // given
    // - 새로운 판매자와 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    long givenProductSellerId = seller.getId();
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // - 새로운 제품 타입 생성
    String givenProductTypeName = "test$test2";
    ProductType type = new ProductType(givenProductTypeName);
    em.persist(type);

    // - 새로운 10개의 제품 생성
    for (int i = 0; i < 10; i++) {
      Product targetProduct = Product_RealDataBuilder.makeProduct(seller, type);
      em.persist(targetProduct);

      // 제품마다 처리되지 않은 신고데이터 작성 (총 10개)
      ProductReport noProcessedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, false);
      em.persist(noProcessedReport);

      // 제품마다 처리된 신고데이터 작성 (총 10개)
      ProductReport processedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, true);
      em.persist(processedReport);
    }
    em.flush();
    em.clear();

    // - 인자세팅
    long inputProductSellerId = givenProductSellerId;
    int inputSliceNum = 0;
    int inputSliceSize = 15;
    PageRequest inputPageRequest =
        PageRequest.of(inputSliceNum, inputSliceSize, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<ProductReport> sliceResult =
        target.findProductReportsByProductSeller(inputProductSellerId, inputPageRequest);

    // target
    // - 페이지 검증
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());
    List<ProductReport> resultProductReport = sliceResult.getContent();
    assertEquals(15, resultProductReport.size());

    // - fetch 로딩 검증
    resultProductReport.forEach(
        productReport -> {
          assertTrue(emUtil.isLoaded(productReport, "reporter"));
          assertTrue(emUtil.isLoaded(productReport, "product"));
          assertTrue(emUtil.isLoaded(productReport.getProduct(), "seller"));
          assertTrue(emUtil.isLoaded(productReport.getProduct(), "productType"));
        });

    // - where 조건 검증
    resultProductReport.forEach(
        productReport -> {
          assertEquals(givenProductSellerId, productReport.getProduct().getSeller().getId());
        });
  }

  @Test
  @DisplayName("findProductReportsByProductSeller() : 정상흐름 - 마지막 페이지")
  public void findProductReportsByProductSeller_ok_lastPage() throws IOException {
    // given
    // - 새로운 판매자와 신고자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    long givenProductSellerId = seller.getId();
    Member reporter = MemberBuilder.fullData().build();
    em.persist(reporter);

    // - 새로운 제품 타입 생성
    String givenProductTypeName = "test$test2";
    ProductType type = new ProductType(givenProductTypeName);
    em.persist(type);

    // - 새로운 10개의 제품 생성
    for (int i = 0; i < 10; i++) {
      Product targetProduct = Product_RealDataBuilder.makeProduct(seller, type);
      em.persist(targetProduct);

      // 제품마다 처리되지 않은 신고데이터 작성 (총 10개)
      ProductReport noProcessedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, false);
      em.persist(noProcessedReport);

      // 제품마다 처리된 신고데이터 작성 (총 10개)
      ProductReport processedReport =
          ProductReport_RealDataBuilder.makeProductReport(reporter, targetProduct, true);
      em.persist(processedReport);
    }
    em.flush();
    em.clear();

    // - 인자세팅
    long inputProductSellerId = givenProductSellerId;
    int inputSliceNum = 1;
    int inputSliceSize = 15;
    PageRequest inputPageRequest =
        PageRequest.of(inputSliceNum, inputSliceSize, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<ProductReport> sliceResult =
        target.findProductReportsByProductSeller(inputProductSellerId, inputPageRequest);

    // target
    // - 페이지 검증
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    List<ProductReport> resultProductReport = sliceResult.getContent();
    assertEquals(5, resultProductReport.size());

    // - fetch 로딩 검증
    resultProductReport.forEach(
        productReport -> {
          assertTrue(emUtil.isLoaded(productReport, "reporter"));
          assertTrue(emUtil.isLoaded(productReport, "product"));
          assertTrue(emUtil.isLoaded(productReport.getProduct(), "seller"));
          assertTrue(emUtil.isLoaded(productReport.getProduct(), "productType"));
        });

    // - where 조건 검증
    resultProductReport.forEach(
        productReport -> {
          assertEquals(givenProductSellerId, productReport.getProduct().getSeller().getId());
        });
  }
}
