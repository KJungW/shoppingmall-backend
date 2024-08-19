package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.repository.ReviewRetrieveRepository;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.review.ReviewRetrieveService;
import com.project.shoppingmall.testdata.ProductBuilder;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

class ReviewRetrieveServiceTest {
  private ReviewRetrieveService target;
  private ReviewRetrieveRepository mockReviewRetrieveRepository;
  private ProductService mockProductService;

  @BeforeEach
  public void beforeEach() {
    this.mockReviewRetrieveRepository = mock(ReviewRetrieveRepository.class);
    this.mockProductService = mock(ProductService.class);
    target = new ReviewRetrieveService(mockReviewRetrieveRepository, mockProductService);
  }

  @Test
  @DisplayName("retrieveByProduct() : 정상흐름")
  public void retrieveByProduct_ok() throws IOException {
    // given
    long givenProductId = 10L;
    int givenSliceNum = 3;
    int givenSliceSize = 20;

    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    when(mockProductService.findById(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    target.retrieveByProduct(givenProductId, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> productIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockReviewRetrieveRepository, times(1))
        .findAllByProduct(productIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductId, productIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate", captoredPageRequest.getSort().getOrderFor("createDate").getProperty());
  }
}
