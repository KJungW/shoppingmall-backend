package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.RecentlyPurchasedProduct;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.service.alarm.AlarmDeleteService;
import com.project.shoppingmall.service.alarm.AlarmFindService;
import com.project.shoppingmall.service.basket_item.BasketItemDeleteService;
import com.project.shoppingmall.service.basket_item.BasketItemFindService;
import com.project.shoppingmall.service.product.ProductDeleteService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import com.project.shoppingmall.service.report.ReportDeleteService;
import com.project.shoppingmall.service.report.ReportFindService;
import com.project.shoppingmall.service.review.ReviewDeleteService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.testdata.alarm.AlamBuilder;
import com.project.shoppingmall.testdata.basketitem.BasketItemBuilder;
import com.project.shoppingmall.testdata.product.ProductBuilder;
import com.project.shoppingmall.testdata.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.testdata.report.ProductReport_RealDataBuilder;
import com.project.shoppingmall.testdata.review.ReviewBuilder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ProductDeleteServiceTest {
  private ProductDeleteService target;
  private ProductFindService mockProductFindService;
  private ProductRepository mockProductRepository;
  private BasketItemFindService mockBasketItemFindService;
  private BasketItemDeleteService mockBasketItemDeleteService;
  private ReviewFindService mockReviewFindService;
  private ReviewDeleteService mockReviewDeleteService;
  private ReportFindService mockReportFindService;
  private ReportDeleteService mockReportDeleteService;
  private PurchaseItemFindService mockPurchaseItemFindService;
  private AlarmFindService mockAlarmFindService;
  private AlarmDeleteService mockAlarmDeleteService;
  private S3Service mockS3Service;

  @BeforeEach
  public void beforeEach() {
    mockProductFindService = mock(ProductFindService.class);
    mockProductRepository = mock(ProductRepository.class);
    mockBasketItemFindService = mock(BasketItemFindService.class);
    mockBasketItemDeleteService = mock(BasketItemDeleteService.class);
    mockReviewFindService = mock(ReviewFindService.class);
    mockReviewDeleteService = mock(ReviewDeleteService.class);
    mockReportFindService = mock(ReportFindService.class);
    mockReportDeleteService = mock(ReportDeleteService.class);
    mockPurchaseItemFindService = mock(PurchaseItemFindService.class);
    mockAlarmFindService = mock(AlarmFindService.class);
    mockAlarmDeleteService = mock(AlarmDeleteService.class);
    mockS3Service = mock(S3Service.class);

    target =
        new ProductDeleteService(
            mockProductFindService,
            mockProductRepository,
            mockBasketItemFindService,
            mockBasketItemDeleteService,
            mockReviewFindService,
            mockReviewDeleteService,
            mockReportFindService,
            mockReportDeleteService,
            mockPurchaseItemFindService,
            mockAlarmFindService,
            mockAlarmDeleteService,
            mockS3Service);

    ReflectionTestUtils.setField(target, "productDeletePossibleDate", 30);
  }

  @Test
  @DisplayName("deleteProductInController() : 정상흐름")
  public void deleteProductInController_ok() throws IOException {
    // given
    // - 인자세팅
    long givenSellerId = 10L;
    long givenProductId = 20L;

    // - productRepository.findByIdWithSeller() 세팅
    Product givenProduct = ProductBuilder.lightData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));

    // - purchaseItemService.findLatestByProduct() 세팅
    when(mockPurchaseItemFindService.findLatestByProduct(anyLong(), anyInt()))
        .thenReturn(new ArrayList<>());

    // - basketItemService.findAllByProduct() 세팅
    List<BasketItem> givenBasketItems =
        new ArrayList<>(
            List.of(
                BasketItemBuilder.fullData().build(),
                BasketItemBuilder.fullData().build(),
                BasketItemBuilder.fullData().build(),
                BasketItemBuilder.fullData().build()));
    when(mockBasketItemFindService.findAllByProduct(anyLong())).thenReturn(givenBasketItems);

    // - reviewService.findByProduct() 세팅
    List<Review> givenReviews =
        new ArrayList<>(
            List.of(
                ReviewBuilder.fullData().build(),
                ReviewBuilder.fullData().build(),
                ReviewBuilder.fullData().build()));
    when(mockReviewFindService.findByProduct(anyLong())).thenReturn(givenReviews);

    // - reportService.findAllByProduct() 세팅
    List<ProductReport> givenProductReports =
        new ArrayList<>(
            List.of(
                ProductReport_RealDataBuilder.fullData().build(),
                ProductReport_RealDataBuilder.fullData().build()));
    when(mockReportFindService.findAllByProduct(anyLong())).thenReturn(givenProductReports);

    // - alarmFindService.findByTargetProduct() 세팅
    List<Alarm> givenAlarms =
        new ArrayList<>(
            List.of(AlamBuilder.makeProductBanAlarm(10L), AlamBuilder.makeProductBanAlarm(20L)));
    when(mockAlarmFindService.findByTargetProduct(anyLong())).thenReturn(givenAlarms);

    // when
    target.deleteProductInController(givenSellerId, givenProductId);

    // then
    // - basketItemDeleteService.deleteBasketItemList() 체크
    ArgumentCaptor<List<BasketItem>> basketItemListCaptor = ArgumentCaptor.forClass(List.class);
    verify(mockBasketItemDeleteService, times(1))
        .deleteBasketItemList(basketItemListCaptor.capture());
    assertEquals(givenBasketItems.size(), basketItemListCaptor.getValue().size());

    // - reviewDeleteService.deleteReviewList() 체크
    ArgumentCaptor<List<Review>> reviewListCaptor = ArgumentCaptor.forClass(List.class);
    verify(mockReviewDeleteService, times(1)).deleteReviewList(reviewListCaptor.capture());
    assertEquals(givenReviews.size(), reviewListCaptor.getValue().size());

    // - reportDeleteService.deleteProductReportList() 체크
    ArgumentCaptor<List<ProductReport>> productReportListCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(mockReportDeleteService, times(1))
        .deleteProductReportList(productReportListCaptor.capture());
    assertEquals(givenProductReports.size(), productReportListCaptor.getValue().size());

    // - alarmDeleteService.deleteAlarmList() 체크
    ArgumentCaptor<List<Alarm>> alarmListCaptor = ArgumentCaptor.forClass(List.class);
    verify(mockAlarmDeleteService, times(1)).deleteAlarmList(alarmListCaptor.capture());
    assertEquals(givenAlarms.size(), alarmListCaptor.getValue().size());

    // - productRepository.delete() 체크
    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(mockProductRepository, times(1)).delete(productCaptor.capture());
    assertSame(givenProduct, productCaptor.getValue());
  }

  @Test
  @DisplayName("deleteProductInController() : 다른 회원의 제품을 제거하려고 시도하고 있습니다.")
  public void deleteProductInController_OtherMemberProduct() throws IOException {
    // given
    // - 인자세팅
    long givenSellerId = 10L;
    long givenProductId = 20L;

    // - productRepository.findByIdWithSeller() 세팅
    long givenOtherMemberId = 40L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenOtherMemberId);
    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));

    // when
    assertThrows(
        DataNotFound.class, () -> target.deleteProductInController(givenSellerId, givenProductId));
  }

  @Test
  @DisplayName("deleteProductInController() : 최근 구매기록이 존재하는 제품의 삭제 시도")
  public void deleteProductInController_RecentlyPurchasedProduct() throws IOException {
    // given
    // - 인자세팅
    long givenSellerId = 10L;
    long givenProductId = 20L;

    // - productRepository.findByIdWithSeller() 세팅
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));

    // - purchaseItemService.findLatestByProduct() 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(
        givenPurchaseItem, "createDate", LocalDateTime.now().minusDays(10));
    List<PurchaseItem> givenPurchaseItemList = new ArrayList<>(List.of(givenPurchaseItem));
    when(mockPurchaseItemFindService.findLatestByProduct(anyLong(), anyInt()))
        .thenReturn(givenPurchaseItemList);

    // when
    assertThrows(
        RecentlyPurchasedProduct.class,
        () -> target.deleteProductInController(givenSellerId, givenProductId));
  }
}
