package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.type.RefundStateType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.time.LocalDateTime;
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
class RefundRepositoryTest {
  @Autowired private RefundRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;
  private PersistenceUnitUtil emUtil;

  @BeforeEach
  public void beforeEach() {
    emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
  }

  @Test
  @DisplayName("findRefundPriceInPeriodBySeller() : 정상흐름")
  public void findRefundPriceInPeriodBySeller_ok() {
    // given
    long inputSellerId;
    LocalDateTime inputStartDate;
    LocalDateTime inputEndDate;

    inputStartDate = LocalDateTime.now();
    Product givenProduct = testDataMaker.saveProduct();
    Purchase givenPurchase =
        testDataMaker.savePurchase(givenProduct, 5, PurchaseStateType.COMPLETE);
    List<Refund> givenRefunds =
        testDataMaker.saveRefundList(givenPurchase.getPurchaseItems(), RefundStateType.COMPLETE);

    em.flush();
    em.clear();
    inputSellerId = givenProduct.getSeller().getId();
    inputEndDate = LocalDateTime.now();

    // when
    Long result =
        target.findRefundPriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    checkResult_findRefundPriceInPeriodBySeller(givenRefunds, result);
  }

  @Test
  @DisplayName("findRefundPriceInPeriodBySeller() : 정상흐름 - Complete 상태의 환불만을 토대로 조회되는 것을 체크")
  public void findRefundPriceInPeriodBySeller_checkRefundStateIsComplete() {
    // given
    long inputSellerId;
    LocalDateTime inputStartDate;
    LocalDateTime inputEndDate;

    inputStartDate = LocalDateTime.now();
    Product givenProduct = testDataMaker.saveProduct();
    List<Refund> completeRefund = makeRefunds(5, givenProduct, RefundStateType.COMPLETE);
    List<Refund> notCompleteRefund = makeRefunds(5, givenProduct, RefundStateType.REJECTED);

    em.flush();
    em.clear();
    inputSellerId = givenProduct.getSeller().getId();
    inputEndDate = LocalDateTime.now();

    // when
    Long result =
        target.findRefundPriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    checkResult_findRefundPriceInPeriodBySeller(completeRefund, result);
  }

  public List<Refund> makeRefunds(int refundCount, Product product, RefundStateType state) {
    Purchase givenPurchase =
        testDataMaker.savePurchase(product, refundCount, PurchaseStateType.COMPLETE);
    return testDataMaker.saveRefundList(givenPurchase.getPurchaseItems(), state);
  }

  public void checkResult_findRefundPriceInPeriodBySeller(
      List<Refund> refundList, long totalRefundPrice) {
    long expectRefundPrice = refundList.stream().mapToLong(Refund::getRefundPrice).sum();
    assertEquals(expectRefundPrice, totalRefundPrice);
  }
}
