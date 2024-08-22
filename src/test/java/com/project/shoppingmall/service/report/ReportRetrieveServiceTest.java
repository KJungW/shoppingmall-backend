package com.project.shoppingmall.service.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.repository.ProductReportRetrieveRepository;
import com.project.shoppingmall.repository.ReviewReportRetrieveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class ReportRetrieveServiceTest {
  private ReportRetrieveService target;
  private ProductReportRetrieveRepository mockProductReportRetrieveRepository;
  private ReviewReportRetrieveRepository mockReviewReportRetrieveRepository;

  @BeforeEach
  public void beforeEach() {
    mockProductReportRetrieveRepository = mock(ProductReportRetrieveRepository.class);
    mockReviewReportRetrieveRepository = mock(ReviewReportRetrieveRepository.class);
    target =
        new ReportRetrieveService(
            mockProductReportRetrieveRepository, mockReviewReportRetrieveRepository);
  }

  @Test
  @DisplayName("findUnprocessedProductReport() : 정상흐름")
  public void findUnprocessedProductReport_ok() {
    // given
    // - 인자세팅
    long givenProductTypeIdCaptor = 3L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    // when
    target.findUnprocessedProductReport(givenProductTypeIdCaptor, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductReportRetrieveRepository, times(1))
        .findUnprocessedProductReport(productTypeIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductTypeIdCaptor, productTypeIdCaptor.getValue());

    PageRequest captoredPageRequest = pageRequestCaptor.getValue();
    assertEquals(givenSliceNum, captoredPageRequest.getPageNumber());
    assertEquals(givenSliceSize, captoredPageRequest.getPageSize());
    assertEquals(
        Sort.Direction.DESC,
        captoredPageRequest.getSort().getOrderFor("createDate").getDirection());
    assertEquals(
        "createDate", captoredPageRequest.getSort().getOrderFor("createDate").getProperty());
  }

  @Test
  @DisplayName("findUnprocessedReviewReportReport() : 정상흐름")
  public void findUnprocessedReviewReportReport_ok() {
    // given
    // - 인자세팅
    long givenProductTypeIdCaptor = 3L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    // when
    target.findUnprocessedReviewReportReport(
        givenProductTypeIdCaptor, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockReviewReportRetrieveRepository, times(1))
        .findUnprocessedReviewReportReport(
            productTypeIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductTypeIdCaptor, productTypeIdCaptor.getValue());

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
