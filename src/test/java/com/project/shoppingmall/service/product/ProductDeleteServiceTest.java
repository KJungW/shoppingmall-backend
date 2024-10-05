package com.project.shoppingmall.service.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.RecentlyPurchasedProduct;
import com.project.shoppingmall.repository.ProductRepository;
import com.project.shoppingmall.service.alarm.AlarmDeleteService;
import com.project.shoppingmall.service.alarm.AlarmFindService;
import com.project.shoppingmall.service.basket_item.BasketItemDeleteService;
import com.project.shoppingmall.service.basket_item.BasketItemFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import com.project.shoppingmall.service.report.ReportDeleteService;
import com.project.shoppingmall.service.report.ReportFindService;
import com.project.shoppingmall.service.review.ReviewDeleteService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
  private Integer productDeletePossibleDate;

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
        spy(
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
                mockS3Service));

    productDeletePossibleDate = 30;
    ReflectionTestUtils.setField(target, "productDeletePossibleDate", productDeletePossibleDate);
  }

  @Test
  @DisplayName("deleteProductInController() : 정상흐름")
  public void deleteProductInController_ok() {
    // given
    long inputSellerId = 10L;
    long inputProductId = 20L;

    Member givenSeller = MemberBuilder.makeMember(inputSellerId);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenSeller);
    LocalDateTime deletedPossibleDate =
        LocalDateTime.now().minusDays(productDeletePossibleDate + 1);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(30L, givenProduct, deletedPossibleDate);

    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));
    when(mockPurchaseItemFindService.findLatestByProduct(anyLong(), anyInt()))
        .thenReturn(List.of(givenPurchaseItem));
    doNothing().when(target).deleteProduct(any());

    // when
    target.deleteProductInController(inputSellerId, inputProductId);
  }

  @Test
  @DisplayName("deleteProductInController() : 다른 회원의 제품을 제거하려고 시도하고 있습니다.")
  public void deleteProductInController_OtherMemberProduct() {
    // given
    long inputSellerId = 10L;
    long inputProductId = 20L;

    Member givenOtherMember = MemberBuilder.makeMember(50L);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenOtherMember);

    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));
    doNothing().when(target).deleteProduct(any());

    // when
    assertThrows(
        DataNotFound.class, () -> target.deleteProductInController(inputSellerId, inputProductId));
  }

  @Test
  @DisplayName("deleteProductInController() : 최근 구매기록이 존재하는 제품의 삭제 시도")
  public void deleteProductInController_RecentlyPurchasedProduct() {
    // given
    long inputSellerId = 10L;
    long inputProductId = 20L;

    Member givenSeller = MemberBuilder.makeMember(inputSellerId);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenSeller);
    LocalDateTime deletedImpossibleDate =
        LocalDateTime.now().minusDays(productDeletePossibleDate - 1);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(30L, givenProduct, deletedImpossibleDate);

    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));
    when(mockPurchaseItemFindService.findLatestByProduct(anyLong(), anyInt()))
        .thenReturn(List.of(givenPurchaseItem));
    doNothing().when(target).deleteProduct(any());

    // when
    assertThrows(
        RecentlyPurchasedProduct.class,
        () -> target.deleteProductInController(inputSellerId, inputProductId));
  }
}
