package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import jakarta.persistence.EntityManager;
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
class ProductBulkRepositoryTest {
  @Autowired private ProductBulkRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;

  @Test
  @DisplayName("banProductsBySellerId() : 정상흐름")
  public void banProductsBySellerId_ok() {
    // given
    long inputSellerId;
    boolean inputIsBan = true;

    int givenProductCount = 10;
    Member seller = testDataMaker.saveMember();
    ProductType productType = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(givenProductCount, seller, productType, false);

    inputSellerId = seller.getId();
    em.flush();
    em.clear();

    // when
    int rowCount = target.banProductsBySellerId(inputSellerId, inputIsBan);

    // then
    assertEquals(givenProductCount, rowCount);
  }

  @Test
  @DisplayName("banProductsBySellerId() : 제품 데이터 없음")
  public void banProductsBySellerId_noProduct() {
    // given
    long inputSellerId;
    boolean inputIsBan = true;

    int givenProductCount = 10;
    Member seller = testDataMaker.saveMember();
    Member otherSeller = testDataMaker.saveMember();
    ProductType productType = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(givenProductCount, otherSeller, productType, false);

    inputSellerId = seller.getId();
    em.flush();
    em.clear();

    // when
    int rowCount = target.banProductsBySellerId(inputSellerId, inputIsBan);

    // then
    assertEquals(0, rowCount);
  }

  @Test
  @DisplayName("changeProductTypeToBaseType() : 정상흐름")
  public void changeProductTypeToBaseType() {
    // given
    ProductType inputBaseProductType;
    long inputTargetProductType;

    int givenProductCount = 10;
    Member seller = testDataMaker.saveMember();
    ProductType baseProductType = testDataMaker.saveProductType("test$base");
    ProductType originProductType = testDataMaker.saveProductType("test$origin");
    testDataMaker.saveProductList(givenProductCount, seller, originProductType, false);

    inputBaseProductType = baseProductType;
    inputTargetProductType = originProductType.getId();
    em.flush();
    em.clear();

    // when
    int rowCount = target.changeProductTypeToBaseType(inputBaseProductType, inputTargetProductType);

    // then
    assertEquals(givenProductCount, rowCount);
    checkProductTypeChange(baseProductType.getId(), givenProductCount);
  }

  public void checkProductTypeChange(long productTypeId, int expectedRowCount) {
    String getProductQuery =
        "select p from Product p "
            + "left join fetch p.productType pt "
            + "where pt.id = :productTypeId";

    List<Product> queryResult =
        em.createQuery(getProductQuery, Product.class)
            .setParameter("productTypeId", productTypeId)
            .getResultList();

    assertEquals(expectedRowCount, queryResult.size());
    queryResult.forEach(product -> assertEquals(productTypeId, product.getProductType().getId()));
  }
}
