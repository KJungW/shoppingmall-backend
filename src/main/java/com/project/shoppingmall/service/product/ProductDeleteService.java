package com.project.shoppingmall.service.product;

import com.project.shoppingmall.dto.block.ImageBlock;
import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.RecentlyPurchasedProduct;
import com.project.shoppingmall.exception.ServerLogicError;
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
import com.project.shoppingmall.type.BlockType;
import com.project.shoppingmall.util.JsonUtil;
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
  private final ProductFindService productFindService;
  private final ProductRepository productRepository;
  private final BasketItemFindService basketItemFindService;
  private final BasketItemDeleteService basketItemDeleteService;
  private final ReviewFindService reviewFindService;
  private final ReviewDeleteService reviewDeleteService;
  private final ReportFindService reportFindService;
  private final ReportDeleteService reportDeleteService;
  private final PurchaseItemFindService purchaseItemFindService;
  private final AlarmFindService alarmFindService;
  private final AlarmDeleteService alarmDeleteService;
  private final S3Service s3Service;

  @Value("${project_role.product.delete_possible_day}")
  private Integer productDeletePossibleDate;

  public void deleteProduct(Product product) {
    if (product == null) throw new ServerLogicError("비어있는 Product를 제거하려고 시도하고 있습니다.");

    List<BasketItem> basketItemList = basketItemFindService.findAllByProduct(product.getId());
    basketItemDeleteService.deleteBasketItemList(basketItemList);

    List<Review> reviewList = reviewFindService.findByProduct(product.getId());
    reviewDeleteService.deleteReviewList(reviewList);

    List<ProductReport> productReportList = reportFindService.findAllByProduct(product.getId());
    reportDeleteService.deleteProductReportList(productReportList);

    List<Alarm> alarmsList = alarmFindService.findByTargetProduct(product.getId());
    alarmDeleteService.deleteAlarmList(alarmsList);

    product
        .getProductImages()
        .forEach(
            image -> {
              s3Service.deleteFile(image.getImageUri());
            });
    product
        .getContents()
        .forEach(
            content -> {
              if (content.getType().equals(BlockType.IMAGE_TYPE)) {
                ImageBlock imageBlock =
                    JsonUtil.convertJsonToObject(content.getContent(), ImageBlock.class);
                s3Service.deleteFile(imageBlock.getImageUri());
              }
            });

    productRepository.delete(product);
  }

  public void deleteProductList(List<Product> productList) {
    productList.forEach(this::deleteProduct);
  }

  public void deleteProductInController(long sellerId, long productId) {
    Product product =
        productFindService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 Prdocut가 존재하지 않습니다."));
    if (!product.getSeller().getId().equals(sellerId))
      throw new DataNotFound("다른 회원의 Product를 제거하려고 하고 있습니다.");

    List<PurchaseItem> latestPurchaseList =
        purchaseItemFindService.findLatestByProduct(product.getId(), 1);
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
