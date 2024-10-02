package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.alarm.Alarm_RealDataBuilder;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.testdata.product.Product_RealDataBuilder;
import com.project.shoppingmall.testdata.purchase.Purchase_RealDataBuilder;
import com.project.shoppingmall.testdata.purchaseitem.PurchaseItem_RealDataBuilder;
import com.project.shoppingmall.testdata.refund.Refund_RealDataBuilder;
import com.project.shoppingmall.testdata.review.Review_RealDataBuilder;
import com.project.shoppingmall.type.PurchaseStateType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
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
  public void retrieveAllByMember_ok() {
    // given
    Member listener = makeMember();
    Product listenerProduct = makeProduct(listener);
    Review listenerReview = makeReview(listener);
    Refund listenerTargerRefund = makeRefund(listenerProduct);
    for (int i = 0; i < 3; i++) {
      em.persist(Alarm_RealDataBuilder.makeMemberBanAlarm(listener));
      em.persist(Alarm_RealDataBuilder.makeReviewBanAlarm(listener, listenerReview));
      em.persist(Alarm_RealDataBuilder.makeProductBanAlarm(listener, listenerProduct));
      em.persist(Alarm_RealDataBuilder.makeRefundRequestAlarm(listener, listenerTargerRefund));
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

  public Product makeProduct(Member member) {
    ProductType type = makeProductType("test$test");
    Product listenerProduct = Product_RealDataBuilder.makeProduct(member, type);
    em.persist(listenerProduct);
    return listenerProduct;
  }

  public Review makeReview(Member reviewWriter) {
    Member otherSeller = makeMember();
    Product otherSellerProduct = makeProduct(otherSeller);
    PurchaseItem purchaseItem = PurchaseItem_RealDataBuilder.makePurchaseItem(otherSellerProduct);
    Purchase purchase =
        Purchase_RealDataBuilder.makePurchase(
            reviewWriter, new ArrayList<>(List.of(purchaseItem)), PurchaseStateType.COMPLETE);
    em.persist(purchase);
    Review review = Review_RealDataBuilder.makeReview(reviewWriter, otherSellerProduct);
    purchaseItem.registerReview(review);
    em.persist(review);
    return review;
  }

  public Refund makeRefund(Product targetProduct) {
    Member refundRequester = makeMember();
    PurchaseItem purchaseItem = PurchaseItem_RealDataBuilder.makePurchaseItem(targetProduct);
    Purchase purchase =
        Purchase_RealDataBuilder.makePurchase(
            refundRequester, new ArrayList<>(List.of(purchaseItem)), PurchaseStateType.COMPLETE);
    em.persist(purchase);
    Refund refund = Refund_RealDataBuilder.makeRefund(purchaseItem);
    em.persist(refund);
    return refund;
  }
}
