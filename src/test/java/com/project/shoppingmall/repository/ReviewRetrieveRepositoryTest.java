package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import com.project.shoppingmall.type.PurchaseStateType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
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
class ReviewRetrieveRepositoryTest {
  @Autowired private ReviewRetrieveRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;
  private PersistenceUnitUtil emUtil;

  @BeforeEach
  public void beforeEach() {
    emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
  }

  @Test
  @DisplayName("findAllByProduct : 정상흐름")
  public void findAllByProduct_ok_firstPage() {
    // given
    long inputProductId;
    PageRequest inputPageRequest = PageRequest.of(1, 4, Sort.by(Sort.Direction.DESC, "createDate"));

    Product product = testDataMaker.saveProduct();
    Purchase purchase = testDataMaker.savePurchase(product, 6, PurchaseStateType.COMPLETE);
    List<Review> reviews = testDataMaker.saveReviewList(product, purchase.getPurchaseItems());

    inputProductId = product.getId();

    em.flush();
    em.clear();

    // when
    Slice<Review> result = target.findAllByProduct(inputProductId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findAllByProduct(result);
    checkWhere_findAllByProduct(inputProductId, result);
  }

  public void checkFetchLoad_findAllByProduct(Slice<Review> target) {
    target
        .getContent()
        .forEach(
            review -> {
              assertTrue(emUtil.isLoaded(review, "writer"));
            });
  }

  public void checkWhere_findAllByProduct(long productId, Slice<Review> target) {
    target
        .getContent()
        .forEach(
            review -> {
              assertEquals(productId, review.getProduct().getId());
              assertFalse(review.getIsBan());
            });
  }
}
