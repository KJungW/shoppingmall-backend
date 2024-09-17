package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.*;
import com.project.shoppingmall.type.PurchaseStateType;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
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
class PurchaseItemRetrieveRepositoryTest {
  @Autowired private PurchaseItemRetrieveRepository target;
  @Autowired private EntityManager em;

  @Test
  @DisplayName("findAllForSeller() : 정상흐름 - 마지막 페이지")
  public void findAllForSeller_ok_lastPage() {
    // given
    Member givenSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    savePurchaseItems(10, givenBuyer, givenProduct);
    em.flush();
    em.clear();

    Long inputProductId = givenProduct.getId();
    PageRequest inputPageRequest = PageRequest.of(1, 6, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<PurchaseItem> sliceResult = target.findAllForSeller(inputProductId, inputPageRequest);

    // then
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    List<PurchaseItem> resultPurchaseItems = sliceResult.getContent();
    assertEquals(4, resultPurchaseItems.size());
    resultPurchaseItems.forEach(
        purchaseItem -> {
          assertEquals(inputProductId, purchaseItem.getProductId());
          assertEquals(PurchaseStateType.COMPLETE, purchaseItem.getPurchase().getState());
        });
  }

  @Test
  @DisplayName("findAllForSellerBetweenDate() : 정상흐름 마지막 페이지 페이지")
  public void findAllForSellerBetweenDate_ok_lastPage() throws InterruptedException {
    // given
    Member givenSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    LocalDateTime inputStartDate = LocalDateTime.now();
    savePurchaseItems(10, givenBuyer, givenProduct);
    LocalDateTime inputEndDate = LocalDateTime.now();
    Thread.sleep(1000L);
    savePurchaseItems(10, givenBuyer, givenProduct);
    em.flush();
    em.clear();

    long inputSellerId = givenSeller.getId();
    PageRequest inputPageRequest = PageRequest.of(1, 6, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<PurchaseItem> sliceResult =
        target.findAllForSellerBetweenDate(
            inputSellerId, inputStartDate, inputEndDate, inputPageRequest);

    // then
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    List<PurchaseItem> resultPurchaseItems = sliceResult.getContent();
    assertEquals(4, resultPurchaseItems.size());
    resultPurchaseItems.forEach(
        item -> {
          assertEquals(inputSellerId, item.getSellerId());
          assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
          assertTrue(item.getCreateDate().isAfter(inputStartDate));
          assertTrue(item.getCreateDate().isBefore(inputEndDate));
        });
  }

  @Test
  @DisplayName("findRefundedAllForBuyer() : 정상흐름 마지막 페이지 페이지")
  public void findRefundedAllForBuyer_ok_lastPage() {
    // given
    Member givenSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    saveRefundedPurchaseItems(10, givenBuyer, givenProduct);
    em.flush();
    em.clear();

    long inputBuyerId = givenBuyer.getId();
    PageRequest inputPageRequest = PageRequest.of(1, 6, Sort.by(Sort.Direction.DESC, "createDate"));

    // when
    Slice<PurchaseItem> sliceResult =
        target.findRefundedAllForBuyer(inputBuyerId, inputPageRequest);

    // then
    assertFalse(sliceResult.isFirst());
    assertTrue(sliceResult.isLast());
    List<PurchaseItem> resultPurchaseItems = sliceResult.getContent();
    assertEquals(4, resultPurchaseItems.size());
    resultPurchaseItems.forEach(
        item -> {
          assertEquals(inputBuyerId, item.getPurchase().getBuyerId());
          assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
          assertFalse(item.getRefunds().isEmpty());
        });
  }

  @Test
  @DisplayName("findRefundedAllForSeller() : 정상흐름 마지막 페이지")
  public void findRefundedAllForSeller_ok_lastPage() {
    // given
    Member givenSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    saveRefundedPurchaseItems(10, givenBuyer, givenProduct);
    em.flush();
    em.clear();

    long inputSellerId = givenSeller.getId();
    PageRequest inputPageRequest =
        PageRequest.of(1, 6, Sort.by(Sort.Direction.DESC, "finalRefundCreatedDate"));

    // when
    Slice<PurchaseItem> sliceData =
        target.findRefundedAllForSeller(inputSellerId, inputPageRequest);

    // then
    assertFalse(sliceData.isFirst());
    assertTrue(sliceData.isLast());
    assertEquals(4, sliceData.getContent().size());
    List<PurchaseItem> purchaseItems = sliceData.getContent();
    purchaseItems.forEach(
        item -> {
          assertEquals(inputSellerId, item.getSellerId());
          assertEquals(PurchaseStateType.COMPLETE, item.getPurchase().getState());
          assertFalse(item.getRefunds().isEmpty());
        });
  }

  private Member saveMember() {
    Member member = MemberBuilder.fullData().build();
    em.persist(member);
    return member;
  }

  private ProductType saveProductType(String typeName) {
    ProductType givenType = new ProductType(typeName);
    em.persist(givenType);
    return givenType;
  }

  private Product saveProduct(Member member, ProductType type) {
    Product product = ProductBuilder.makeNoBannedProduct(member, type);
    em.persist(product);
    return product;
  }

  private List<PurchaseItem> savePurchaseItems(int count, Member givenBuyer, Product givenProduct) {
    List<PurchaseItem> purchaseItems = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      PurchaseItem purchaseItem = PurchaseItemBuilder.makePurchaseItem(givenProduct);
      purchaseItems.add(purchaseItem);
      Purchase purchase =
          PurchaseBuilder.makePurchase(
              givenBuyer, new ArrayList<>(List.of(purchaseItem)), PurchaseStateType.COMPLETE);
      em.persist(purchase);
    }
    return purchaseItems;
  }

  private List<Refund> saveRefundedPurchaseItems(
      int count, Member givenBuyer, Product givenProduct) {
    List<PurchaseItem> savedPurchaseItems = savePurchaseItems(count, givenBuyer, givenProduct);
    return savedPurchaseItems.stream()
        .map(
            purchaseItem -> {
              Refund givenRefund = RefundBuilder.makeRefund(purchaseItem);
              em.persist(givenRefund);
              return givenRefund;
            })
        .toList();
  }
}
