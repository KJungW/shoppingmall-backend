package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.value.DeliveryInfo;
import com.project.shoppingmall.type.LoginType;
import com.project.shoppingmall.type.MemberRoleType;
import com.project.shoppingmall.type.PurchaseStateType;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class PurchaseItemRetrieveRepositoryTest {
  @Autowired private PurchaseItemRetrieveRepository target;
  @Autowired private EntityManager em;
  private Long givenProductId;
  private Long givenBuyerId;
  private Long givenSellerId;

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
            .scoreAvg(4.0)
            .build();
    em.persist(targetProduct);
    givenProductId = targetProduct.getId();

    // 10개의 Fail 상태의 Purchase 데이터 생성
    for (int i = 0; i < 10; i++) {
      List<PurchaseItem> purchaseItems = new ArrayList<>();
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
      }
      int totalPrice = purchaseItems.stream().mapToInt(PurchaseItem::getFinalPrice).sum();
      DeliveryInfo deliveryInfo =
          new DeliveryInfo(buyer.getNickName(), "test address", "11011", "101-0000-0000");
      Purchase purchase =
          Purchase.builder()
              .buyer(buyer)
              .purchaseItems(purchaseItems)
              .purchaseUid(i + "test-fail-PurchaseUid")
              .purchaseTitle("임시구매" + i)
              .deliveryInfo(deliveryInfo)
              .totalPrice(totalPrice)
              .build();
      purchase.convertStateToFail(i + "test-fail-PaymentUid");
      em.persist(purchase);
    }

    // 10개의 Complete상태의 Purchase 데이터 생성
    for (int i = 0; i < 10; i++) {
      List<PurchaseItem> purchaseItems = new ArrayList<>();
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

      // Purchase마다 생성된 3개의 PurchaseItem 중 첫번째 PurchaseItem에 대해서 reqeuset 상태의 Refund 생성 (총 10개)
      PurchaseItem requestRefundTarget = purchaseItems.get(0);
      Refund requestRefund =
          Refund.builder()
              .refundPrice(requestRefundTarget.getFinalPrice())
              .requestTitle("환불이 필요합니다.")
              .requestContent("환불을 해주시면 감사하겠습니다.")
              .build();
      requestRefundTarget.addRefund(requestRefund);
      em.persist(requestRefund);

      // Purchase마다 생성된 3개의 PurchaseItem 중 두번째 PurchaseItem에 대해서 complete 상태의 Refund 생성 (총 10개)
      PurchaseItem completeRefundTarget = purchaseItems.get(1);
      Refund completeRefund =
          Refund.builder()
              .refundPrice(requestRefundTarget.getFinalPrice())
              .requestTitle("환불이 필요합니다.")
              .requestContent("환불을 해주시면 감사하겠습니다.")
              .build();
      completeRefundTarget.addRefund(completeRefund);
      completeRefund.acceptRefund("환불을 승인합니다. 반품을 진행해주세요");
      completeRefund.completeRefund();
      em.persist(requestRefund);
    }
  }

  @Test
  @DisplayName("findAllForSeller() : 정상흐름 - 첫번째 페이지")
  public void findAllForSeller_ok_firstPage() {
    // given
    PageRequest pageRequest = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<PurchaseItem> sliceResult = target.findAllForSeller(givenProductId, pageRequest);

    // then
    assertTrue(sliceResult.isFirst());
    assertFalse(sliceResult.isLast());

    List<PurchaseItem> resultPurchaseItems = sliceResult.getContent();
    assertEquals(20, resultPurchaseItems.size());
    resultPurchaseItems.forEach(
        purchaseItem -> {
          assertEquals(givenProductId, purchaseItem.getProductId());
        });
    resultPurchaseItems.forEach(
        purchaseItem -> {
          assertEquals(PurchaseStateType.COMPLETE, purchaseItem.getPurchase().getState());
        });
  }

  @Test
  @DisplayName("findAllForSeller() : 정상흐름 - 마지막 페이지")
  public void findAllForSeller_ok_lastPage() {
    // given
    PageRequest pageRequest = PageRequest.of(1, 20, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<PurchaseItem> sliceResult = target.findAllForSeller(givenProductId, pageRequest);

    // then
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());

    List<PurchaseItem> resultPurchaseItems = sliceResult.getContent();
    assertEquals(10, resultPurchaseItems.size());
    resultPurchaseItems.forEach(
        purchaseItem -> {
          assertEquals(givenProductId, purchaseItem.getProductId());
        });
    resultPurchaseItems.forEach(
        purchaseItem -> {
          assertEquals(PurchaseStateType.COMPLETE, purchaseItem.getPurchase().getState());
        });
  }

  @Test
  @DisplayName("findRefundedAllForBuyer() : 정상흐름 첫번째 페이지")
  public void findRefundedAllForBuyer_ok_firstPage() {
    // given
    PageRequest pageRequest = PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<PurchaseItem> sliceData = target.findRefundedAllForBuyer(givenBuyerId, pageRequest);

    // then
    // - 페이지 정보 검증
    assertTrue(sliceData.isFirst());
    assertFalse(sliceData.isLast());
    assertEquals(15, sliceData.getContent().size());

    // - 조회된 PurchaseItem의 구매자 ID 검증
    List<PurchaseItem> purchaseItems = sliceData.getContent();
    purchaseItems.forEach(
        item -> {
          assertEquals(givenBuyerId, item.getPurchase().getBuyer().getId());
        });

    // - 조회된 PurchaseItem의 구매상태가 Complete인지 검증
    purchaseItems.forEach(
        item -> {
          assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
        });

    // - 조회된 모든 PurchaseItem에 Refund 요청이 존재하는지 검증
    purchaseItems.forEach(
        item -> {
          assertFalse(item.getRefunds().isEmpty());
        });
  }

  @Test
  @DisplayName("findRefundedAllForBuyer() : 정상흐름 마지막 페이지 페이지")
  public void findRefundedAllForBuyer_ok_lastPage() {
    // given
    PageRequest pageRequest = PageRequest.of(1, 15, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<PurchaseItem> sliceData = target.findRefundedAllForBuyer(givenBuyerId, pageRequest);

    // then
    // - 페이지 정보 검증
    assertFalse(sliceData.isFirst());
    assertTrue(sliceData.isLast());
    assertEquals(5, sliceData.getContent().size());

    // - 조회된 PurchaseItem의 구매자 ID 검증
    List<PurchaseItem> purchaseItems = sliceData.getContent();
    purchaseItems.forEach(
        item -> {
          assertEquals(givenBuyerId, item.getPurchase().getBuyer().getId());
        });

    // - 조회된 PurchaseItem의 구매상태가 Complete인지 검증
    purchaseItems.forEach(
        item -> {
          assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
        });

    // - 조회된 모든 PurchaseItem에 Refund 요청이 존재하는지 검증
    purchaseItems.forEach(
        item -> {
          assertFalse(item.getRefunds().isEmpty());
        });
  }

  @Test
  @DisplayName("findRefundedAllForSeller() : 정상흐름 첫번째 페이지")
  public void findRefundedAllForSeller_ok_firstPage() {
    // given
    PageRequest pageRequest =
        PageRequest.of(0, 15, Sort.by(Sort.Direction.DESC, "finalRefundCreatedDate"));

    // when
    Slice<PurchaseItem> sliceData = target.findRefundedAllForSeller(givenSellerId, pageRequest);

    // then
    // - 페이지 정보 검증
    assertTrue(sliceData.isFirst());
    assertFalse(sliceData.isLast());
    assertEquals(15, sliceData.getContent().size());

    // - 조회된 PurchaseItem의 판매자 ID 검증
    List<PurchaseItem> purchaseItems = sliceData.getContent();
    purchaseItems.forEach(
        item -> {
          assertEquals(givenSellerId, item.getSellerId());
        });

    // - 조회된 PurchaseItem의 구매상태가 Complete인지 검증
    purchaseItems.forEach(
        item -> {
          assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
        });

    // - 조회된 모든 PurchaseItem에 Refund 요청이 존재하는지 검증
    purchaseItems.forEach(
        item -> {
          assertFalse(item.getRefunds().isEmpty());
        });
  }

  @Test
  @DisplayName("findRefundedAllForSeller() : 정상흐름 마지막 페이지")
  public void findRefundedAllForSeller_ok_lastPage() {
    // given
    PageRequest pageRequest =
        PageRequest.of(1, 15, Sort.by(Sort.Direction.DESC, "finalRefundCreatedDate"));

    // when
    Slice<PurchaseItem> sliceData = target.findRefundedAllForSeller(givenSellerId, pageRequest);

    // then
    // - 페이지 정보 검증
    assertFalse(sliceData.isFirst());
    assertTrue(sliceData.isLast());
    assertEquals(5, sliceData.getContent().size());

    // - 조회된 PurchaseItem의 판매자 ID 검증
    List<PurchaseItem> purchaseItems = sliceData.getContent();
    purchaseItems.forEach(
        item -> {
          assertEquals(givenSellerId, item.getSellerId());
        });

    // - 조회된 PurchaseItem의 구매상태가 Complete인지 검증
    purchaseItems.forEach(
        item -> {
          assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
        });

    // - 조회된 모든 PurchaseItem에 Refund 요청이 존재하는지 검증
    purchaseItems.forEach(
        item -> {
          assertFalse(item.getRefunds().isEmpty());
        });
  }
}
