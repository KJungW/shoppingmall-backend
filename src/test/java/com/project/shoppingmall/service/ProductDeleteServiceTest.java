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
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.service.report.ReportDeleteService;
import com.project.shoppingmall.service.report.ReportService;
import com.project.shoppingmall.service.review.ReviewDeleteService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.testdata.*;
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
  private ProductService mockProductService;
  private ProductRepository mockProductRepository;
  private BasketItemFindService mockBasketItemFindService;
  private BasketItemDeleteService mockBasketItemDeleteService;
  private ReviewService mockReviewService;
  private ReviewDeleteService mockReviewDeleteService;
  private ReportService mockReportService;
  private ReportDeleteService mockReportDeleteService;
  private PurchaseItemService mockPurchaseItemService;
  private AlarmFindService mockAlarmFindService;
  private AlarmDeleteService mockAlarmDeleteService;

  @BeforeEach
  public void beforeEach() {
    mockProductService = mock(ProductService.class);
    mockProductRepository = mock(ProductRepository.class);
    mockBasketItemFindService = mock(BasketItemFindService.class);
    mockBasketItemDeleteService = mock(BasketItemDeleteService.class);
    mockReviewService = mock(ReviewService.class);
    mockReviewDeleteService = mock(ReviewDeleteService.class);
    mockReportService = mock(ReportService.class);
    mockReportDeleteService = mock(ReportDeleteService.class);
    mockPurchaseItemService = mock(PurchaseItemService.class);
    mockAlarmFindService = mock(AlarmFindService.class);
    mockAlarmDeleteService = mock(AlarmDeleteService.class);

    target =
        new ProductDeleteService(
            mockProductService,
            mockProductRepository,
            mockBasketItemFindService,
            mockBasketItemDeleteService,
            mockReviewService,
            mockReviewDeleteService,
            mockReportService,
            mockReportDeleteService,
            mockPurchaseItemService,
            mockAlarmFindService,
            mockAlarmDeleteService);

    ReflectionTestUtils.setField(target, "productDeletePossibleDate", 30);
  }

  @Test
  @DisplayName("deleteProductBySeller() : 정상흐름")
  public void deleteProductBySeller_ok() throws IOException {
    // given
    // - 인자세팅
    long givenSellerId = 10L;
    long givenProductId = 20L;

    // - productRepository.findByIdWithSeller() 세팅
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // - purchaseItemService.findLatestByProduct() 세팅
    when(mockPurchaseItemService.findLatestByProduct(anyLong(), anyInt()))
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
    when(mockReviewService.findByProduct(anyLong())).thenReturn(givenReviews);

    // - reportService.findAllByProduct() 세팅
    List<ProductReport> givenProductReports =
        new ArrayList<>(
            List.of(
                ProductReportBuilder.fullData().build(), ProductReportBuilder.fullData().build()));
    when(mockReportService.findAllByProduct(anyLong())).thenReturn(givenProductReports);

    // - alarmFindService.findByTargetProduct() 세팅
    List<Alarm> givenAlarms =
        new ArrayList<>(
            List.of(
                AlamBuilder.productBanFullData().build(),
                AlamBuilder.productBanFullData().build()));
    when(mockAlarmFindService.findByTargetProduct(anyLong())).thenReturn(givenAlarms);

    // when
    target.deleteProductBySeller(givenSellerId, givenProductId);

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
  @DisplayName("deleteProductBySeller() : 다른 회원의 제품을 제거하려고 시도하고 있습니다.")
  public void deleteProductBySeller_OtherMemberProduct() throws IOException {
    // given
    // - 인자세팅
    long givenSellerId = 10L;
    long givenProductId = 20L;

    // - productRepository.findByIdWithSeller() 세팅
    long givenOtherMemberId = 40L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenOtherMemberId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    assertThrows(
        DataNotFound.class, () -> target.deleteProductBySeller(givenSellerId, givenProductId));
  }

  @Test
  @DisplayName("deleteProductBySeller() : 최근 구매기록이 존재하는 제품의 삭제 시도")
  public void deleteProductBySeller_RecentlyPurchasedProduct() throws IOException {
    // given
    // - 인자세팅
    long givenSellerId = 10L;
    long givenProductId = 20L;

    // - productRepository.findByIdWithSeller() 세팅
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // - purchaseItemService.findLatestByProduct() 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(
        givenPurchaseItem, "createDate", LocalDateTime.now().minusDays(10));
    List<PurchaseItem> givenPurchaseItemList = new ArrayList<>(List.of(givenPurchaseItem));
    when(mockPurchaseItemService.findLatestByProduct(anyLong(), anyInt()))
        .thenReturn(givenPurchaseItemList);

    // when
    assertThrows(
        RecentlyPurchasedProduct.class,
        () -> target.deleteProductBySeller(givenSellerId, givenProductId));
  }
}
