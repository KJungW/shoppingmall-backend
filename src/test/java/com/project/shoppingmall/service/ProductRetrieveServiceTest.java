package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.repository.ProductRetrieveRepository;
import com.project.shoppingmall.type.ProductRetrieveFilterType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class ProductRetrieveServiceTest {
  private ProductRetrieveService target;
  private ProductRetrieveRepository mockProductRetrieveRepository;

  @BeforeEach
  public void beforeEach() {
    mockProductRetrieveRepository = mock(ProductRetrieveRepository.class);
    target = new ProductRetrieveService(mockProductRetrieveRepository);
  }

  @Test
  @DisplayName("retrieveByTypeWithFilter() : 정상흐름1")
  public void retrieveByTypeWithFilter_ok1() {
    // given
    Long givenProductTypeId = 6L;
    int givenSliceSize = 5;
    int givenSliceNum = 10;
    ProductRetrieveFilterType givenFilterType = ProductRetrieveFilterType.LATEST;

    // when
    target.retrieveByTypeWithFilter(
        givenProductTypeId, givenSliceSize, givenSliceNum, givenFilterType);

    // then
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Boolean> isBanCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findByProductTypeIdAndIsBan(
            productTypeIdCaptor.capture(), isBanCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductTypeId, productTypeIdCaptor.getValue());
    assertEquals(false, isBanCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.DESC,
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate",
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("retrieveByTypeWithFilter() : 정상흐름2")
  public void retrieveByTypeWithFilter_ok2() {
    // given
    Long givenProductTypeId = 6L;
    int givenSliceSize = 5;
    int givenSliceNum = 10;
    ProductRetrieveFilterType givenFilterType = ProductRetrieveFilterType.LOW_PRICE;

    // when
    target.retrieveByTypeWithFilter(
        givenProductTypeId, givenSliceSize, givenSliceNum, givenFilterType);

    // then
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Boolean> isBanCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findByProductTypeIdAndIsBan(
            productTypeIdCaptor.capture(), isBanCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductTypeId, productTypeIdCaptor.getValue());
    assertEquals(false, isBanCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.ASC,
        pageRequestCaptor.getValue().getSort().getOrderFor("finalPrice").getDirection());
    assertEquals(
        "finalPrice",
        pageRequestCaptor.getValue().getSort().getOrderFor("finalPrice").getProperty());
  }

  @Test
  @DisplayName("retrieveBySearchWordWithFilter() : 정상흐름1")
  public void retrieveBySearchWordWithFilter_ok1() {
    // given
    String givenSearchWord = "testWord";
    int givenSliceSize = 5;
    int givenSliceNum = 10;
    ProductRetrieveFilterType givenFilterType = ProductRetrieveFilterType.LOW_SCORE;

    // when
    target.retrieveBySearchWordWithFilter(
        givenSearchWord, givenSliceSize, givenSliceNum, givenFilterType);

    // then
    ArgumentCaptor<String> searchWordCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> isBanCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findByNameContainingIgnoreCaseAndIsBan(
            searchWordCaptor.capture(), isBanCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenSearchWord, searchWordCaptor.getValue());
    assertEquals(false, isBanCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.ASC,
        pageRequestCaptor.getValue().getSort().getOrderFor("scoreAvg").getDirection());
    assertEquals(
        "scoreAvg", pageRequestCaptor.getValue().getSort().getOrderFor("scoreAvg").getProperty());
  }

  @Test
  @DisplayName("retrieveByTypeWithFilter() : 정상흐름2")
  public void retrieveBySearchWordWithFilter_ok2() {
    // given
    String givenSearchWord = "testWord";
    int givenSliceSize = 5;
    int givenSliceNum = 10;
    ProductRetrieveFilterType givenFilterType = ProductRetrieveFilterType.OLDEST;

    // when
    target.retrieveBySearchWordWithFilter(
        givenSearchWord, givenSliceSize, givenSliceNum, givenFilterType);

    // then
    ArgumentCaptor<String> searchWordCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Boolean> isBanCaptor = ArgumentCaptor.forClass(Boolean.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductRetrieveRepository, times(1))
        .findByNameContainingIgnoreCaseAndIsBan(
            searchWordCaptor.capture(), isBanCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenSearchWord, searchWordCaptor.getValue());
    assertEquals(false, isBanCaptor.getValue());
    assertEquals(givenSliceSize, pageRequestCaptor.getValue().getPageSize());
    assertEquals(givenSliceNum, pageRequestCaptor.getValue().getPageNumber());
    assertEquals(
        Sort.Direction.ASC,
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate",
        pageRequestCaptor.getValue().getSort().getOrderFor("createDate").getProperty());
  }
}
