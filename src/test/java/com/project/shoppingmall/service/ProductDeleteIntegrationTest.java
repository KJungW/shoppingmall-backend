package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.service.product.ProductDeleteService;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
public class ProductDeleteIntegrationTest {
  @Autowired private ProductDeleteService target;
  @Autowired private EntityManager em;
  private Product givenProduct;
  private List<PurchaseItem> givenPurchaseItems = new ArrayList<>();
  private List<Review> givenReviews = new ArrayList<>();

  @BeforeEach
  public void beforeEach() {
    // 판매자와 구매자 생성
    Member seller =
        Member.builder()
            .loginType(LoginType.NAVER)
            .socialId("testMemberSocialId123123421aaa")
            .nickName("seller")
            .profileImageUrl("init/member_profile_img.png")
            .profileImageDownLoadUrl(
                "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/member_profile_img.png")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    em.persist(seller);
    Member buyer =
        Member.builder()
            .loginType(LoginType.NAVER)
            .socialId("testMemberSocialIdcvv90498s0df")
            .nickName("buyer")
            .profileImageUrl("init/member_profile_img.png")
            .profileImageDownLoadUrl(
                "https://shoppingmall-s3-bucket.s3.ap-northeast-2.amazonaws.com/init/member_profile_img.png")
            .role(MemberRoleType.ROLE_MEMBER)
            .isBan(false)
            .build();
    em.persist(buyer);

    // 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);

    // 구매할 제품 생성
    Product targetProduct =
        Product.builder()
            .seller(seller)
            .productType(type)
            .productImages(new ArrayList<>())
            .contents(new ArrayList<>())
            .singleOptions(new ArrayList<>())
            .multipleOptions(new ArrayList<>())
            .name("test입니다.")
            .price(2000)
            .discountAmount(100)
            .discountRate(10.0)
            .isBan(false)
            .scoreAvg(0.0)
            .build();
    em.persist(targetProduct);
    givenProduct = targetProduct;

    // 5개의 BasketItem 생성
    for (int i = 0; i < 5; i++) {
      BasketItem basketItem =
          BasketItem.builder().member(buyer).product(targetProduct).options("").build();
      em.persist(basketItem);
    }

    // 5개의 ProductReport 생성
    for (int i = 0; i < 5; i++) {
      ProductReport productReport =
          ProductReport.builder()
              .reporter(buyer)
              .title("테스트용 ProductReport 제목입니다.")
              .description("테스트용 ProductReport 설명입니다.")
              .product(targetProduct)
              .build();
      em.persist(productReport);
    }

    // 5개의 Purchase 생성
    for (int i = 0; i < 5; i++) {
      List<PurchaseItem> purchaseItems = new ArrayList<>();
      // Purchase마다 3개의 PurchaseItem 생성
      for (int k = 0; k < 3; k++) {
        ProductDataForPurchase productOptionObj =
            ProductDataForPurchase.builder()
                .productId(targetProduct.getId())
                .sellerId(targetProduct.getSeller().getId())
                .sellerName(targetProduct.getSeller().getNickName())
                .productName(targetProduct.getName())
                .productTypeName(targetProduct.getProductType().getTypeName())
                .price(targetProduct.getPrice())
                .discountAmount(targetProduct.getDiscountAmount())
                .discountRate(targetProduct.getDiscountRate())
                .build();
        PurchaseItem purchaseItem =
            PurchaseItem.builder()
                .productData(productOptionObj)
                .finalPrice(targetProduct.getFinalPrice())
                .build();
        purchaseItems.add(purchaseItem);
        givenPurchaseItems.add(purchaseItem);

        // PurchaseItem마다 Review 생성
        Review review =
            Review.builder()
                .writer(buyer)
                .product(targetProduct)
                .score(i < 5 ? 3 : 2)
                .title("testTitle")
                .reviewImageUri("testImageUri")
                .reviewImageDownloadUrl("testImageUrl")
                .description("testDescription")
                .build();
        purchaseItem.registerReview(review);
        em.persist(review);
        givenReviews.add(review);

        // Review마다 ReviewReport 생성
        ReviewReport reviewReport =
            ReviewReport.builder()
                .reporter(buyer)
                .title("테스트용 ReviewReport 제목 입니다.")
                .description("테스트용 ReviewReport 설명 입니다.")
                .review(review)
                .build();
        em.persist(reviewReport);
      }
      int totalPrice = purchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();
      DeliveryInfo deliveryInfo =
          new DeliveryInfo(buyer.getNickName(), "test address", "11011", "101-0000-0000");
      Purchase purchase =
          Purchase.builder()
              .buyer(buyer)
              .purchaseItems(purchaseItems)
              .purchaseUid(i + "test-complete-PurchaseUid")
              .purchaseTitle("임시구매" + i)
              .deliveryInfo(deliveryInfo)
              .totalPrice(totalPrice)
              .build();
      purchase.convertStateToComplete(i + "test-complete-PaymentUid");
      em.persist(purchase);
    }
  }

  @Test
  @DisplayName("deleteProduct() : 정상흐름")
  public void deleteProduct_ok() {
    // when
    target.deleteProduct(givenProduct);

    // then
    String checkBasketItemResultQuery =
        "select bi From BasketItem bi where bi.product.id = :productId";
    List<BasketItem> basketItemResult =
        em.createQuery(checkBasketItemResultQuery, BasketItem.class)
            .setParameter("productId", givenProduct.getId())
            .getResultList();
    assertEquals(0, basketItemResult.size());

    String checkProductReportResultQuery =
        "select pr From ProductReport pr where pr.product.id = :productId";
    List<ProductReport> productReportResult =
        em.createQuery(checkProductReportResultQuery, ProductReport.class)
            .setParameter("productId", givenProduct.getId())
            .getResultList();
    assertEquals(0, productReportResult.size());

    String checkReviewResultQuery = "select r From Review r where r.product.id = :productId";
    List<Review> reviewResult =
        em.createQuery(checkReviewResultQuery, Review.class)
            .setParameter("productId", givenProduct.getId())
            .getResultList();
    assertEquals(0, reviewResult.size());

    givenReviews.forEach(
        review -> {
          String checkReviewReportResultQuery =
              "select rr From ReviewReport rr where rr.review.id = :reviewId";
          List<ReviewReport> reviewReportResult =
              em.createQuery(checkReviewReportResultQuery, ReviewReport.class)
                  .setParameter("reviewId", review.getId())
                  .getResultList();
          assertEquals(0, reviewReportResult.size());
        });

    givenPurchaseItems.forEach(
        purchaseItem -> {
          String checkPurchaseItemResult =
              "select pi From PurchaseItem pi where pi.id = :purchaseItemId";
          PurchaseItem purchaseItemResult =
              em.createQuery(checkPurchaseItemResult, PurchaseItem.class)
                  .setParameter("purchaseItemId", purchaseItem.getId())
                  .getSingleResult();
          assertNull(purchaseItemResult.getReview());
        });
  }
}
