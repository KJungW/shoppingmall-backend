package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.testdata.product.Product_RealDataBuilder;
import com.project.shoppingmall.testdata.purchase.Purchase_RealDataBuilder;
import com.project.shoppingmall.testdata.purchaseitem.PurchaseItem_RealDataBuilder;
import com.project.shoppingmall.type.PurchaseStateType;
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
class PurchaseItemRepositoryTest {
  @Autowired private PurchaseItemRepository target;
  @Autowired private EntityManager em;

  @Test
  @DisplayName("findSalesRevenuePriceInMonthBySeller() : 정상흐름")
  public void findSalesRevenuePriceInMonthBySeller_ok() {
    // given
    Member givenSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    LocalDateTime inputStartDate = LocalDateTime.now();
    List<PurchaseItem> givenPurchaseItems =
        savePurchaseItems(10, givenBuyer, givenProduct, PurchaseStateType.COMPLETE);
    em.flush();
    LocalDateTime inputEndDate = LocalDateTime.now();
    em.clear();

    long inputSellerId = givenSeller.getId();

    // when
    Long realRevenuePrice =
        target.findSalesRevenuePriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    long expectedRevenuePrice =
        givenPurchaseItems.stream().mapToLong(PurchaseItem::getFinalPrice).sum();
    assertEquals(expectedRevenuePrice, realRevenuePrice);
  }

  @Test
  @DisplayName("findSalesRevenuePriceInMonthBySeller() : 정상흐름 - 현재 판매자의 판매기록만을 토대로 조회되는 것을 체크")
  public void findSalesRevenuePriceInMonthBySeller_checkOtherSellerExclude() {
    // given
    Member givenSeller = saveMember();
    Member givenOtherSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    Product givenOtherProduct = saveProduct(givenOtherSeller, givenType);
    LocalDateTime inputStartDate = LocalDateTime.now();
    List<PurchaseItem> givenPurchaseItems =
        savePurchaseItems(10, givenBuyer, givenProduct, PurchaseStateType.COMPLETE);
    List<PurchaseItem> givenOtherPurchaseItems =
        savePurchaseItems(10, givenBuyer, givenOtherProduct, PurchaseStateType.COMPLETE);
    em.flush();
    LocalDateTime inputEndDate = LocalDateTime.now();
    em.clear();

    long inputSellerId = givenSeller.getId();

    // when
    Long realRevenuePrice =
        target.findSalesRevenuePriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    long expectedRevenuePrice =
        givenPurchaseItems.stream().mapToLong(PurchaseItem::getFinalPrice).sum();
    assertEquals(expectedRevenuePrice, realRevenuePrice);
  }

  @Test
  @DisplayName("findSalesRevenuePriceInMonthBySeller() : 정상흐름 - 완료된 구매만을 토대로 조회되는 것을 체크")
  public void findSalesRevenuePriceInMonthBySeller_checkPurchaseSateIsComplete() {
    // given
    Member givenSeller = saveMember();
    Member givenBuyer = saveMember();
    ProductType givenType = saveProductType("test$test");
    Product givenProduct = saveProduct(givenSeller, givenType);
    LocalDateTime inputStartDate = LocalDateTime.now();
    List<PurchaseItem> givenCompletePurchaseItems =
        savePurchaseItems(10, givenBuyer, givenProduct, PurchaseStateType.COMPLETE);
    List<PurchaseItem> givenFailPurchaseItems =
        savePurchaseItems(10, givenBuyer, givenProduct, PurchaseStateType.FAIL);

    givenFailPurchaseItems.forEach(
        purchaseItem ->
            System.out.println(
                "purchaseItem.getPurchase().getState() = "
                    + purchaseItem.getPurchase().getState()));

    em.flush();
    LocalDateTime inputEndDate = LocalDateTime.now();
    em.clear();

    long inputSellerId = givenSeller.getId();

    // when
    Long realRevenuePrice =
        target.findSalesRevenuePriceInPeriodBySeller(inputSellerId, inputStartDate, inputEndDate);

    // then
    long expectedRevenuePrice =
        givenCompletePurchaseItems.stream().mapToLong(PurchaseItem::getFinalPrice).sum();
    assertEquals(expectedRevenuePrice, realRevenuePrice);
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
    Product product = Product_RealDataBuilder.makeProduct(member, type);
    em.persist(product);
    return product;
  }

  private List<PurchaseItem> savePurchaseItems(
      int count, Member givenBuyer, Product givenProduct, PurchaseStateType purchaseState) {
    List<PurchaseItem> purchaseItems = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      PurchaseItem purchaseItem = PurchaseItem_RealDataBuilder.makePurchaseItem(givenProduct);
      purchaseItems.add(purchaseItem);
      Purchase purchase =
          Purchase_RealDataBuilder.makePurchase(
              givenBuyer, new ArrayList<>(List.of(purchaseItem)), purchaseState);
      em.persist(purchase);
    }
    return purchaseItems;
  }
}
