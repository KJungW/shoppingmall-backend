package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import com.project.shoppingmall.type.PurchaseStateType;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class PurchaseItemRepositoryTest {
  @Autowired private PurchaseItemRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;

  @Test
  @DisplayName("findSalesRevenuePriceInMonthBySeller() : 정상흐름")
  public void findSalesRevenuePriceInMonthBySeller_ok() {
    // given
    long inputSellerId;
    LocalDateTime inputStartDate;
    LocalDateTime inputEndDate;

    Member givenSeller = testDataMaker.saveMember();
    ProductType givenType = testDataMaker.saveProductType("test$test");
    Product givenProduct = testDataMaker.saveProduct(givenSeller, givenType);
    em.flush();

    inputSellerId = givenSeller.getId();
    inputStartDate = LocalDateTime.now();
    Purchase completePurchase = makeCompletePurchase(5, givenProduct);
    em.flush();
    em.clear();
    inputEndDate = LocalDateTime.now();

    // when
    Long resultRevenue =
        target.findSalesRevenuePriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    checkRevenue(completePurchase, resultRevenue);
  }

  @Test
  @DisplayName("findSalesRevenuePriceInMonthBySeller() : 정상흐름 - 현재 판매자의 판매기록만을 토대로 조회되는 것을 체크")
  public void findSalesRevenuePriceInMonthBySeller_checkOtherSellerExclude() {
    // given
    long inputSellerId;
    LocalDateTime inputStartDate;
    LocalDateTime inputEndDate;

    Member givenSeller = testDataMaker.saveMember();
    Member givenOtherSeller = testDataMaker.saveMember();
    ProductType givenType = testDataMaker.saveProductType("test$test");
    Product givenProduct = testDataMaker.saveProduct(givenOtherSeller, givenType);
    em.flush();

    inputSellerId = givenSeller.getId();
    inputStartDate = LocalDateTime.now();
    Purchase completePurchase = makeCompletePurchase(5, givenProduct);
    em.flush();
    em.clear();
    inputEndDate = LocalDateTime.now();

    // when
    Long resultRevenue =
        target.findSalesRevenuePriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    checkRevenueIsEmpty(resultRevenue);
  }

  @Test
  @DisplayName("findSalesRevenuePriceInMonthBySeller() : 정상흐름 - 완료된 구매만을 토대로 조회되는 것을 체크")
  public void findSalesRevenuePriceInMonthBySeller_checkPurchaseSateIsComplete() {
    // given
    long inputSellerId;
    LocalDateTime inputStartDate;
    LocalDateTime inputEndDate;

    Member givenSeller = testDataMaker.saveMember();
    ProductType givenType = testDataMaker.saveProductType("test$test");
    Product givenProduct = testDataMaker.saveProduct(givenSeller, givenType);
    em.flush();

    inputSellerId = givenSeller.getId();
    inputStartDate = LocalDateTime.now();
    Purchase notCompletePurchase = makeNotCompletePurchase(5, givenProduct);
    Purchase completePurchase = makeCompletePurchase(5, givenProduct);
    em.flush();
    em.clear();
    inputEndDate = LocalDateTime.now();

    // when
    Long resultRevenue =
        target.findSalesRevenuePriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    checkRevenue(completePurchase, resultRevenue);
  }

  @Test
  @DisplayName("findSalesRevenuePriceInMonthBySeller() : 정상흐름 - 시간대에 맞지 않는 것은 조회되지 않는 것을 체크")
  public void findSalesRevenuePriceInMonthBySeller_checkPurchaseInDate() {
    // given
    long inputSellerId;
    LocalDateTime inputStartDate;
    LocalDateTime inputEndDate;

    inputStartDate = LocalDateTime.now();
    Member givenSeller = testDataMaker.saveMember();
    ProductType givenType = testDataMaker.saveProductType("test$test");
    Product givenProduct = testDataMaker.saveProduct(givenSeller, givenType);
    inputEndDate = LocalDateTime.now();
    em.flush();

    inputSellerId = givenSeller.getId();
    Purchase completePurchase = makeCompletePurchase(5, givenProduct);
    em.flush();
    em.clear();

    // when
    Long resultRevenue =
        target.findSalesRevenuePriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    checkRevenueIsEmpty(resultRevenue);
  }

  public Purchase makeCompletePurchase(int purchaseItemCount, Product product) {
    return testDataMaker.savePurchase(product, purchaseItemCount, PurchaseStateType.COMPLETE);
  }

  public Purchase makeNotCompletePurchase(int purchaseItemCount, Product product) {
    return testDataMaker.savePurchase(product, purchaseItemCount, PurchaseStateType.READY);
  }

  public void checkRevenue(Purchase purchase, long revenue) {
    long expectedRevenuePrice =
        purchase.getPurchaseItems().stream().mapToLong(PurchaseItem::getFinalPrice).sum();
    assertEquals(expectedRevenuePrice, revenue);
  }

  public void checkRevenueIsEmpty(Long revenue) {
    assertNull(revenue);
  }
}
