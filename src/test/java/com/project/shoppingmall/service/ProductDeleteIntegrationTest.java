package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.service.product.ProductDeleteService;
import com.project.shoppingmall.testdata.*;
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.JsonUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@SpringBootTest
@Transactional
@Rollback
public class ProductDeleteIntegrationTest {
  @Autowired private ProductDeleteService target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;
  @Autowired private S3Client s3Client;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  private long targetProductId;

  @BeforeEach
  public void beforeEach() {
    Member seller = testDataMaker.saveMember();
    Member buyer = testDataMaker.saveMember();
    Member other = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    Product targetProduct = testDataMaker.saveProduct(seller, type);
    testDataMaker.saveProductImage(targetProduct);
    testDataMaker.saveProductContent(targetProduct);
    targetProductId = targetProduct.getId();
    ProductReport productReport = testDataMaker.saveProductReport(buyer, targetProduct);
    BasketItem basketItem = testDataMaker.saveBasketItem(buyer, targetProduct);
    PurchaseItem purchaseItem = testDataMaker.savePurchaseItem(targetProduct, buyer);
    Review review = testDataMaker.saveReview(buyer, targetProduct, purchaseItem);
    ReviewReport reviewReport = testDataMaker.saveReviewReport(other, review);

    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("deleteProduct() : 정상흐름")
  public void deleteProduct_ok() {
    Product givenProduct =
        em.createQuery("select p from Product p where p.id = :targetProductId", Product.class)
            .setParameter("targetProductId", targetProductId)
            .getSingleResult();

    // when
    target.deleteProduct(givenProduct);
    em.flush();
    em.clear();

    // then
    checkProductReportDelete(givenProduct);
    checkBasketItemDelete(givenProduct);
    checkReviewDelete(givenProduct);
    checkReviewReportDelete(givenProduct);
    checkPurchaseItemNotDelete(givenProduct);
    checkProductImageDelete(givenProduct);
    checkProductContentImageDelete(givenProduct);
  }

  private void checkReviewDelete(Product givenProduct) {
    String checkReviewResultQuery = "select r From Review r where r.product.id = :productId";
    List<Review> reviewResult =
        em.createQuery(checkReviewResultQuery, Review.class)
            .setParameter("productId", givenProduct.getId())
            .getResultList();
    assertEquals(0, reviewResult.size());
  }

  private void checkProductReportDelete(Product givenProduct) {
    String checkProductReportResultQuery =
        "select pr From ProductReport pr where pr.product.id = :productId";
    List<ProductReport> productReportResult =
        em.createQuery(checkProductReportResultQuery, ProductReport.class)
            .setParameter("productId", givenProduct.getId())
            .getResultList();
    assertEquals(0, productReportResult.size());
  }

  private void checkBasketItemDelete(Product givenProduct) {
    String checkBasketItemResultQuery =
        "select bi From BasketItem bi where bi.product.id = :productId";
    List<BasketItem> basketItemResult =
        em.createQuery(checkBasketItemResultQuery, BasketItem.class)
            .setParameter("productId", givenProduct.getId())
            .getResultList();
    assertEquals(0, basketItemResult.size());
  }

  private void checkReviewReportDelete(Product givenProduct) {
    String checkReviewReportResultQuery =
        "select rr From ReviewReport rr where rr.review.product.id = :productId";
    List<ReviewReport> reviewReportResult =
        em.createQuery(checkReviewReportResultQuery, ReviewReport.class)
            .setParameter("productId", givenProduct.getId())
            .getResultList();
    assertEquals(0, reviewReportResult.size());
  }

  private void checkPurchaseItemNotDelete(Product givenProduct) {
    String checkPurchaseItemResult =
        "select pi From PurchaseItem pi where pi.productId = :prodcutId";
    List<PurchaseItem> purchaseItemResult =
        em.createQuery(checkPurchaseItemResult, PurchaseItem.class)
            .setParameter("prodcutId", givenProduct.getId())
            .getResultList();
    assertEquals(1, purchaseItemResult.size());
  }

  private void checkProductImageDelete(Product givenProduct) {
    givenProduct
        .getProductImages()
        .forEach(
            productImage -> {
              assertThrows(
                  NoSuchKeyException.class,
                  () -> {
                    HeadObjectRequest headObjectRequest =
                        HeadObjectRequest.builder()
                            .bucket(bucketName)
                            .key(productImage.getImageUri())
                            .build();
                    s3Client.headObject(headObjectRequest);
                  });
            });
  }

  private void checkProductContentImageDelete(Product givenProduct) {
    givenProduct
        .getContents()
        .forEach(
            content -> {
              if (content.getType().equals(BlockType.IMAGE_TYPE)) {
                assertThrows(
                    NoSuchKeyException.class,
                    () -> {
                      ImageBlock imageBlock =
                          JsonUtil.convertJsonToObject(content.getContent(), ImageBlock.class);
                      HeadObjectRequest headObjectRequest =
                          HeadObjectRequest.builder()
                              .bucket(bucketName)
                              .key(imageBlock.getImageUri())
                              .build();
                      s3Client.headObject(headObjectRequest);
                    });
              }
            });
  }
}
