package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.testdata.member.MemberBuilder;
import com.project.shoppingmall.testdata.product.Product_RealDataBuilder;
import jakarta.persistence.EntityManager;
import java.io.IOException;
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
class ProductBulkRepositoryTest {
  @Autowired private ProductBulkRepository target;
  @Autowired private EntityManager em;

  @BeforeEach
  public void beforeEach() throws IOException {
    // 판매자와 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);

    // 제품 타입 생성
    ProductType type = new ProductType("test$test");
    em.persist(type);

    // 제품 생성
    for (int i = 0; i < 10; i++) {
      Product targetProduct = Product_RealDataBuilder.makeProduct(seller, type);
      em.persist(targetProduct);
    }
  }

  @Test
  @DisplayName("banProductsBySellerId() : 정상흐름")
  public void banProductsBySellerId_ok() {
    // given
    // - 새로운 판매자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    long givenSellerId = seller.getId();

    // - 새로운 제품 타입 생성
    ProductType type = new ProductType("test$test1");
    em.persist(type);

    // - 새로운 제품 생성
    for (int i = 0; i < 8; i++) {
      Product targetProduct = Product_RealDataBuilder.makeProduct(seller, type);
      em.persist(targetProduct);
    }
    // - 인자세팅
    long inputSellerId = givenSellerId;
    boolean inputIsBan = true;

    // when
    int rowCount = target.banProductsBySellerId(inputSellerId, inputIsBan);

    // then
    assertEquals(8, rowCount);

    String query = "select p from Product p " + "left join p.seller s " + "where s.id = :sellerId";
    List<Product> queryResult =
        em.createQuery(query, Product.class)
            .setParameter("sellerId", inputSellerId)
            .getResultList();
    assertEquals(8, queryResult.size());
    queryResult.forEach(product -> assertEquals(inputIsBan, product.getIsBan()));
  }

  @Test
  @DisplayName("banProductsBySellerId() : 제품 데이터 없음")
  public void banProductsBySellerId_noProduct() throws IOException {
    // given
    // - 새로운 판매자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);
    long givenSellerId = seller.getId();

    // - 인자세팅
    long inputSellerId = givenSellerId;
    boolean inputIsBan = true;

    // when
    int rowCount = target.banProductsBySellerId(inputSellerId, inputIsBan);

    // then
    assertEquals(0, rowCount);

    String query = "select p from Product p " + "left join p.seller s " + "where s.id = :sellerId";
    List<Product> queryResult =
        em.createQuery(query, Product.class)
            .setParameter("sellerId", inputSellerId)
            .getResultList();
    assertEquals(0, queryResult.size());
  }

  @Test
  @DisplayName("changeProductTypeToBaseType() : 정상흐름")
  public void changeProductTypeToBaseType() throws IOException {
    // given
    // - 새로운 판매자 생성
    Member seller = MemberBuilder.fullData().build();
    em.persist(seller);

    // - 새로운 제품 타입 생성
    ProductType baseType = new ProductType("base$type");
    em.persist(baseType);
    ProductType commonType = new ProductType("common$type");
    em.persist(commonType);
    long givenTypeId = commonType.getId();

    // - 새로운 제품 생성
    for (int i = 0; i < 8; i++) {
      Product targetProduct = Product_RealDataBuilder.makeProduct(seller, commonType);
      em.persist(targetProduct);
    }
    em.flush();
    em.clear();

    // - 인자세팅
    String baseProductGetQuery = "select pt from ProductType pt where pt.typeName like 'base$type'";
    ProductType inputBaseType =
        em.createQuery(baseProductGetQuery, ProductType.class).getSingleResult();
    long inputTypeId = givenTypeId;

    // when
    int rowCount = target.changeProductTypeToBaseType(inputBaseType, inputTypeId);

    // then
    assertEquals(8, rowCount);

    String getProductQuery =
        "select p from Product p "
            + "left join fetch p.productType pt "
            + "where pt.id = :baseProductTypeId";
    List<Product> queryResult =
        em.createQuery(getProductQuery, Product.class)
            .setParameter("baseProductTypeId", inputBaseType.getId())
            .getResultList();
    assertEquals(8, queryResult.size());
    queryResult.forEach(
        product -> assertEquals(inputBaseType.getId(), product.getProductType().getId()));
  }
}
