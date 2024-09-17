package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.*;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.type.RefundStateType;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @Test
  @DisplayName("findRefundPriceInPeriodBySeller() : 정상흐름")
  public void findRefundPriceInPeriodBySeller_ok() {
    // given
    Member givenSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    LocalDateTime inputStartDate = LocalDateTime.now();
    List<Refund> givenRefunds =
        saveRefundedPurchaseItems(10, givenBuyer, givenProduct, RefundStateType.COMPLETE);
    em.flush();
    LocalDateTime inputEndDate = LocalDateTime.now();
    em.clear();

    long inputSellerId = givenSeller.getId();

    // when
    Long realRefundPrice =
        target.findRefundPriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    long expectRefundPrice = givenRefunds.stream().mapToLong(Refund::getRefundPrice).sum();
    assertEquals(expectRefundPrice, realRefundPrice);
  }

  @Test
  @DisplayName("findRefundPriceInPeriodBySeller() : 정상흐름 - Complete 상태의 환불만을 토대로 조회되는 것을 체크")
  public void findRefundPriceInPeriodBySeller_checkRefundStateIsComplete() {
    // given
    Member givenSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    LocalDateTime inputStartDate = LocalDateTime.now();
    List<Refund> givenCompleteRefunds =
        saveRefundedPurchaseItems(10, givenBuyer, givenProduct, RefundStateType.COMPLETE);
    List<Refund> givenRequestRefunds =
        saveRefundedPurchaseItems(10, givenBuyer, givenProduct, RefundStateType.REJECTED);
    em.flush();
    LocalDateTime inputEndDate = LocalDateTime.now();
    em.clear();

    long inputSellerId = givenSeller.getId();

    // when
    Long realRefundPrice =
        target.findRefundPriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    long expectRefundPrice = givenCompleteRefunds.stream().mapToLong(Refund::getRefundPrice).sum();
    assertEquals(expectRefundPrice, realRefundPrice);
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
      int count, Member givenBuyer, Product givenProduct, RefundStateType refundState) {
    List<PurchaseItem> savedPurchaseItems = savePurchaseItems(count, givenBuyer, givenProduct);
    return savedPurchaseItems.stream()
        .map(
            purchaseItem -> {
              Refund givenRefund = RefundBuilder.makeRefund(refundState, purchaseItem);
              em.persist(givenRefund);
              return givenRefund;
            })
        .toList();
  }
}
