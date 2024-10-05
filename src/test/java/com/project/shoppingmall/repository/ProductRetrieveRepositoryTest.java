package com.project.shoppingmall.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_entity.IntegrationTestDataMaker;
import com.project.shoppingmall.type.ProductSaleType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class ProductRetrieveRepositoryTest {
  @Autowired private ProductRetrieveRepository target;
  @Autowired private EntityManager em;
  @Autowired private IntegrationTestDataMaker testDataMaker;
  private PersistenceUnitUtil emUtil;

  @BeforeEach
  public void beforeEach() {
    emUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
  }

  @Test
  @DisplayName("findByProductType() : 정상흐름 - 첫번째 페이지")
  public void findByProductType_ok_firstPage() {
    // given
    long inputTypeId;
    PageRequest inputPageRequest = PageRequest.of(0, 4);

    Member seller = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(3, seller, type, false, ProductSaleType.ON_SALE);
    testDataMaker.saveProductList(3, seller, type, false, ProductSaleType.ON_SALE);
    testDataMaker.saveProductList(3, seller, type, false, ProductSaleType.DISCONTINUED);

    inputTypeId = type.getId();
    em.flush();
    em.clear();

    // when
    Slice<Product> result = target.findByProductType(inputTypeId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, true, false, true, false, result);
    SliceManager.checkContentSize(4, result);
    checkFetchLoad_findByProductType(result);
    checkWhere_findByProductType(inputTypeId, result);
  }

  @Test
  @DisplayName("findByProductType() : 정상흐름 - 마지막 페이지")
  public void findByProductType_ok_lastPage() {
    // given
    long inputTypeId;
    PageRequest inputPageRequest = PageRequest.of(1, 4);

    Member seller = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(3, seller, type, false, ProductSaleType.ON_SALE);
    testDataMaker.saveProductList(3, seller, type, false, ProductSaleType.ON_SALE);
    testDataMaker.saveProductList(3, seller, type, false, ProductSaleType.DISCONTINUED);

    inputTypeId = type.getId();
    em.flush();
    em.clear();

    // when
    Slice<Product> result = target.findByProductType(inputTypeId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findByProductType(result);
    checkWhere_findByProductType(inputTypeId, result);
  }

  @Test
  @DisplayName("findBySearchWord() : 정상흐름 - 첫번째 페이지")
  public void findBySearchWord_ok_firstPage() {
    // given
    String inputSearchWord = "searchWordTest";
    PageRequest inputPageRequest = PageRequest.of(0, 4);

    Member seller = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(
        3, inputSearchWord + "1234", seller, type, false, ProductSaleType.ON_SALE);
    testDataMaker.saveProductList(
        3, inputSearchWord + " test", seller, type, false, ProductSaleType.ON_SALE);

    em.flush();
    em.clear();

    // when
    Slice<Product> result = target.findBySearchWord(inputSearchWord, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, true, false, true, false, result);
    SliceManager.checkContentSize(4, result);
    checkFetchLoad_findBySearchWord(result);
    checkWhere_findBySearchWord(inputSearchWord, result);
  }

  @Test
  @DisplayName("findBySearchWord() : 정상흐름 - 마지막 페이지")
  public void findBySearchWord_ok_lastPage() {
    // given
    String inputSearchWord = "searchWordTest";
    PageRequest inputPageRequest = PageRequest.of(1, 4);

    Member seller = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(
        3, inputSearchWord + "1234", seller, type, false, ProductSaleType.ON_SALE);
    testDataMaker.saveProductList(
        3, inputSearchWord + " test", seller, type, false, ProductSaleType.ON_SALE);

    em.flush();
    em.clear();

    // when
    Slice<Product> result = target.findBySearchWord(inputSearchWord, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findBySearchWord(result);
    checkWhere_findBySearchWord(inputSearchWord, result);
  }

  @Test
  @DisplayName("findAllBySeller() : 정상흐름 - 첫번째 페이지")
  public void findAllBySeller_ok_firstPage() {
    // given
    long inputSellerId;
    PageRequest inputPageRequest = PageRequest.of(0, 4);

    Member seller = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(6, seller, type, false);

    inputSellerId = seller.getId();
    em.flush();
    em.clear();

    // when
    Slice<Product> result = target.findAllBySeller(inputSellerId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, true, false, true, false, result);
    SliceManager.checkContentSize(4, result);
    checkFetchLoad_findAllBySeller(result);
    checkWhere_findAllBySeller(inputSellerId, result);
  }

  @Test
  @DisplayName("findAllBySeller() : 정상흐름 - 마지막 페이지")
  public void findAllBySeller_ok_lastPage() {
    // given
    long inputSellerId;
    PageRequest inputPageRequest = PageRequest.of(1, 4);

    Member seller = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(6, seller, type, false);

    inputSellerId = seller.getId();
    em.flush();
    em.clear();

    // when
    Slice<Product> result = target.findAllBySeller(inputSellerId, inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(2, result);
    checkFetchLoad_findAllBySeller(result);
    checkWhere_findAllBySeller(inputSellerId, result);
  }

  @Test
  @DisplayName("findAllByRandom() : 정상흐름 - 첫번째 페이지")
  public void findAllByRandom_ok_firstPage() {
    // given
    PageRequest inputpageRequest = PageRequest.of(0, 5);

    Member seller = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(8, seller, type, false);

    // when
    Slice<Product> result = target.findAllByRandom(inputpageRequest);

    // then
    SliceManager.checkOnlyPageData(inputpageRequest, true, false, true, false, result);
    SliceManager.checkContentSize(5, result);
    checkFetchLoad_findAllByRandom(result);
    checkWhere_findAllByRandom(result);
  }

  @Test
  @DisplayName("findAllByRandom() : 정상흐름 - 마지막 페이지")
  public void findAllByRandom_ok_lastPage() {
    // given
    PageRequest inputPageRequest = PageRequest.of(1, 5);

    Member seller = testDataMaker.saveMember();
    ProductType type = testDataMaker.saveProductType("test$test");
    testDataMaker.saveProductList(8, seller, type, false);

    // when
    Slice<Product> result = target.findAllByRandom(inputPageRequest);

    // then
    SliceManager.checkOnlyPageData(inputPageRequest, false, true, false, true, result);
    SliceManager.checkContentSize(3, result);
    checkFetchLoad_findAllByRandom(result);
    checkWhere_findAllByRandom(result);
  }

  public void checkFetchLoad_findByProductType(Slice<Product> target) {
    target
        .getContent()
        .forEach(
            product -> {
              assertTrue(emUtil.isLoaded(product, "seller"));
              assertTrue(emUtil.isLoaded(product, "productType"));
            });
  }

  public void checkWhere_findByProductType(long typeId, Slice<Product> target) {
    target
        .getContent()
        .forEach(
            product -> {
              assertEquals(typeId, product.getProductType().getId());
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
              assertFalse(product.getIsBan());
            });
  }

  public void checkFetchLoad_findBySearchWord(Slice<Product> target) {
    target
        .getContent()
        .forEach(
            product -> {
              assertTrue(emUtil.isLoaded(product, "seller"));
              assertTrue(emUtil.isLoaded(product, "productType"));
            });
  }

  public void checkWhere_findBySearchWord(String searchWord, Slice<Product> target) {
    target
        .getContent()
        .forEach(
            product -> {
              assertTrue(product.getName().contains(searchWord));
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
              assertFalse(product.getIsBan());
            });
  }

  public void checkFetchLoad_findAllBySeller(Slice<Product> target) {
    target
        .getContent()
        .forEach(
            product -> {
              assertTrue(emUtil.isLoaded(product, "seller"));
              assertTrue(emUtil.isLoaded(product, "productType"));
            });
  }

  public void checkWhere_findAllBySeller(long sellerId, Slice<Product> target) {
    target
        .getContent()
        .forEach(
            product -> {
              assertEquals(sellerId, product.getSeller().getId());
            });
  }

  public void checkFetchLoad_findAllByRandom(Slice<Product> target) {
    target
        .getContent()
        .forEach(
            product -> {
              assertTrue(emUtil.isLoaded(product, "seller"));
              assertTrue(emUtil.isLoaded(product, "productType"));
            });
  }

  public void checkWhere_findAllByRandom(Slice<Product> target) {
    target
        .getContent()
        .forEach(
            product -> {
              assertEquals(ProductSaleType.ON_SALE, product.getSaleState());
              assertFalse(product.getIsBan());
            });
  }
}
