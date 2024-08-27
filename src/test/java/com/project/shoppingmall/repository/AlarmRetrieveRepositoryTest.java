package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
class AlarmRetrieveRepositoryTest {
  @Autowired private AlarmRetrieveRepository target;
  @Autowired private EntityManager em;

  @Test
  @DisplayName("retrieveAllByMember() : 정상흐름")
  public void retrieveAllByMember_ok() throws IOException {
    // given
    Member listener = makeMember();
    Product listenerProduct = makeProduct(listener);
    Review listenerReview = makeReview(listener);
    Refund listenerTargerRefund = makeRefund(listenerProduct);
    for (int i = 0; i < 3; i++) {
      em.persist(AlamBuilder.memberBanFullData(listener).build());
      em.persist(AlamBuilder.reviewBanFullData(listener, listenerReview).build());
      em.persist(AlamBuilder.productBanFullData(listener, listenerProduct).build());
      em.persist(AlamBuilder.refundRequestFullData(listener, listenerTargerRefund).build());
    }
    em.flush();
    em.clear();

    long inputListenerId = listener.getId();
    int inputSliceNum = 2;
    int inputSliceSize = 5;
    PageRequest inputPageRequest =
        PageRequest.of(inputSliceNum, inputSliceSize, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<Alarm> sliceResult = target.retrieveAllByMember(inputListenerId, inputPageRequest);

    // then
    // - 페이지 검증
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    List<Alarm> resultAlarmList = sliceResult.getContent();
    assertEquals(2, resultAlarmList.size());

    // - fetch 로딩 검증
    PersistenceUnitUtil emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
    resultAlarmList.forEach(
        alarm -> {
          assertTrue(emUtil.isLoaded(alarm, "listener"));
          assertTrue(emUtil.isLoaded(alarm, "targetReview"));
          assertTrue(emUtil.isLoaded(alarm, "targetProduct"));
          assertTrue(emUtil.isLoaded(alarm, "targetRefund"));
        });

    // - where 조건 검증
    resultAlarmList.forEach(
        alarm -> {
          assertEquals(inputListenerId, alarm.getListener().getId());
        });
  }

  public Member makeMember() {
    Member member = MemberBuilder.fullData().build();
    em.persist(member);
    return member;
  }

  public ProductType makeProductType(String typeName) {
    ProductType type = new ProductType(typeName);
    em.persist(type);
    return type;
  }

  public Product makeProduct(Member member) throws IOException {
    ProductType type = makeProductType("test$test");
    Product listenerProduct = ProductBuilder.makeNoBannedProduct(member, type);
    em.persist(listenerProduct);
    return listenerProduct;
  }

  public Review makeReview(Member reviewWriter) throws IOException {
    Member otherSeller = makeMember();
    Product otherSellerProduct = makeProduct(otherSeller);
    PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(otherSellerProduct);
    Purchase purchase =
        PurchaseBuilder.makeCompleteStatePurchase(
            reviewWriter, new ArrayList<>(List.of(purchaseItem)));
    em.persist(purchase);
    Review review = ReviewBuilder.makeReview(reviewWriter, otherSellerProduct);
    purchaseItem.registerReview(review);
    em.persist(review);
    return review;
  }

  public Refund makeRefund(Product targetProduct) throws IOException {
    Member refundRequester = makeMember();
    PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(targetProduct);
    Purchase purchase =
        PurchaseBuilder.makeCompleteStatePurchase(
            refundRequester, new ArrayList<>(List.of(purchaseItem)));
    em.persist(purchase);
    Refund refund = RefundBuilder.makeRefund(purchaseItem);
    em.persist(refund);
    return refund;
  }
}
