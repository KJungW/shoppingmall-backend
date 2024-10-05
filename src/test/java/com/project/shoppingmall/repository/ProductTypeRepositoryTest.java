package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.final_value.FinalValue;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class ProductTypeRepositoryTest {
  @Autowired private ProductTypeRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;

  @Test
  @DisplayName("findBaseProductType() : 정상흐름")
  public void findBaseProductType_ok() {
    // given
    List<ProductType> otherProductType = testDataMaker.saveProductTypeList(5);

    // when
    Optional<ProductType> queryResult =
        target.findBaseProductType(FinalValue.BASE_PRODUCT_TYPE_PREFIX);

    // then
    checkResult_findBaseProductType(queryResult);
  }

  @Test
  @DisplayName("findBaseProductType() : 기본 제품타입이 없는 경우")
  public void findBaseProductType_noProductType() {
    // given
    deleteBaseProductType();
    List<ProductType> otherProductType = testDataMaker.saveProductTypeList(5);

    // when
    Optional<ProductType> queryResult =
        target.findBaseProductType(FinalValue.BASE_PRODUCT_TYPE_PREFIX);

    // then
    assertTrue(queryResult.isEmpty());
  }

  public void deleteBaseProductType() {
    String deleteQueryString =
        "delete from ProductType pt "
            + "where pt.typeName like concat(:baseProductTypePrefix, '%')";
    int deletedRowCount =
        em.createQuery(deleteQueryString)
            .setParameter("baseProductTypePrefix", FinalValue.BASE_PRODUCT_TYPE_PREFIX)
            .executeUpdate();
    assertEquals(1, deletedRowCount);
  }

  public void checkResult_findBaseProductType(Optional<ProductType> queryResult) {
    assertTrue(queryResult.isPresent());
    assertEquals(
        FinalValue.BASE_PRODUCT_TYPE_PREFIX + FinalValue.BASE_PRODUCT_TYPE_NAME,
        queryResult.get().getTypeName());
  }
}
