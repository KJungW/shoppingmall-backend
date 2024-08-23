package com.project.shoppingmall.service_manage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.repository.ProductReportRetrieveRepository;
import com.project.shoppingmall.repository.ReviewReportRetrieveRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service_manage.report.ReportRetrieveManageService;
import com.project.shoppingmall.testdata.MemberBuilder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

class ReportRetrieveManageServiceTest {
  private ReportRetrieveManageService target;
  private MemberService mockMemberService;
  private ProductReportRetrieveRepository mockProductReportRetrieveRepository;
  private ReviewReportRetrieveRepository mockReviewReportRetrieveRepository;

  @BeforeEach
  public void beforeEach() {
    mockMemberService = mock(MemberService.class);
    mockProductReportRetrieveRepository = mock(ProductReportRetrieveRepository.class);
    mockReviewReportRetrieveRepository = mock(ReviewReportRetrieveRepository.class);
    target =
        new ReportRetrieveManageService(
            mockMemberService,
            mockProductReportRetrieveRepository,
            mockReviewReportRetrieveRepository);
  }

  @Test
  @DisplayName("findUnprocessedProductReport() : 정상흐름")
  public void findUnprocessedProductReport_ok() {
    // given
    // - 인자세팅
    long givenProductTypeId = 3L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    // when
    target.findUnprocessedProductReport(givenProductTypeId, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductReportRetrieveRepository, times(1))
        .findUnprocessedProductReport(productTypeIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductTypeId, productTypeIdCaptor.getValue());

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
    long givenProductTypeId = 3L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    // when
    target.findUnprocessedReviewReportReport(givenProductTypeId, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> productTypeIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockReviewReportRetrieveRepository, times(1))
        .findUnprocessedReviewReportReport(
            productTypeIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(givenProductTypeId, productTypeIdCaptor.getValue());

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
  @DisplayName("findProductReportsByProductSeller() : 정상흐름")
  public void findProductReportsByProductSeller_ok() {
    // given
    // - 인자세팅
    long inputProductSellerId = 3L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    Member givenProductSeller = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProductSeller, "id", inputProductSellerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenProductSeller));

    // when
    target.findProductReportsByProductSeller(inputProductSellerId, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> productSellerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockProductReportRetrieveRepository, times(1))
        .findProductReportsByProductSeller(
            productSellerIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(inputProductSellerId, productSellerIdCaptor.getValue());

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
  @DisplayName("findReviewReportsByReviewWriter() : 정상흐름")
  public void findReviewReportsByReviewWriter() {
    // given
    // - 인자세팅
    long inputReviewWriterId = 3L;
    int givenSliceNum = 0;
    int givenSliceSize = 5;

    Member givenReviewWriter = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReviewWriter, "id", inputReviewWriterId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenReviewWriter));

    // when
    target.findReviewReportsByReviewWriter(inputReviewWriterId, givenSliceNum, givenSliceSize);

    // then
    ArgumentCaptor<Long> reviewWriterIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
    verify(mockReviewReportRetrieveRepository, times(1))
        .findReviewReportsByReviewWriter(
            reviewWriterIdCaptor.capture(), pageRequestCaptor.capture());

    assertEquals(inputReviewWriterId, reviewWriterIdCaptor.getValue());

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
