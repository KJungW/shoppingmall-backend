package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_dto.SliceManager;
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
  @Autowired private IntegrationTestDataMaker testDataMaker;
  private PersistenceUnitUtil emUtil;

  @BeforeEach
  public void beforeEach() {
    emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
  }

  @Test
  @DisplayName("findAllForSeller() : 정상흐름 - 마지막 페이지")
  public void findAllForSeller_ok_lastPage() {
    // given
    long inputProductId;
    PageRequest inputPageRequest = PageRequest.of(1, 4, Sort.by(Sort.Direction.DESC, "createDate"));

    Member givenSeller = testDataMaker.saveMember();
    ProductType givenType = testDataMaker.saveProductType("test$test");
    Product givenProduct = testDataMaker.saveProduct(givenSeller, givenType);
    Purchase givenPurchase =
        testDataMaker.savePurchase(givenProduct, 6, PurchaseStateType.COMPLETE);

    inputProductId = givenProduct.getId();
    em.flush();
    em.clear();

    // when
    Slice<PurchaseItem> result = target.findAllForSeller(inputProductId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findAllForSeller(result);
    checkWhere_findAllForSeller(inputProductId, result);
  }

  @Test
  @DisplayName("findAllForSellerBetweenDate() : 정상흐름 마지막 페이지 페이지")
  public void findAllForSellerBetweenDate_ok_lastPage() {
    long inputSellerId;
    LocalDateTime inputStartDate;
    LocalDateTime inputEndDate;
    PageRequest inputPageRequest = PageRequest.of(1, 4, Sort.by(Sort.Direction.DESC, "createDate"));

    inputStartDate = LocalDateTime.now();
    Member givenSeller = testDataMaker.saveMember();
    ProductType givenType = testDataMaker.saveProductType("test$test");
    Product givenProduct = testDataMaker.saveProduct(givenSeller, givenType);
    Purchase givenPurchaseInDate =
        testDataMaker.savePurchase(givenProduct, 6, PurchaseStateType.COMPLETE);
    em.flush();
    inputSellerId = givenSeller.getId();
    inputEndDate = LocalDateTime.now();

    Purchase givenPurchaseOutDate =
        testDataMaker.savePurchase(givenProduct, 6, PurchaseStateType.COMPLETE);
    em.flush();
    em.clear();

    // when
    Slice<PurchaseItem> result =
        target.findAllForSellerBetweenDate(
            inputSellerId, inputStartDate, inputEndDate, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findAllForSellerBetweenDate(result);
    checkWhere_findAllForSellerBetweenDate(inputSellerId, inputStartDate, inputEndDate, result);
  }

  @Test
  @DisplayName("findRefundedAllForBuyer() : 정상흐름 마지막 페이지 페이지")
  public void findRefundedAllForBuyer_ok_lastPage() {
    // given
    long inputBuyerId;
    PageRequest inputPageRequest = PageRequest.of(1, 4, Sort.by(Sort.Direction.DESC, "createDate"));

    Product givenProduct = testDataMaker.saveProduct();
    Member givenBuyer = testDataMaker.saveMember();
    Purchase givenPurchase =
        testDataMaker.savePurchase(givenBuyer, givenProduct, 6, PurchaseStateType.COMPLETE);
    List<Refund> givenRefunds =
        testDataMaker.saveRefundList(givenPurchase.getPurchaseItems(), RefundStateType.COMPLETE);

    inputBuyerId = givenBuyer.getId();
    em.flush();
    em.clear();

    // when
    Slice<PurchaseItem> result = target.findRefundedAllForBuyer(inputBuyerId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findRefundedAllForBuyer(result);
    checkWhere_findRefundedAllForBuyer(inputBuyerId, result);
  }

  @Test
  @DisplayName("findRefundedAllForSeller() : 정상흐름 마지막 페이지")
  public void findRefundedAllForSeller_ok_lastPage() {
    // given
    long inputSellerId;
    PageRequest inputPageRequest =
        PageRequest.of(1, 4, Sort.by(Sort.Direction.DESC, "finalRefundCreatedDate"));

    Member givenSeller = testDataMaker.saveMember();
    ProductType givenType = testDataMaker.saveProductType("test$test");
    Product givenProduct = testDataMaker.saveProduct(givenSeller, givenType);
    Purchase givenPurchase =
        testDataMaker.savePurchase(givenProduct, 6, PurchaseStateType.COMPLETE);
    List<Refund> givenRefunds =
        testDataMaker.saveRefundList(givenPurchase.getPurchaseItems(), RefundStateType.COMPLETE);

    inputSellerId = givenSeller.getId();
    em.flush();
    em.clear();

    // when
    Slice<PurchaseItem> result = target.findRefundedAllForSeller(inputSellerId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findRefundedAllForSeller(result);
    checkWhere_findRefundedAllForSeller(inputSellerId, result);
  }

  public void checkFetchLoad_findAllForSeller(Slice<PurchaseItem> target) {
    target
        .getContent()
        .forEach(
            purchaseItem -> {
              assertTrue(emUtil.isLoaded(purchaseItem, "purchase"));
            });
  }

  public void checkWhere_findAllForSeller(long inputProductId, Slice<PurchaseItem> target) {
    target
        .getContent()
        .forEach(
            item -> {
              assertEquals(inputProductId, item.getProductId());
              assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
            });
  }

  public void checkFetchLoad_findAllForSellerBetweenDate(Slice<PurchaseItem> target) {
    target
        .getContent()
        .forEach(
            purchaseItem -> {
              assertTrue(emUtil.isLoaded(purchaseItem, "purchase"));
            });
  }

  public void checkWhere_findAllForSellerBetweenDate(
      long sellerId, LocalDateTime startDate, LocalDateTime endDate, Slice<PurchaseItem> target) {
    target
        .getContent()
        .forEach(
            item -> {
              assertEquals(sellerId, item.getSellerId());
              assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
              assertTrue(item.getCreateDate().isAfter(startDate));
              assertTrue(item.getCreateDate().isBefore(endDate));
            });
  }

  public void checkFetchLoad_findRefundedAllForBuyer(Slice<PurchaseItem> target) {
    target
        .getContent()
        .forEach(
            purchaseItem -> {
              assertTrue(emUtil.isLoaded(purchaseItem, "purchase"));
            });
  }

  public void checkWhere_findRefundedAllForBuyer(long buyerId, Slice<PurchaseItem> target) {
    target
        .getContent()
        .forEach(
            item -> {
              assertFalse(item.getRefunds().isEmpty());
              assertEquals(buyerId, item.getPurchase().getBuyerId());
              assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
            });
  }

  public void checkFetchLoad_findRefundedAllForSeller(Slice<PurchaseItem> target) {
    target
        .getContent()
        .forEach(
            purchaseItem -> {
              assertTrue(emUtil.isLoaded(purchaseItem, "purchase"));
            });
  }

  public void checkWhere_findRefundedAllForSeller(long sellerId, Slice<PurchaseItem> target) {
    target
        .getContent()
        .forEach(
            item -> {
              assertFalse(item.getRefunds().isEmpty());
              assertEquals(sellerId, item.getSellerId());
              assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
            });
  }
}
