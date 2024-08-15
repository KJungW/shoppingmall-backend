package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.exception.ContinuousReportError;
import com.project.shoppingmall.repository.ProductReportRepository;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.ProductBuilder;
import com.project.shoppingmall.testdata.ProductReportBuilder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ReportServiceTest {
  private ReportService target;
  private ProductService mockedProductService;
  private MemberService mockedMemberService;
  private ProductReportRepository mockedProductReportRepository;

  @BeforeEach
  public void beforeEach() {
    mockedProductService = mock(ProductService.class);
    mockedMemberService = mock(MemberService.class);
    mockedProductReportRepository = mock(ProductReportRepository.class);
    target =
        new ReportService(mockedProductService, mockedMemberService, mockedProductReportRepository);
  }

  @Test
  @DisplayName("saveProductReport() : 정상흐름")
  public void saveProductReport_ok() throws IOException {
    // given
    // - 인자세팅
    long rightMemberId = 10L;
    long rightProductId = 20L;
    String rightTitle = "test Title";
    String rightDescription = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockedMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockedProductService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

    // - productReportRepository.findLatestReport() 세팅
    when(mockedProductReportRepository.findLatestReport(any(), any(), any()))
        .thenReturn(new ArrayList<>());

    // when
    target.saveProductReport(rightMemberId, rightProductId, rightTitle, rightDescription);

    // then
    ArgumentCaptor<ProductReport> reportCaptor = ArgumentCaptor.forClass(ProductReport.class);
    verify(mockedProductReportRepository, times(1)).save(reportCaptor.capture());
    ProductReport reportResult = reportCaptor.getValue();

    assertEquals(rightMemberId, reportResult.getReporter().getId());
    assertEquals(rightTitle, reportResult.getTitle());
    assertEquals(rightDescription, reportResult.getDescription());
    assertFalse(reportResult.isProcessedComplete());
    assertEquals(givenProductId, reportResult.getProduct().getId());
  }

  @Test
  @DisplayName("saveProductReport() : 24시간 이내의 같은 제품 연속신고")
  public void saveProductReport_continuousReportIn24Hour() throws IOException {
    // given
    // - 인자세팅
    long rightMemberId = 10L;
    long rightProductId = 20L;
    String rightTitle = "test Title";
    String rightDescription = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockedMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockedProductService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

    // - productReportRepository.findLatestReport() 세팅
    ProductReport givenProductReport = ProductReportBuilder.fullData().build();
    ReflectionTestUtils.setField(
        givenProductReport, "createDate", LocalDateTime.now().minusHours(15));
    when(mockedProductReportRepository.findLatestReport(any(), any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(givenProductReport)));

    // when
    assertThrows(
        ContinuousReportError.class,
        () ->
            target.saveProductReport(rightMemberId, rightProductId, rightTitle, rightDescription));
  }

  @Test
  @DisplayName("saveProductReport() : 24시간 이후의 같은 제품 연속신고")
  public void saveProductReport_continuousReportAfter24Hour() throws IOException {
    // given
    // - 인자세팅
    long rightMemberId = 10L;
    long rightProductId = 20L;
    String rightTitle = "test Title";
    String rightDescription = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockedMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockedProductService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

    // - productReportRepository.findLatestReport() 세팅
    ProductReport givenProductReport = ProductReportBuilder.fullData().build();
    ReflectionTestUtils.setField(
        givenProductReport, "createDate", LocalDateTime.now().minusHours(30));
    when(mockedProductReportRepository.findLatestReport(any(), any(), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(givenProductReport)));

    // when
    target.saveProductReport(rightMemberId, rightProductId, rightTitle, rightDescription);

    // then
    ArgumentCaptor<ProductReport> reportCaptor = ArgumentCaptor.forClass(ProductReport.class);
    verify(mockedProductReportRepository, times(1)).save(reportCaptor.capture());
    ProductReport reportResult = reportCaptor.getValue();

    assertEquals(rightMemberId, reportResult.getReporter().getId());
    assertEquals(rightTitle, reportResult.getTitle());
    assertEquals(rightDescription, reportResult.getDescription());
    assertFalse(reportResult.isProcessedComplete());
    assertEquals(givenProductId, reportResult.getProduct().getId());
  }
}
