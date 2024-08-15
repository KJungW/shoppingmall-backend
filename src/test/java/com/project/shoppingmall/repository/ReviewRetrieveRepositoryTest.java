package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.value.DeliveryInfo;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class ReviewRetrieveRepositoryTest {
  @Autowired private ReviewRetrieveRepository target;
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

        // PurchaseItem마다 Review 생성 (일발리뷰: 20, 벤처리리뷰: 10)
        Review review =
            Review.builder()
                .writer(buyer)
                .product(targetProduct)
                .score(3)
                .title("testTitle")
                .reviewImageUri("testImageUri")
                .reviewImageDownloadUrl("testImageUrl")
                .description("testDescription")
                .build();
        if (k == 2) ReflectionTestUtils.setField(review, "isBan", true);
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
  @DisplayName("findAllByProduct : 정상흐름 첫번째 페이지")
  public void findAllByProduct_ok_firstPage() {
    // given
    PageRequest pageRequest = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createDate"));
    // when
    Slice<Review> sliceResult = target.findAllByProduct(givenProductId, pageRequest);

    // then
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());

    List<Review> reviewList = sliceResult.getContent();
    assertEquals(15, reviewList.size());
    reviewList.forEach(review -> assertEquals(givenProductId, review.getProduct().getId()));
    reviewList.forEach(review -> assertFalse(review.getIsBan()));
  }

  @Test
  @DisplayName("findAllByProduct : 정상흐름 마지막 페이지")
  public void findAllByProduct_ok_LastPage() {
    // given
    PageRequest pageRequest = PageRequest.of(1, 15, Sort.by(Sort.Direction.DESC, "createDate"));
    // when
    Slice<Review> sliceResult = target.findAllByProduct(givenProductId, pageRequest);

    // then
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());

    List<Review> reviewList = sliceResult.getContent();
    assertEquals(5, reviewList.size());
    reviewList.forEach(review -> assertEquals(givenProductId, review.getProduct().getId()));
    reviewList.forEach(review -> assertFalse(review.getIsBan()));
  }
}
