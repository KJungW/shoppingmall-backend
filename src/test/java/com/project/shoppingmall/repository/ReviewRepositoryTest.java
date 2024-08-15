package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
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
class ReviewRepositoryTest {
  @Autowired private ReviewRepository target;
  @Autowired private EntityManager em;

  private Long givenBuyerId;
  private Long givenSellerId;
  private Long givenProductId;
  private List<PurchaseItem> givenPurchaseItems;

  @BeforeEach
  public void beforeEach() {
    givenPurchaseItems = new ArrayList<>();

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
    givenSellerId = seller.getId();
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
    givenBuyerId = buyer.getId();

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
    givenProductId = targetProduct.getId();

    // 10개의 Complete상태의 Purchase 데이터 생성
    for (int i = 0; i < 10; i++) {
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
  @DisplayName("calcReviewScoresInProduct : 정상흐름")
  public void calcReviewScoresInProduct_ok() {
    // when
    ReviewScoresCalcResult result = target.calcReviewScoresInProduct(givenProductId);

    // then
    Assertions.assertEquals(30, result.getReviewCount());
    Assertions.assertEquals(2.5, result.getScoreAverage());
  }

  @Test
  @DisplayName("calcReviewScoresInProduct : 리뷰가 하나도 존재하지 않을 경우")
  public void calcReviewScoresInProduct_noReview() {
    // given
    List<Review> allReviewList = givenPurchaseItems.stream().map(PurchaseItem::getReview).toList();
    givenPurchaseItems.forEach(item -> ReflectionTestUtils.setField(item, "review", null));
    allReviewList.forEach(review -> em.remove(review));
    em.flush();

    // when
    ReviewScoresCalcResult result = target.calcReviewScoresInProduct(givenProductId);

    // then
    Assertions.assertEquals(0, result.getReviewCount());
    Assertions.assertEquals(0, result.getScoreAverage());
  }
}
