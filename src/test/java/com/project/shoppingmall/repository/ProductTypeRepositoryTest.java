package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.final_value.FinalValue;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class ProductTypeRepositoryTest {
  @Autowired private ProductTypeRepository target;
  @Autowired private EntityManager em;

  @BeforeEach
  public void beforeEach() {
    String jpql =
        "select pt from ProductType pt "
            + "where pt.typeName like concat(:baseProductTypePrefix, '%') ";
    ProductType originBaseProductType =
        em.createQuery(jpql, ProductType.class)
            .setParameter("baseProductTypePrefix", FinalValue.BASE_PRODUCT_TYPE_PREFIX)
            .getSingleResult();
    em.remove(originBaseProductType);
  }

  @Test
  @DisplayName("findBaseProductType() : 정상흐름")
  public void findBaseProductType_ok() {
    // given
    // - 기본 제품타입 생성
    ProductType baseProductType = new ProductType(FinalValue.BASE_PRODUCT_TYPE_NAME);
    ReflectionTestUtils.setField(
        baseProductType,
        "typeName",
        FinalValue.BASE_PRODUCT_TYPE_PREFIX + FinalValue.BASE_PRODUCT_TYPE_NAME);
    em.persist(baseProductType);

    // - 그외 일반 제품타입 생성
    for (int i = 0; i < 10; i++) {
      ProductType testType = new ProductType("test$test" + i);
      em.persist(testType);
    }

    // when
    Optional<ProductType> queryResult =
        target.findBaseProductType(FinalValue.BASE_PRODUCT_TYPE_PREFIX);

    // then
    assertTrue(queryResult.isPresent());
    assertEquals(
        FinalValue.BASE_PRODUCT_TYPE_PREFIX + FinalValue.BASE_PRODUCT_TYPE_NAME,
        queryResult.get().getTypeName());
  }

  @Test
  @DisplayName("findBaseProductType() : 기본 제품타입이 없는 경우")
  public void findBaseProductType_noProductType() {
    // given
    // - 일반 제품타입 생성
    for (int i = 0; i < 10; i++) {
      ProductType testType = new ProductType("test$test" + i);
      em.persist(testType);
    }

    // when
    Optional<ProductType> queryResult =
        target.findBaseProductType(FinalValue.BASE_PRODUCT_TYPE_PREFIX);

    // then
    assertTrue(queryResult.isEmpty());
  }
}
