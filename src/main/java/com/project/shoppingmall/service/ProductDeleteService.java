package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductDeleteService {
  private final ProductRepository productRepository;
  private final BasketItemService basketItemService;
  private final BasketItemDeleteService basketItemDeleteService;
  private final ReviewService reviewService;
  private final ReviewDeleteService reviewDeleteService;
  private final ReportService reportService;
  private final ReportDeleteService reportDeleteService;

  public void deleteProduct(long sellerId, long productId) {
    Product product =
        productRepository
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 Prdocut가 존재하지 않습니다."));
    if (!product.getSeller().getId().equals(sellerId))
      throw new DataNotFound("다른 회원의 Product를 제거하려고 하고 있습니다.");

    List<BasketItem> basketItemList = basketItemService.findAllByProduct(product.getId());
    basketItemDeleteService.deleteBasketItemList(basketItemList);

    List<Review> reviewList = reviewService.findByProduct(product.getId());
    reviewDeleteService.deleteReviewList(reviewList);

    List<ProductReport> productReportList = reportService.findAllByProduct(product.getId());
    reportDeleteService.deleteProductReportList(productReportList);

    productRepository.delete(product);
  }
}
