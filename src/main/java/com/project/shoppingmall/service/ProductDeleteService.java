package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductDeleteService {
  private final ProductService productService;
  private final ProductRepository productRepository;
  private final BasketItemService basketItemService;
  private final BasketItemDeleteService basketItemDeleteService;
  private final ReviewService reviewService;
  private final ReviewDeleteService reviewDeleteService;
  private final ReportService reportService;
  private final ReportDeleteService reportDeleteService;
  private final PurchaseItemService purchaseItemService;

  @Value("${project_role.product.delete_possible_day}")
  private Integer productDeletePossibleDate;

  public void deleteProduct(Product product) {
    if (product == null) throw new ServerLogicError("비어있는 Product를 제거하려고 시도하고 있습니다.");

    List<BasketItem> basketItemList = basketItemService.findAllByProduct(product.getId());
    basketItemDeleteService.deleteBasketItemList(basketItemList);

    List<Review> reviewList = reviewService.findByProduct(product.getId());
    reviewDeleteService.deleteReviewList(reviewList);

    List<ProductReport> productReportList = reportService.findAllByProduct(product.getId());
    reportDeleteService.deleteProductReportList(productReportList);

    productRepository.delete(product);
  }

  public void deleteProductBySeller(long sellerId, long productId) {
    Product product =
        productService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 Prdocut가 존재하지 않습니다."));
    if (!product.getSeller().getId().equals(sellerId))
      throw new DataNotFound("다른 회원의 Product를 제거하려고 하고 있습니다.");

    List<PurchaseItem> latestPurchaseList =
        purchaseItemService.findLatestByProduct(product.getId(), 1);
    if (latestPurchaseList != null && !latestPurchaseList.isEmpty()) {
      PurchaseItem lastestPurchaseItem = latestPurchaseList.get(0);
      if (lastestPurchaseItem
          .getCreateDate()
          .isAfter(LocalDateTime.now().minusDays(productDeletePossibleDate)))
        throw new RecentlyPurchasedProduct(
            productDeletePossibleDate + "일동안 구매기록이 없는 제품만 삭제할 수 있습니다.");
    }

    deleteProduct(product);
  }
}
